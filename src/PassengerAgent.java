import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class PassengerAgent extends Agent {

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            int startStop, endStop;

            startStop = Integer.parseInt((String)  args[0]);
            endStop = Integer.parseInt((String)  args[1]);

            System.out.println("Client Name: " + getLocalName());
            System.out.println("Current stop: " + startStop);
            System.out.println("Ending stop: " + endStop + "\n");

            // Add a TickerBehaviour that checks for bus
            addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick() {

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("bus-agency");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following bus:");

                        AID [] sellerAgents = new AID[result.length];

                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    }
                    catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
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
            fe.printStackTrace();
        }

        System.out.println("Passenger reached his destination");
    }
}
