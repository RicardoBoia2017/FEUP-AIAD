package MainAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusAgent extends Agent {

    private AID[] targetStop;
    private Coordinates coords;
    private float speed;
    private ArrayList<StopDetails> itinerary = new ArrayList<>();
    private int availableSeats;
    private int totalSeats;
    private int price;
    private float dishonestyDegree;
    private double priceFlexibility;
    private int collaboration;
    private double gain = 0;
    private boolean random = false;

    protected void setup() {

        Object[] args = getArguments();

        if (args == null || args.length != 8) {
            System.err.println("Incorrect number of arguments");
            System.err.println("Bus arguments: startStop endStop speed capacity pricePerMinute dishonestyDegree(0-5) priceFlexibility(0-5) collaboration" );
            doDelete();
        } else {

            coords = new Coordinates(Integer.parseInt((String) args[0]), Integer.parseInt((String) args[1]));
            speed = Float.parseFloat((String) args[2]);
            totalSeats = Integer.parseInt((String) args[3]);
            availableSeats = totalSeats;
            price = Integer.parseInt((String) args[4]);
            dishonestyDegree = Float.parseFloat((String) args[5]) / 10;
            priceFlexibility = Float.parseFloat((String) args[6]) / (5 / 0.30);
            collaboration =  Integer.parseInt((String) args[7]);

            if (dishonestyDegree < 0 || dishonestyDegree > 5) {
                System.out.println("Dishonesty degree value must be between 0 and 5");
                doDelete();
            }

            if (priceFlexibility < 0 || priceFlexibility > 5) {
                System.out.println("Price flexibility value must be between 0 and 5");
                doDelete();
            }

            System.out.println("Bus \"" + getLocalName() + "\" started");

            DFAgentDescription dfd = getTemplate("bus-agency", "JADE-bus-agency", coords, this, null, null);
            dfd.setName(getAID());

            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            addBehaviour(new NegotiationServer(this));

            addBehaviour(new ReservationsServer(this));

            if(collaboration == 1)
                addBehaviour(new RegisterInCollaboration());

            double timeOnCell = (1 / this.speed) * 1000;

            addBehaviour(new TickerBehaviour(this, (long) timeOnCell) {
                BusAgent currentBus = (BusAgent) myAgent;

                protected void onTick() {
                    if (!currentBus.getItinerary().isEmpty()) {
                        currentBus.setCoords(currentBus.getNextPosition());

                        StopDetails nextStop = currentBus.itinerary.get(0);

                        if (currentBus.getCoords().equals(nextStop.getCoords())) {
                            currentBus.availableSeats += currentBus.getItinerary().get(0).getLeavingPassengers().size();

                            currentBus.informPassengersArrived(currentBus.getItinerary().get(0).getLeavingPassengers());
                            currentBus.getItinerary().remove(0);
                            deregisterFromStop(nextStop.getName());
                        }
                    } else {
                        DFAgentDescription[] result;
                        DFAgentDescription templateGlobal = new DFAgentDescription();
                        ServiceDescription sdGlobal = new ServiceDescription();
                        sdGlobal.setType("stop");
                        templateGlobal.addServices(sdGlobal);

                        try {
                            result = DFService.search(myAgent, templateGlobal);

                            targetStop = new AID[result.length];
                            String[] names = new String[result.length];

                            if (result.length > 0) {
                                for (int i = 0; i < result.length; ++i) {
                                    targetStop[i] = result[i].getName();
                                    names[i] = targetStop[i].getLocalName();
                                }

                                int j;
                                do {
                                    j = (int) (Math.random() * ((result.length - 1) + 1));
                                } while (currentBus.getStopDetails(result[j]).getCoords() == this.currentBus.coords);

                                registerInStop(result[j], null, false);
                                random = true;
                            }

                        } catch (FIPAException e) {
                            e.printStackTrace();
                        }
                    }

                    currentBus.updateServiceInfo();
                }
            });
        }

    }

    private Coordinates getNextPosition() {
        Coordinates ret = this.coords;
        Coordinates nextStop = this.itinerary.get(0).getCoords();

        if (this.coords.getY() < nextStop.getY()) {
            ret.setY(ret.getY() + 1);
        } else if (this.coords.getY() > nextStop.getY()) {
            ret.setY(ret.getY() - 1);
        }

        if (this.coords.getX() < nextStop.getX()) {
            ret.setX(ret.getX() + 1);
        } else if (this.coords.getX() > nextStop.getX()) {
            ret.setX(ret.getX() - 1);
        }

        return ret;
    }

    protected void takeDown() {
        System.out.println("Bus " + this.getLocalName() + " stopped operating");
    }

    private class NegotiationServer extends CyclicBehaviour {

        BusAgent currentBus;
        HashMap<String,PassengerInfo> colResponsesLeft = new HashMap<>();

        private NegotiationServer(BusAgent currentBus) {
            this.currentBus = currentBus;
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if(msg.getConversationId().equals("Collaboration"))
                {
                    String passengerName;
                    int collaboratorDistance;

                    collaboratorDistance = Integer.parseInt(msg.getContent().substring(0, msg.getContent().indexOf(" ")));
                    passengerName = msg.getContent().substring(msg.getContent().indexOf(" ") + 1);

                    PassengerInfo pi = colResponsesLeft.get(passengerName);

                    if(pi == null)
                    {
                        ACLMessage reply = msg.createReply();
                        reply.setContent("99999 " + passengerName);
                        myAgent.send(reply);
                    }
                    else {
                        if (pi.getBusDistance() > collaboratorDistance)
                        {
                            ACLMessage reply = pi.getReply();
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent("not-available");
                            myAgent.send(reply);
                        }
                        else {
                            if (pi.getResponsesLeft() == 1)
                            {
                                ACLMessage reply = pi.getReply();
                                reply.setPerformative(ACLMessage.PROPOSE);
                                reply.setContent(pi.getBusTime() + " " + pi.getBusPrice());
                                colResponsesLeft.remove(passengerName);
                                myAgent.send(reply);
                            }
                            else
                                pi.decrementResponsesLeft();
                        }
                    }
                }

                else {
                    String[] args;
                    String startStop, endStop;
                    args = msg.getContent().split(" ");

                    startStop = args[0];
                    endStop = args[1];

                    DFAgentDescription startStopTemplate = currentBus.getStopAgentDescription("stop" + startStop);
                    DFAgentDescription endStopTemplate = currentBus.getStopAgentDescription("stop" + endStop);

                    int distance = currentBus.getPassengerTripDistance(startStopTemplate, endStopTemplate);
                    double time = (distance / currentBus.speed) * (1 - this.currentBus.dishonestyDegree);
                    double price = (currentBus.price * time) / 100;

                    ACLMessage reply = msg.createReply();

                    if(currentBus.collaboration == 1 && msg.getConversationId().equals("Proposal")) {
                        DFAgentDescription colTemplate = getTemplate("collaboration", "col");
                        DFAgentDescription busTemplate = getTemplate("bus", null);

                        DFAgentDescription colDF = null;
                        try {
                            colDF = DFService.search(myAgent, colTemplate)[0];
                            DFAgentDescription[] results = DFService.search(myAgent, colDF.getName(), busTemplate);

                            if(results.length > 1) {
                                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                                for (DFAgentDescription bus : results) {
                                    AID name = bus.getName();

                                    if (!name.getLocalName().equals(getLocalName()))
                                        cfp.addReceiver(name);
                                }

                                cfp.setContent(distance + " " + msg.getSender().getLocalName());
                                cfp.setConversationId("Collaboration");
                                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                                myAgent.send(cfp);

                                colResponsesLeft.put(msg.getSender().getLocalName(), new PassengerInfo(reply, distance, time, price, results.length - 1));

                                return;
                            }
                        } catch (FIPAException e) {
                            e.printStackTrace();
                        }
                    }

                    if (availableSeats > 0) {
                        createReply(reply, args, time, price);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("not-available");
                    }

                    myAgent.send(reply);
                }
            } else {
                block();
            }
        }

        private void createReply(ACLMessage reply, String[] args, double time, double price) {

            if (args.length > 2) {
                double bestPrice = Double.parseDouble(args[2]);
                double discountNeeded = 1 - (bestPrice / price);

                if ((discountNeeded + 0.02) > currentBus.priceFlexibility) {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                } else {
                    price = (1 - (discountNeeded + 0.02)) * price;
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(time + " " + price);
                }
            } else {
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(time + " " + price);
            }
        }

    }

    private class ReservationsServer extends CyclicBehaviour {

        BusAgent currentBus;

        private ReservationsServer(BusAgent currentBus) {
            this.currentBus = currentBus;
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String[] stops;
                String startStop, endStop;

                stops = msg.getContent().split("");

                startStop = stops[0];
                endStop = stops[2];

                ACLMessage reply = msg.createReply();

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

                try {
                    DFAgentDescription[] resultStartStop = DFService.search(myAgent, templateStart);
                    DFAgentDescription[] resultEndStop = DFService.search(myAgent, templateEnd);

                    if (resultStartStop.length == 0) {
                        System.err.println("Start stop doesn't exist");
                        return;
                    }
                    if (resultEndStop.length == 0) {
                        System.err.println("End stop doesn't exist");
                        return;
                    }

                    if (!StopDetails.checkIfStartEndInOrder(resultStartStop[0].getName().getLocalName(), resultEndStop[0].getName().getLocalName(), currentBus.itinerary)) {
                        if (StopDetails.getFirstStopByName(resultStartStop[0].getName().getLocalName(), currentBus.itinerary) != null) {
                            registerInStop(resultEndStop[0], msg.getSender(), true);
                        } else {
                            registerInStop(resultStartStop[0], null, true);
                            registerInStop(resultEndStop[0], msg.getSender(), true);
                        }
                    } else {
                        registerInStop(resultEndStop[0], msg.getSender(), false);
                    }

                    currentBus.availableSeats--;

                    if ((resultStartStop.length != 0) && (resultEndStop.length != 0)) {
                        int distance = currentBus.getPassengerTripDistance(resultStartStop[0], resultEndStop[0]);
                        String time = String.valueOf((distance / currentBus.speed) * (1 - this.currentBus.dishonestyDegree));
                        String price = String.valueOf((currentBus.price * Double.parseDouble(time)) / 100);
                        currentBus.gain += Double.parseDouble(price);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                reply.setPerformative(ACLMessage.INFORM);
                myAgent.send(reply);
            } else {
                block();
            }
        }

    }

    private class RegisterInCollaboration extends SimpleBehaviour {

        private boolean registered = false;

        @Override
        public void action() {

            DFAgentDescription[] results = null;

            DFAgentDescription colTemplate = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("collaboration");
            colTemplate.addServices(sd);

            DFAgentDescription busTemplate = new DFAgentDescription();
            busTemplate.setName(myAgent.getAID());
            sd = new ServiceDescription();
            sd.setType("bus");
            sd.setName(myAgent.getLocalName());
            busTemplate.addServices(sd);

            try {
                do {
                    results = DFService.search(myAgent, colTemplate);
                } while (results.length == 0);

                DFService.register(myAgent, results[0].getName(), busTemplate);
                registered = true;
            }
            catch(FIPAException fe)
            {
                System.err.println(fe.toString());
                fe.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return registered;
        }
    }

    private void registerInStop(DFAgentDescription stop, AID passengerDestiny, boolean addRepeated) {

        DFAgentDescription busTemplate = new DFAgentDescription();
        busTemplate.setName(this.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bus");
        sd.setName(this.getLocalName());
        busTemplate.addServices(sd);

        if (random && !this.itinerary.isEmpty()) {
            deregisterFromStop(this.itinerary.get(0).getName());
            this.itinerary.clear();
            random = false;
        }
        try {
            DFAgentDescription[] results = DFService.search(this, stop.getName(), busTemplate);

            if (results.length == 0 || addRepeated) {

                if (results.length == 0) {
                    DFService.register(this, stop.getName(), busTemplate);
                }

                StopDetails stopDetails = getStopDetails(stop);
                if (passengerDestiny != null) {
                    stopDetails.setLeavingPassenger(passengerDestiny);
                }

                if(passengerDestiny == null && !this.itinerary.isEmpty()){
                    this.addToBestItineraryPosition(stopDetails);
                } else {
                    this.itinerary.add(stopDetails);
                }

            } else if (passengerDestiny != null) {
                StopDetails.getFirstStopByName(stop.getName().getLocalName(), this.itinerary).setLeavingPassenger(passengerDestiny);
            }
        } catch (FIPAException fe) {
            System.err.println(fe.toString());
            fe.printStackTrace();
        }

    }

    public ArrayList<StopDetails> getItinerary() {
        return itinerary;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    private void deregisterFromStop(String nextStop) {

        DFAgentDescription stopTemplate = getTemplate("stop", nextStop);

        try {
            DFAgentDescription stopDF = DFService.search(this, stopTemplate)[0];

            DFAgentDescription busTemplate = getTemplate("bus", getLocalName());
            busTemplate.setName(getAID());

            if (StopDetails.getFirstStopByName(stopDF.getName().getLocalName(), this.itinerary) == null)
                DFService.deregister(this, stopDF.getName(), busTemplate);

        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }

    private StopDetails getStopDetails(DFAgentDescription stop) {
        Iterator serviceIterator = stop.getAllServices();
        serviceIterator.next();

        ServiceDescription serviceStop = (ServiceDescription) serviceIterator.next();

        Iterator propertyIterator = serviceStop.getAllProperties();
        Coordinates stopCoords = new Coordinates();
        stopCoords.setX(Integer.parseInt((String) (((Property) propertyIterator.next()).getValue())));
        stopCoords.setY(Integer.parseInt((String) (((Property) propertyIterator.next()).getValue())));

        return new StopDetails(stop.getName().getLocalName(), stopCoords);
    }

    public int getPassengerTripDistance(DFAgentDescription startStop, DFAgentDescription endStop) {
        ArrayList<StopDetails> futureItinerary = new ArrayList<>(this.itinerary);

        if (StopDetails.getFirstStopByName(startStop.getName().getLocalName(), futureItinerary) == null) {
            futureItinerary.add(this.getStopDetails(startStop));
        }

        if (StopDetails.getFirstStopByName(endStop.getName().getLocalName(), futureItinerary) == null) {
            futureItinerary.add(this.getStopDetails(endStop));
        }

        int distance = this.coords.calculateDistance(futureItinerary.get(0).getCoords());

        for (int i = 1; i < futureItinerary.size(); i++) {

            distance += futureItinerary.get(i - 1).getCoords().calculateDistance(futureItinerary.get(i).getCoords());

            if (futureItinerary.get(i).getName().equals(endStop.getName().getLocalName())) {
                break;
            }
        }

        return distance;
    }

    private DFAgentDescription getStopAgentDescription(String stopName) {
        DFAgentDescription stopTemplate = getTemplate("stop", stopName);

        try {
            return DFService.search(this, stopTemplate)[0];
        } catch (FIPAException ex) {
            Logger.getLogger(BusAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static DFAgentDescription getTemplate(String type, String name) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdStart = new ServiceDescription();

        if (type != null) {
            sdStart.setType(type);
        }
        if (name != null) {
            sdStart.setName(name);
        }

        template.addServices(sdStart);

        return template;
    }

    static DFAgentDescription getTemplate(String type, String name, Coordinates coords) {
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

    static DFAgentDescription getTemplate(String type, String name, Coordinates coords, Agent myAgent, Double occupancyRate, Double gain) {
        DFAgentDescription template = getTemplate(type, name, coords);
        template.setName(myAgent.getAID());

        ServiceDescription sd = (ServiceDescription) template.getAllServices().next();
        if (occupancyRate != null) {
            Property occupancy = new Property();
            occupancy.setName("Occupancy");
            occupancy.setValue(occupancyRate);
            sd.addProperties(occupancy);
        }

        if (gain != null) {
            Property gainProp = new Property();
            gainProp.setName("Gain");
            gainProp.setValue(gain);
            sd.addProperties(gainProp);
        }

        return template;
    }

    static void setProperty(ServiceDescription sd, String name, double value) {
        Property occupancy = new Property();
        occupancy.setName(name);
        occupancy.setValue(value);
        sd.addProperties(occupancy);
    }

    private void updateServiceInfo() {
        DFAgentDescription template = getTemplate("bus-agency", "JADE-bus-agency", this.coords);

        ServiceDescription sd = (ServiceDescription) template.getAllServices().next();
        setProperty(sd, "Occupancy", this.getOccupancyRate());
        setProperty(sd, "Gain", this.gain);

        try {
            DFService.modify(this, template);
        } catch (FIPAException ex) {
            Logger.getLogger(BusAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double getOccupancyRate() {
        int occupiedSeats = this.totalSeats - this.availableSeats;
        return (double) occupiedSeats / this.totalSeats;
    }

    private void informPassengersArrived(ArrayList<AID> leavingPassengers) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        for (AID passenger : leavingPassengers) {
            message.addReceiver(passenger);
        }
        message.setContent("ARRIVED TO DESTINATION");
        this.send(message);
    }

    private void addToBestItineraryPosition(StopDetails newStop) {
        ArrayList<StopDetails> tmpItenerary = new ArrayList<>(this.itinerary);
        ArrayList<StopDetails> bestItenerary = new ArrayList<>();
        int minDistance = Integer.MIN_VALUE;
        int totalDistance = 0;

        for(int i = 0;i<=this.itinerary.size();i++){
            tmpItenerary = new ArrayList<>(this.itinerary);
            if(tmpItenerary.isEmpty()) {
                totalDistance = this.coords.calculateDistance(newStop.getCoords());
            }else{
                totalDistance = this.coords.calculateDistance(tmpItenerary.get(0).getCoords());
            }

            tmpItenerary.add(i, newStop);

            for(int x = 0;x<tmpItenerary.size()-1;x++) {
                totalDistance += tmpItenerary.get(x).getCoords().calculateDistance(tmpItenerary.get(x+1).getCoords());
            }

            if(totalDistance < minDistance){
                minDistance=totalDistance;
                bestItenerary=tmpItenerary;
            }
        }

        this.itinerary = new ArrayList<>(bestItenerary);
    }
}
