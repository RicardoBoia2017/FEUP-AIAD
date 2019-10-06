import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class BusAgent extends Agent{

    private Coordinates coords;
    private ArrayList<Integer> itinerary = new ArrayList<>();

    protected void setup() {
        this.coords = new Coordinates(0,0); //TODO define starting point
        System.out.println("Bus started");

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

                Integer price = (int)(Math.random() * 30); //Time or price of the trip

                if (price != null) {
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

}
