import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class BusAgent extends Agent{

    private Coordinates coords;
    private float speed = 1; //cells per second //TODO: Dynamic speed
    private java.util.Map<String,Coordinates> itinerary = new LinkedHashMap<>();
    private int availableSeats = 40; //TODO this value can change
   
    protected void setup() {
        this.coords = new Coordinates(0,0); //TODO define starting point
        System.out.println("Bus started");

        //register bus directly on main DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bus-agency");
        sd.setName("JADE-bus-agency");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
     
        addBehaviour(new OfferRequestsServer());
        
        addBehaviour(new ReservationOrdersServer(this));
        
        float timeOnCell= (1/this.speed)*1000; //time (in milliseconds) spent in a cell
        
        //moves a cell at a time
        addBehaviour(new TickerBehaviour(this, (long) timeOnCell) {
            BusAgent currentBus = (BusAgent)myAgent;
            protected void onTick() {
                //if there is a next stop
                if(currentBus.getItinerary().size()>0){
                    currentBus.setCoords(currentBus.getNextPosition());
                    System.out.println(currentBus.getLocalName()+" CURRENT POSITION : "+currentBus.getCoords().getX()+" "+currentBus.getCoords().getY());

                    String nextStop = currentBus.itinerary.entrySet().iterator().next().getKey();
                    Coordinates nextStopCoords = (Coordinates)currentBus.itinerary.entrySet().iterator().next().getValue();
                    
                    //arrived to stop, remove it from itinerary
                    if(currentBus.getCoords().equals(nextStopCoords)){
                        System.out.println(currentBus.getLocalName()+" ARRIVED AT "+ nextStop);

                        currentBus.getItinerary().remove(nextStop);
                        deregisterFromStop(nextStop);
                    }
                }
                
            }
        });
        
    }

    //bus next position based on next stop
    private Coordinates getNextPosition(){
        Coordinates ret = this.coords;
        Coordinates nextStop = this.itinerary.entrySet().iterator().next().getValue();
        
        //verify the line
        if(this.coords.getY()<nextStop.getY()){
            ret.setY(ret.getY()+1);
        }else if(this.coords.getY()>nextStop.getY()){
            ret.setY(ret.getY()-1);
        }
        
        //verify the column
        if(this.coords.getX()<nextStop.getX()){
            ret.setX(ret.getX()+1);
        }else if(this.coords.getX()>nextStop.getX()){
            ret.setX(ret.getX()-1);
        }
        
        return ret;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Bus stopped operating");
    }

    /**
     Inner class OfferRequestsServer.
     This is the behaviour used by Bus agents to serve incoming requests
     for offer from buyer agents.
     If the bus can pick the client, then it replies
     with a PROPOSE message specifying the time/price. Otherwise a REFUSE message is
     sent back.
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String[] stops;
                String startStop, endStop;
                // CFP Message received. Process it
                stops = msg.getContent().split("");

                startStop = stops[0];
                endStop = stops[2];

                System.out.println("Stops : " + startStop + ", " + endStop);
                ACLMessage reply = msg.createReply();

                Integer price = (int)(Math.random() * 30); //TODO this value is not random

                if (availableSeats > 0) {
                    // The bus can give a lift to the passenger
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price));
                }
                else {
                    // The bus is not available for that passenger
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }

                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
    
    
    
    /**
     */
    private class ReservationOrdersServer extends CyclicBehaviour {

        BusAgent currentBus;

        private ReservationOrdersServer(BusAgent currentBus) {
            this.currentBus = currentBus;
        }
  
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String[] stops;
                String startStop, endStop;
                // CFP Message received. Process it
                stops = msg.getContent().split("");

                startStop = stops[0];
                endStop = stops[2];

                System.out.println("Accepts Stops : " + startStop + ", " + endStop);
                ACLMessage reply = msg.createReply();
                
                //Bus tries to register on DF of respective stops
                DFAgentDescription templateStart = new DFAgentDescription();
                ServiceDescription sdStart = new ServiceDescription();
                sdStart.setType("stop");
                sdStart.setName("stop" + startStop);
                templateStart.addServices(sdStart);
                
                DFAgentDescription templateEnd = new DFAgentDescription();
                ServiceDescription sdEnd = new ServiceDescription();
                sdEnd.setType("stop");
                sdEnd.setName("stop" + endStop);
                templateEnd.addServices(sdEnd);

                //bus registers in both start and end stops of the passenger
                try {
                    DFAgentDescription[] resultStartStop = DFService.search(myAgent, templateStart);
                    DFAgentDescription[] resultEndStop = DFService.search(myAgent, templateEnd);
                    
                    if(resultStartStop.length == 0){
                        System.err.println("Start stop doesn't exist");
                    }else{
                        registerInStop(resultStartStop[0]);
                    }
                    
                    if(resultEndStop.length == 0){
                        System.err.println("End stop doesn't exist");
                    }else{
                        registerInStop(resultEndStop[0]);
                    }
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                
                
                reply.setPerformative(ACLMessage.INFORM);
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
        
        private void registerInStop(DFAgentDescription stop){
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( currentBus.getAID() );
            ServiceDescription sd  = new ServiceDescription();
            sd.setType("bus");
            sd.setName( currentBus.getLocalName() );
            dfd.addServices(sd);

            try {
                DFAgentDescription[] results = DFService.search(myAgent, stop.getName(), dfd);

                if(results.length == 0)
                    DFService.register(currentBus,stop.getName(), dfd );
            }
            catch (FIPAException fe) {
                System.err.println(fe.toString());
                fe.printStackTrace(); 
            }
            
            //find the stop coordinates in its DFAgentDescription
            Iterator serviceIterator = stop.getAllServices();
            serviceIterator.next();
            serviceIterator.remove(); //skips first service
            
            ServiceDescription serviceStop = (ServiceDescription) serviceIterator.next();
             
            Iterator propertyIterator = serviceStop.getAllProperties();
            Coordinates stopCoords = new Coordinates();
            stopCoords.setX(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));
            propertyIterator.remove();
            stopCoords.setY(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));
            
            
            //add stop to itenerary
            currentBus.itinerary.put(stop.getName().getLocalName(),stopCoords);
        }
        
    }  // End of inner class OfferRequestsServer


    public Map<String, Coordinates> getItinerary() {
        return itinerary;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    private void deregisterFromStop(String nextStop) {

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdEnd = new ServiceDescription();
        sdEnd.setType("stop");
        sdEnd.setName(nextStop);
        template.addServices(sdEnd);

        try {
            DFAgentDescription stop = DFService.search(this, template)[0];

            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd  = new ServiceDescription();
            sd.setType("bus");
            sd.setName(getLocalName());
            dfd.addServices(sd);

            DFService.deregister(this, stop.getName(), dfd);

        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }
}
