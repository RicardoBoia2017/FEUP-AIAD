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

import java.util.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusAgent extends Agent{

    private Coordinates coords;
    private float speed = 1; //cells per second //TODO: Dynamic speed
    private Map<String, StopDetails> itinerary = new LinkedHashMap<>();
    private int availableSeats = 40; //TODO this value can change
    private int pricePerMinute = 10; //cents (its easier)
   
    protected void setup() {

        Object[] args = getArguments();

        if(args == null || args.length != 2) {
            System.out.println("No starting stop and/or ending stop specified");
            doDelete();
        }
        else {
            coords = new Coordinates((int) args[0], (int) args[1]);
            System.out.println("Bus \"" + getLocalName() + "\" started");

            DFAgentDescription dfd = getTemplate("bus-agency","JADE-bus-agency",coords,this);
            dfd.setName(getAID());
            

            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }


            addBehaviour(new OfferRequestsServer(this));

            addBehaviour(new ReservationOrdersServer(this));

            float timeOnCell = (1 / this.speed) * 1000; //time (in milliseconds) spent in a cell

            //moves a cell at a time
            addBehaviour(new TickerBehaviour(this, (long) timeOnCell) {
                BusAgent currentBus = (BusAgent) myAgent;

                protected void onTick() {
                    //if there is a next stop
                    if (currentBus.getItinerary().size() > 0) {
                        currentBus.setCoords(currentBus.getNextPosition());
                        System.out.println(currentBus.getLocalName() + " CURRENT POSITION : " + currentBus.getCoords().getX() + " " + currentBus.getCoords().getY());

                        String nextStop = currentBus.itinerary.entrySet().iterator().next().getKey();
                        Coordinates nextStopCoords = currentBus.itinerary.entrySet().iterator().next().getValue().getCoords();

                        //arrived to stop, remove it from itinerary
                        if (currentBus.getCoords().equals(nextStopCoords)) {
                            System.out.println(currentBus.getLocalName() + " ARRIVED AT " + nextStop);

                            currentBus.availableSeats += currentBus.getItinerary().get(nextStop).getLeavingPassengers();
                            System.out.println("Available seats: " + currentBus.availableSeats);

                            currentBus.getItinerary().remove(nextStop);
                            deregisterFromStop(nextStop);
                        }
                    }
                    
                    //inform map of current position
                    currentBus.updateServiceCoords();

                }
            });
        }
        
    }

    //bus next position based on next stop
    private Coordinates getNextPosition(){
        Coordinates ret = this.coords;
        Coordinates nextStop = this.itinerary.entrySet().iterator().next().getValue().getCoords();
        
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
       /* try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }*/

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

        BusAgent currentBus;

        private OfferRequestsServer(BusAgent currentBus) {
            this.currentBus = currentBus;
        }

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
                
                DFAgentDescription startStopTemplate = currentBus.getStopAgentDescription("stop"+startStop);         
                DFAgentDescription endStopTemplate = currentBus.getStopAgentDescription("stop"+endStop);

                //Coordinates stopCoords = getStopCoordinates(startStopTemplate);
                Coordinates stopCoords = getStopCoordinates(startStopTemplate).getCoords();

                ACLMessage reply = msg.createReply();

                //int distance = currentBus.getCoords().calculateDistance(stopCoords);
                int distance = currentBus.getPassengerTripDistance(startStopTemplate, endStopTemplate);
                
                //System.out.println(currentBus.getCoords() + " - " + stopCoords + " = " + distance);
                System.out.println(" NEW DISTANCE = " + currentBus.getPassengerTripDistance(startStopTemplate, endStopTemplate));

                if (availableSeats > 0) {
                    // The bus can give a lift to the passenger
                    String time = String.valueOf(distance/ currentBus.speed);
                    String price = String.valueOf((currentBus.pricePerMinute * Double.valueOf(time)) / 100);

                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(time + " " + price);
                } else {
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
                        registerInStop(resultStartStop[0], false);
                    }
                    
                    if(resultEndStop.length == 0){
                        System.err.println("End stop doesn't exist");
                    }else{
                        registerInStop(resultEndStop[0], true);
                    }

                    currentBus.availableSeats--;

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
        
        private void registerInStop(DFAgentDescription stop, boolean destiny){
            DFAgentDescription busTemplate = new DFAgentDescription();
            busTemplate.setName( currentBus.getAID() );
            ServiceDescription sd  = new ServiceDescription();
            sd.setType("bus");
            sd.setName( currentBus.getLocalName() );
            busTemplate.addServices(sd);

            try {
                DFAgentDescription[] results = DFService.search(myAgent, stop.getName(), busTemplate);

                if(results.length == 0) {
                    DFService.register(currentBus, stop.getName(), busTemplate);
                    StopDetails stopDetails = getStopCoordinates(stop);
                    if(destiny)
                        stopDetails.setLeavingPassengers(1);

                    currentBus.itinerary.put(stop.getName().getLocalName(), stopDetails);
                }

                else if (destiny)
                    this.currentBus.itinerary.get(stop.getName().getLocalName()).setLeavingPassengers(1);
            }
            catch (FIPAException fe) {
                System.err.println(fe.toString());
                fe.printStackTrace(); 
            }

        }

    }  // End of inner class OfferRequestsServer

    public  Map<String, StopDetails> getItinerary() {
        return itinerary;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    private void deregisterFromStop(String nextStop) {

        DFAgentDescription stopTemplate = getTemplate("stop",nextStop);

        try { 
            DFAgentDescription stopDF = DFService.search(this, stopTemplate)[0];

            DFAgentDescription busTemplate = getTemplate("bus",getLocalName());
            busTemplate.setName(getAID());

            DFService.deregister(this, stopDF.getName(), busTemplate);

        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }

    private StopDetails getStopCoordinates(DFAgentDescription stop)
    {
        Iterator serviceIterator = stop.getAllServices();
        serviceIterator.next();
        //serviceIterator.remove(); //skips first service

        ServiceDescription serviceStop = (ServiceDescription) serviceIterator.next();

        Iterator propertyIterator = serviceStop.getAllProperties();
        Coordinates stopCoords = new Coordinates();
        stopCoords.setX(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));
        //propertyIterator.remove();
        stopCoords.setY(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));

        return new StopDetails(stopCoords, 0);
    }
    
    
    //gets distance from current position 2 to end stop in the current bus itinerary
    public int getPassengerTripDistance(DFAgentDescription startStop,DFAgentDescription endStop){
        //creates a possible itinerary if it would accept the passanger
        Map<String,StopDetails> futureItinerary = new HashMap<>(this.itinerary);

        //if bus doesnt have start stop, adds it to itinerary
        if(futureItinerary.get(startStop.getName().getLocalName()) == null){
            futureItinerary.put(startStop.getName().getLocalName(), this.getStopCoordinates(startStop));
        }
        
        //if bus doesnt have end stop, adds it to itinerary
        if(futureItinerary.get(endStop.getName().getLocalName()) == null){
            futureItinerary.put(endStop.getName().getLocalName(), this.getStopCoordinates(endStop));
        }
        
        Iterator<String> itineraryIt = futureItinerary.keySet().iterator();
        
        String prevStopName = itineraryIt.next();
        DFAgentDescription prevStop = this.getStopAgentDescription(prevStopName); //previous stop
        //itineraryIt.remove();
        String curStopName;
        DFAgentDescription curStop; //current stop
        
        int distance = this.coords.calculateDistance(this.getStopCoordinates(prevStop).getCoords()); //initial distance between bus position and next stop
        
        //goes around the itinerary to calculate total distance until the end stop
        while(itineraryIt.hasNext()){
             curStopName = itineraryIt.next();
             curStop = this.getStopAgentDescription(curStopName);
            
            distance += this.getStopCoordinates(prevStop).getCoords().calculateDistance(this.getStopCoordinates(curStop).getCoords());
             
            //end of the trip for passenger
            if(curStopName.equals(endStop.getName().getLocalName())){
                 break;
             }
             
             prevStop=curStop;
        }
        
        
        return distance;
    }
    
    
    private DFAgentDescription getStopAgentDescription(String stopName){
        DFAgentDescription stopTemplate = getTemplate("stop", stopName);
        
        try {
            return DFService.search(this, stopTemplate)[0];
        } catch (FIPAException ex) {
            Logger.getLogger(BusAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    static DFAgentDescription getTemplate(String type, String name)
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdStart = new ServiceDescription();

        if (type != null)
            sdStart.setType(type);
        if(name != null)
            sdStart.setName(name);
        
        template.addServices(sdStart);

        return template;
    }
    
    static DFAgentDescription getTemplate(String type, String name,Agent myAgent){
       DFAgentDescription template = getTemplate(type, name);
       template.setName(myAgent.getAID());
       
       return template;
    }
    
    static DFAgentDescription getTemplate(String type,String name,Coordinates coords){
        DFAgentDescription template = getTemplate(type, name);
        
        //Service description with coordinates as properties
        ServiceDescription sd = (ServiceDescription) template.getAllServices().next();
        Property coordCol = new Property();
        coordCol.setName("Col");
        coordCol.setValue(coords.getX());
        Property coordLine = new Property();
        coordLine.setName("Line");
        coordLine.setValue(coords.getY());
        sd.addProperties(coordCol);
        sd.addProperties(coordLine);
        
        return template;
    }
    
     static DFAgentDescription getTemplate(String type, String name,Coordinates coords,Agent myAgent){
       DFAgentDescription template = getTemplate(type, name, coords);
       template.setName(myAgent.getAID());
       
       return template;
    }
    
    void updateServiceCoords(){
        DFAgentDescription template = getTemplate("bus-agency", "JADE-bus-agency",this.coords,this);
        
        try {
            DFService.modify(this, template);
        } catch (FIPAException ex) {
            Logger.getLogger(BusAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
