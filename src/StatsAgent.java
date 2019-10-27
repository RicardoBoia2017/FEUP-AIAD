
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StatsAgent extends Agent{
    public static long REFRESH_RATE = 20;
    private double averageOcupancyRate;
    
    
    protected void setup() {
         addBehaviour(new TickerBehaviour(this, REFRESH_RATE) {
                StatsAgent currentAgent = (StatsAgent) myAgent;

                protected void onTick() {
                    DFAgentDescription busTemplate = BusAgent.getTemplate("bus-agency","JADE-bus-agency");
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
                }
                
         });
    }
    
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
