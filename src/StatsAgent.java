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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StatsAgent extends Agent{
    public static long REFRESH_RATE = 20;
    private double averageOcupancyRate;
    private double averageEstimatedTime = -1;
    
    protected void setup() {
        
        DFAgentDescription template = new DFAgentDescription();
        template.setName(this.getAID());
        ServiceDescription service = new ServiceDescription();
        service.setType("stats");
        service.setName("stats1");
        template.addServices(service);

        try {
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        addBehaviour(new PassangerStatsServer(this));
        
         addBehaviour(new TickerBehaviour(this, REFRESH_RATE) {
                StatsAgent currentAgent = (StatsAgent) myAgent;

                protected void onTick() {
                    DFAgentDescription busTemplate = (DFAgentDescription) BusAgent.getTemplate("bus-agency","JADE-bus-agency");
                    //DFAgentDescription stopTemplate = BusAgent.getTemplate("stop",null);
                    
                    try {      
                        DFAgentDescription[] allBuses = DFService.search(myAgent, busTemplate);
                        
                        double sum=0;
                        double total=0;
                        
                        for(DFAgentDescription bus: allBuses) {
                           sum+=StatsAgent.getBusOccupancyRate(bus);
                           total++;
                        }
                                            
                        currentAgent.averageOcupancyRate = sum/total;
                    } catch (FIPAException ex) {
                        Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    System.out.println("OCCUPANCY: "+currentAgent.averageOcupancyRate);
                    System.out.println("AVERAGE ESTIMATED TIME: "+currentAgent.averageEstimatedTime);
                }
                
         });
    }
    
    
    private class PassangerStatsServer extends CyclicBehaviour {

        StatsAgent currentStats;

        private PassangerStatsServer(StatsAgent currentStats) {
            this.currentStats = currentStats;
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
               if(msg.getConversationId() == "estimated-time"){
                 double estimatedTime = Double.parseDouble(msg.getContent());
                if(currentStats.averageEstimatedTime==-1)
                    currentStats.averageEstimatedTime=estimatedTime;
                else
                    currentStats.averageEstimatedTime=(currentStats.averageEstimatedTime+estimatedTime)/2;
               }
            }
            else {
                block();
            }
        }

    }  // End of inner class OfferRequestsServer

    
    static double getBusOccupancyRate(DFAgentDescription busAgent)
    {
        Iterator serviceIterator = busAgent.getAllServices();

        ServiceDescription serviceAgent = (ServiceDescription) serviceIterator.next();
        
        //information service is always the last one
        if(serviceIterator.hasNext())
            serviceAgent = (ServiceDescription)serviceIterator.next();

        Iterator propertyIterator = serviceAgent.getAllProperties();
        
        //skip coordinates properties
        propertyIterator.next();
        propertyIterator.next();
        
        //bus is not forced to have occupancy ***TO SEE AGAIN
        if(propertyIterator.hasNext())
          return Double.parseDouble((String)((Property)propertyIterator.next()).getValue());
        
        return (double)0.0;
    }

}
