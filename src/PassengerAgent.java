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

public class PassengerAgent extends Agent {

    private AID[] targetBuses;
    private int startStop;
    private int endStop;

    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length == 2) {
            startStop = Integer.parseInt((String)  args[0]);
            endStop = Integer.parseInt((String)  args[1]);

            System.out.println("Client Name: " + getLocalName());
            System.out.println("Current stop: " + startStop);
            System.out.println("Ending stop: " + endStop + "\n");

            // Add a TickerBehaviour that checks for bus
            addBehaviour(new TickerBehaviour(this, 5000) {
                protected void onTick() {

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("bus-agency");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following buses:");

                        targetBuses = new AID[result.length];

                        for (int i = 0; i < result.length; ++i) {
                            targetBuses[i] = result[i].getName();
                            System.out.println(targetBuses[i].getName());
                        }

                        System.out.println("");
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
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
        }

        System.out.println("Passenger reached his destination");
    }

    /**
     Inner class RequestPerformer.
     This is the behaviour used by Book-buyer agents to request seller
     agents the target book.
     */
    private class RequestPerformer extends Behaviour {
        private AID bestBus; // The agent who provides the best offer
        private int bestTime;  // The best offered time
        private int repliesCnt = 0; // The counter of replies from bus agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < targetBuses.length; ++i) {
                        cfp.addReceiver(targetBuses[i]);
                    }
                    cfp.setContent(String.valueOf(startStop));
                    cfp.setConversationId("bus-agency");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("bus-agency"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    System.out.println("Sending message");
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            int time = Integer.parseInt(reply.getContent());
                            if (bestBus == null || time < bestTime) {
                                // This is the best offer at present
                                bestTime = time;
                                bestBus = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= targetBuses.length) {
                            // We received all replies
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
                    order.addReceiver(bestBus);
                    order.setContent(String.valueOf(startStop));
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
                            System.out.println("Bus \" " + reply.getSender().getName() + " \" will arrive in the next few minutes");
                            System.out.println("Time = " + bestTime);
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
                    System.out.println("Bus: " + bestBus);
                    System.out.println("Time: " + bestTime);
                    break;
            }
        }

        public boolean done() {
            if (step == 2 && bestBus == null) {
                System.out.println("There are no buses");
            }
            return ((step == 2 && bestBus == null) || step == 4);
        }
    }  // End of inner class RequestPerformer
}
