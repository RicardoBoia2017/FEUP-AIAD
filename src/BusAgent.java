import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;

public class BusAgent extends Agent{

    private Coordinates coords;
    private ArrayList<Integer> itinerary = new ArrayList<>();

    protected void setup() {
        this.coords = new Coordinates(0,0); //TODO define starting point
        System.out.println("Bus started");

        // Add a TickerBehaviour that schedules a request to seller agents every minute
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                System.out.println("Checking new passengers");
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bus-agency"); //TODO define type of service
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found the following customers:");
                    AID[] customers = new AID[result.length];

                    for (int i = 0; i < result.length; ++i) {
                        customers[i] = result[i].getName();
                        System.out.println(customers[i].getName());
                    }

                    System.out.println("");
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        } );

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

}
