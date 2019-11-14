package MainAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PassengerAgent extends Agent {

    private AID[] targetBuses;
    private AID statsAgent;
    private int startStop;
    private int endStop;
    private double alpha;
    private double estimatedTime;
    private Instant instantOfEstimation;

    protected void setup() {
        Object[] args = getArguments();
        
        try {
            DFAgentDescription statsTemplate = getTemplate("stats",null);
            while(DFService.search(this, statsTemplate).length==0){}
            
            DFAgentDescription stats = DFService.search(this, statsTemplate)[0]; 
            this.statsAgent = stats.getName();
        } catch (FIPAException ex) {
            Logger.getLogger(PassengerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (args != null && args.length == 3) {
            startStop = Integer.parseInt((String) args[0]);
            endStop = Integer.parseInt((String) args[1]);
            alpha = Double.parseDouble((String) args[2]) / 100;

            if(alpha < 0 || alpha > 100)
            {
                System.out.println("Time preference value has to be between 0 and 100");
                doDelete();
            }

            System.out.println("Client Name: " + getLocalName() + " Stops: " + startStop + " -> " + endStop);

            addBehaviour(new TickerBehaviour(this, 5000) {
                protected void onTick() {
                    if(((PassengerAgent)myAgent).instantOfEstimation==null){
                        DFAgentDescription busTemplate = getTemplate("bus",null);
                        DFAgentDescription startStopTemplate = getTemplate("stop", "stop" + startStop);

                        try {
                            DFAgentDescription stopDF = DFService.search(myAgent, startStopTemplate)[0];
                            DFAgentDescription[] result = DFService.search(myAgent, stopDF.getName(), busTemplate);

                            if(result.length > 0) {
                                System.out.println("Found the following buses:");

                                targetBuses = new AID[result.length];

                                for (int i = 0; i < result.length; ++i) {
                                    targetBuses[i] = result[i].getName();
                                    System.out.println(targetBuses[i].getName());
                                }

                                System.out.println();
                            }

                            else
                            {
                                DFAgentDescription templateGlobal = new DFAgentDescription();
                                ServiceDescription sdGlobal = new ServiceDescription();
                                sdGlobal.setType("bus-agency");
                                sdGlobal.setName("JADE-bus-agency");
                                templateGlobal.addServices(sdGlobal);

                                result = DFService.search(myAgent, templateGlobal);
                                System.out.println("No bus has stop in itinerary. Sending message to all buses");
                                System.out.println("Found the following buses:");

                                targetBuses = new AID[result.length];

                                for (int i = 0; i < result.length; ++i) {
                                    targetBuses[i] = result[i].getName();
                                    System.out.println(targetBuses[i].getName());
                                }

                                System.out.println();
                            }
                        }
                        catch (FIPAException fe) {
                            fe.printStackTrace();
                        }

                        // Perform the request
                        myAgent.addBehaviour(new RequestPerformer());
                    }
                }
            } );

        } else {
            System.err.println("Incorrect number of arguments");
            System.err.println("Passenger arguments: startStop endStop timePreference(0-5)");
            doDelete();
        }
    }

    protected void takeDown() {
        if(this.instantOfEstimation!=null){
            Instant instanceArrivedAtDestination = Instant.now();
            double actualTravelTime = (double)Duration.between(this.instantOfEstimation, instanceArrivedAtDestination).toMillis()/1000;

            double timeDeviation = (actualTravelTime-this.estimatedTime)/this.estimatedTime;

            this.informStats(String.valueOf(timeDeviation), "time-deviation");
            System.out.println("ACTUAL TIME: "+actualTravelTime+" SECONDS");
            System.out.println("DEVIATION: "+timeDeviation);
        }
    }

    /**
     Inner class RequestPerformer.
     This is the behaviour used by passenger agents to send requests to bus agents
     */
    private class RequestPerformer extends Behaviour {
        private BusProposal bestProposal = null;
        private ArrayList<BusProposal> proposals = new ArrayList<>();
        private int repliesCnt = 0;
        private MessageTemplate mt;
         private MessageTemplate mtDone;
        private int step = 0;
        private double minTime = 99999;
        private double maxTime = 0;
        private double minPrice = 99999;
        private double maxPrice = 0;

        public void action() {
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID bus: targetBuses) {
                        cfp.addReceiver(bus);
                    }
                    cfp.setContent(startStop + " " + endStop);
                    cfp.setConversationId("bus-agency");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bus-agency"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    mtDone = MessageTemplate.MatchContent("ARRIVED TO DESTINATION");
                    step = 1;

                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String[] tokens = reply.getContent().split(" ");
                            double time = Double.parseDouble(tokens[0]);
                            double price = Double.parseDouble(tokens[1]);

                            proposals.add(new BusProposal(reply.getSender(), time, price));

                            if(time < this.minTime)
                                this.minTime = time;

                            if (time > this.maxTime)
                                this.maxTime = time;

                            if(price < this.minPrice)
                                this.minPrice = price;

                            if (price > this.maxPrice)
                                this.maxPrice = price;

                        }

                        repliesCnt++;
                        if (repliesCnt >= targetBuses.length) {
                            determineBestOffer();
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestProposal.getBus());
                    order.setContent(startStop + " " + endStop);
                    order.setConversationId("bus-agency");
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bus-agency"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;

                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println("Bus \" " + reply.getSender().getName() + " \" will arrive at the destination in " + bestProposal.getTime() + " seconds");
                            ((PassengerAgent)myAgent).informStats(String.valueOf(bestProposal.getTime()),"estimated-time");
                            ((PassengerAgent)myAgent).estimatedTime = bestProposal.getTime();
                            ((PassengerAgent)myAgent).instantOfEstimation = Instant.now();
                        }
                        else {
                            System.out.println("Attempt failed");
                        }

                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
                case 4:
                    reply = myAgent.receive(mtDone);
                    if (reply != null) {
                        step=5;
                        myAgent.doDelete();
                    }
                    else {
                        block();
                    }
                    break;
            }
        }

        private void determineBestOffer() {

            double bestValue = 999;

            for(BusProposal bp: this.proposals)
            {
                double timeNormalization = 0;
                double priceNormalization = 0;

                if(this.minTime < this.maxTime)
                    timeNormalization = (bp.getTime() - this.minTime) / (this.maxTime - this.minTime);

                if(this.minPrice < this.maxPrice)
                    priceNormalization = (bp.getPrice() - this.minPrice) / (this.maxPrice - this.minPrice);

                double value = alpha * timeNormalization + (1 - alpha) * priceNormalization;

                if(value < bestValue) {
                    bestProposal = bp;
                    bestValue = value;
                }
            }

        }

        public boolean done() {
            if (step == 2 && proposals.isEmpty()) {
                System.out.println("There are no buses");
            }
            return ((step == 2 && proposals.isEmpty()) || step == 5);
        }
    }

    private DFAgentDescription getTemplate(String type, String name)
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
    
    private void informStats(String content,String conversation){
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(this.statsAgent);
        message.setContent(content);
        message.setConversationId(conversation);
        message.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
        this.send(message);   
    }

}
