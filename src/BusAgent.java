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

import java.util.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusAgent extends Agent{

    private AID[] targetStop;
    private Coordinates coords;
    private float speed;
    private Map<String, StopDetails> itinerary = new LinkedHashMap<>();
    private int availableSeats;
    private int totalSeats;
    private int pricePerMinute = 10;
    private float dishonestyDegree = (float) 0.5;
    private double gain=0;
    private boolean random = false;
    protected void setup() {

        Object[] args = getArguments();

        if(args == null || args.length != 6) {
            System.err.println("Incorrect number of arguments");
            System.err.println("Bus arguments: startStop endStop speed capacity pricePerMinute dishonestyDegree(0-5)");
            doDelete();
        }

        else {
            coords = new Coordinates(Integer.parseInt((String) args[0]), Integer.parseInt((String) args[1]));
            speed = Float.parseFloat((String) args[2]);
            totalSeats = Integer.parseInt((String) args[3]);
            availableSeats=totalSeats;
            pricePerMinute = Integer.parseInt((String) args[4]);
            dishonestyDegree = Float.parseFloat((String) args[5]) / 10;

            if(dishonestyDegree < 0 || dishonestyDegree > 5)
            {
                System.out.println("Dishonesty degree value must be between 0 and 5");
                doDelete();
            }

            System.out.println("Bus \"" + getLocalName() + "\" started");

            DFAgentDescription dfd = getTemplate("bus-agency","JADE-bus-agency",coords,this,null,null);
            dfd.setName(getAID());

            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }


            addBehaviour(new OfferRequestsServer(this));

            addBehaviour(new ReservationOrdersServer(this));

            double timeOnCell = (1 / this.speed) * 1000; //time (in milliseconds) spent in a cell

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

                            currentBus.availableSeats += currentBus.getItinerary().get(nextStop).getLeavingPassengers().size();
                            System.out.println("Available seats: " + currentBus.availableSeats);

                            currentBus.informPassengersArrived(currentBus.getItinerary().get(nextStop).getLeavingPassengers());
                            currentBus.getItinerary().remove(nextStop);
                            deregisterFromStop(nextStop);
                        }
                    }
                    
                    else if(currentBus.getItinerary().size() == 0)  {

                        DFAgentDescription[] result;
                    	 DFAgentDescription templateGlobal = new DFAgentDescription();
                         ServiceDescription sdGlobal = new ServiceDescription();
                         sdGlobal.setType("stop");
                         templateGlobal.addServices(sdGlobal);

                         try {
							result = DFService.search(myAgent, templateGlobal);

	                         targetStop = new AID[result.length];
                             String[] names = new String[result.length];

                             if(result.length > 0) {
                                 for (int i = 0; i < result.length; ++i) {
                                     targetStop[i] = result[i].getName();
                                     names[i]=targetStop[i].getLocalName();
                                 }

                                 int j;
                                 do {
                                     j = (int) (Math.random() * ((result.length - 1) + 1));
                                 }while(currentBus.getStopCoordinates(result[j]).getCoords()==this.currentBus.coords);

                                 registerInStop(result[j],null);
                                 random = true;
                             }

						} catch (FIPAException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                    
                    //inform map of current position
                    currentBus.updateServiceInfo();

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
     for offer from passenger agents.
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
                stops = msg.getContent().split("");

                startStop = stops[0];
                endStop = stops[2];
                
                DFAgentDescription startStopTemplate = currentBus.getStopAgentDescription("stop"+startStop);         
                DFAgentDescription endStopTemplate = currentBus.getStopAgentDescription("stop"+endStop);

                ACLMessage reply = msg.createReply();

                int distance = currentBus.getPassengerTripDistance(startStopTemplate, endStopTemplate);

                if (availableSeats > 0) {
                    String time = String.valueOf((distance/ currentBus.speed) * (1 - this.currentBus.dishonestyDegree));
                    String price = String.valueOf((currentBus.pricePerMinute * Double.parseDouble(time)) / 100);

                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(time + " " + price);
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }

                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

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
                        registerInStop(resultStartStop[0], null);
                    }
                    
                    if(resultEndStop.length == 0){
                        System.err.println("End stop doesn't exist");
                    }else{
                        registerInStop(resultEndStop[0], msg.getSender());
                    }

                    currentBus.availableSeats--;
                    
                    if((resultStartStop.length != 0)&&(resultEndStop.length != 0)){
                        int distance = currentBus.getPassengerTripDistance(resultStartStop[0], resultEndStop[0]);
                        String time = String.valueOf((distance/ currentBus.speed) * (1 - this.currentBus.dishonestyDegree));
                        String price = String.valueOf((currentBus.pricePerMinute * Double.parseDouble(time)) / 100);
                        currentBus.gain+=Double.parseDouble(price);
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

    }
    private void registerInStop(DFAgentDescription stop, AID passengerDestiny){
        DFAgentDescription busTemplate = new DFAgentDescription();
        busTemplate.setName( this.getAID() );
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("bus");
        sd.setName( this.getLocalName() );
        busTemplate.addServices(sd);

        if(random && !this.itinerary.isEmpty()){
            deregisterFromStop(this.itinerary.entrySet().iterator().next().getKey());
            this.itinerary.clear();
            random=false;
        }
        try {
            DFAgentDescription[] results = DFService.search(this, stop.getName(), busTemplate);

            if(results.length == 0) {


                DFService.register(this, stop.getName(), busTemplate);
                StopDetails stopDetails = getStopCoordinates(stop);
                if(passengerDestiny!=null)
                    stopDetails.setLeavingPassenger(passengerDestiny);

                this.itinerary.put(stop.getName().getLocalName(), stopDetails);
            }

            else if (passengerDestiny!=null)
                this.itinerary.get(stop.getName().getLocalName()).setLeavingPassenger(passengerDestiny);
        }
        catch (FIPAException fe) {
            System.err.println(fe.toString());
            fe.printStackTrace();
        }

    }

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

        ServiceDescription serviceStop = (ServiceDescription) serviceIterator.next();

        Iterator propertyIterator = serviceStop.getAllProperties();
        Coordinates stopCoords = new Coordinates();
        stopCoords.setX(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));
        stopCoords.setY(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));

        return new StopDetails(stopCoords);
    }
    
    
    public int getPassengerTripDistance(DFAgentDescription startStop,DFAgentDescription endStop){
        Map<String,StopDetails> futureItinerary = new HashMap<>(this.itinerary);

        if(futureItinerary.get(startStop.getName().getLocalName()) == null){
            futureItinerary.put(startStop.getName().getLocalName(), this.getStopCoordinates(startStop));
        }
        
        if(futureItinerary.get(endStop.getName().getLocalName()) == null){
            futureItinerary.put(endStop.getName().getLocalName(), this.getStopCoordinates(endStop));
        }
        
        Iterator<String> itineraryIt = futureItinerary.keySet().iterator();
        
        String prevStopName = itineraryIt.next();
        DFAgentDescription prevStop = this.getStopAgentDescription(prevStopName);
        String curStopName;
        DFAgentDescription curStop;
        
        int distance = this.coords.calculateDistance(this.getStopCoordinates(prevStop).getCoords());
        
        while(itineraryIt.hasNext()){
             curStopName = itineraryIt.next();
             curStop = this.getStopAgentDescription(curStopName);
            
            distance += this.getStopCoordinates(prevStop).getCoords().calculateDistance(this.getStopCoordinates(curStop).getCoords());
             
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

    static DFAgentDescription getTemplate(String type,String name,Coordinates coords){
        DFAgentDescription template = getTemplate(type, name);
        
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
     
    static DFAgentDescription getTemplate(String type, String name,Coordinates coords,Agent myAgent,Double occupancyRate, Double gain){
       DFAgentDescription template = getTemplate(type, name, coords);
       template.setName(myAgent.getAID());
       
        ServiceDescription sd = (ServiceDescription) template.getAllServices().next();
        if(occupancyRate != null) {
            Property occupancy = new Property();
            occupancy.setName("Occupancy");
            occupancy.setValue(occupancyRate);
            sd.addProperties(occupancy);
        }

        if(gain != null) {
            Property gainProp = new Property();
            gainProp.setName("Gain");
            gainProp.setValue(gain);
            sd.addProperties(gainProp);
        }
       
       return template;
    }

    static void setProperty(ServiceDescription sd, String name, double value)
    {
        Property occupancy = new Property();
        occupancy.setName(name);
        occupancy.setValue(value);
        sd.addProperties(occupancy);
    }
    
    private void updateServiceInfo(){
        DFAgentDescription template = getTemplate("bus-agency", "JADE-bus-agency",this.coords);

        ServiceDescription sd = (ServiceDescription) template.getAllServices().next();
        setProperty(sd, "Occupancy",this.getOccupancyRate());
        setProperty(sd, "Gain",this.gain);

        try {
            DFService.modify(this, template);
        } catch (FIPAException ex) {
            Logger.getLogger(BusAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private double getOccupancyRate(){
        int occupiedSeats = this.totalSeats-this.availableSeats;
        return (double)occupiedSeats/this.totalSeats;
    }
    
    
    private void informPassengersArrived(ArrayList<AID> leavingPassengers){
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        for (AID passenger: leavingPassengers) {
            message.addReceiver(passenger);
        }
        message.setContent("ARRIVED TO DESTINATION");
        this.send(message);
    }
}
