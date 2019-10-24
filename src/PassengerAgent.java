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

import java.util.ArrayList;

public class PassengerAgent extends Agent {

    private AID[] targetBuses;
    private int startStop;
    private int endStop;
    private float alpha = (float) 0.5;

    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length == 2) {
            startStop = Integer.parseInt((String)  args[0]);
            endStop = Integer.parseInt((String)  args[1]);

            System.out.println("Client Name: " + getLocalName() + " Stops: " + startStop + " -> " + endStop);

            // Add a TickerBehaviour that checks for bus
            addBehaviour(new TickerBehaviour(this, 5000) {
                protected void onTick() {

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
            } );

        } else {
            // Make the agent terminate
            System.out.println("No starting stop and/or ending stop specified");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Passenger reached his destination");
    }

    /**
     Inner class RequestPerformer.
     This is the behaviour used by Book-buyer agents to request seller
     agents the target book.
     */
    private class RequestPerformer extends Behaviour {
        private BusProposal bestProposal = null; // The agent who provides the best offer
        private ArrayList<BusProposal> proposals = new ArrayList<>();
        private int repliesCnt = 0; // The counter of replies from bus agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        private float minTime = 99999;
        private float maxTime = 0;
        private float minPrice = 99999;
        private float maxPrice = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID bus: targetBuses) {
                        cfp.addReceiver(bus);
                    }
                    cfp.setContent(startStop + " " + endStop);
                    cfp.setConversationId("bus-agency");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);

                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bus-agency"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;

                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String[] tokens = ((String) reply.getContent()).split(" ");
                            float time = Float.parseFloat(tokens[0]);
                            float price = Float.parseFloat(tokens[1]);

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
                            // We received all replies
                            determineBestOffer();
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestProposal.getBus());
                    order.setContent(startStop + " " + endStop);
                    order.setConversationId("bus-agency");
                    order.setReplyWith("order"+System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bus-agency"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            System.out.println("Bus \" " + reply.getSender().getName() + " \" will arrive at the destination in " + bestProposal.getTime() + " seconds");
                            myAgent.doDelete();
                        }
                        else {
                            System.out.println("Attempt failed: requested book already sold.");
                        }

                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }

        private void determineBestOffer() {

            float bestValue = 999;

            for(BusProposal bp: this.proposals)
            {
                float timeNormalization = (bp.getTime() - this.minTime) / (this.maxTime - this.minTime);
                float priceNormalization = (bp.getPrice() - this.minPrice) / (this.maxPrice - this.minPrice);

                float value = alpha * timeNormalization + (1 - alpha) * priceNormalization;

                if(value < bestValue)
                    bestProposal = bp;
            }

        }

        public boolean done() {
            if (step == 2 && proposals.isEmpty()) {
                System.out.println("There are no buses");
            }
            return ((step == 2 && proposals.isEmpty()) || step == 4);
        }
    }  // End of inner class RequestPerformer

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

}
