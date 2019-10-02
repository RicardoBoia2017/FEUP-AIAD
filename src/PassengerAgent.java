import jade.core.Agent;
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

            // Register the client in the yellow pages
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("bus-agency"); //TODO define type of service
            sd.setName("JADE-bus-agency");
            dfd.addServices(sd);
            try {
                DFService.register(this, dfd);
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

        } else {
            // Make the agent terminate
            System.out.println("No starting stop and/or ending stop specified");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Passenger reached his destination");
    }
}
