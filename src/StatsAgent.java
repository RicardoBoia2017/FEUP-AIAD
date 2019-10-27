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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StatsAgent extends Agent{
    public static long REFRESH_RATE = 20;
    private double averageOcupancyRate;
    private double maxAverageOccupancyRate = -1;
    private double averageEstimatedTime = -1;
    private double averageTimeDeviation = -1;
    private double totalGain = 0;
    private HashMap<String,Double> allBusesGain;
    private StatsGUI myGUI;

    public StatsAgent() {
        this.allBusesGain = new HashMap();
    }
    
    protected void setup() {
        
        DFAgentDescription template = new DFAgentDescription();
        template.setName(this.getAID());
        ServiceDescription service = new ServiceDescription();
        service.setType("stats");
        service.setName("stats1");
        template.addServices(service);
        
        myGUI = new StatsGUI(this);
        myGUI.setVisible(true);

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
                        
                        double sumOccupancy=0;
                        double sumGain=0;
                        double total=0;
                        
                        currentAgent.allBusesGain.clear();
                        
                        for(DFAgentDescription bus: allBuses) {
                           sumOccupancy+=StatsAgent.getBusOccupancyRate(bus);
                           sumGain+=StatsAgent.getBusGain(bus);
                           total++;
                           currentAgent.allBusesGain.put(bus.getName().getLocalName(), StatsAgent.getBusGain(bus));
                        }
                                            
                        currentAgent.averageOcupancyRate = sumOccupancy/total;
                        if(currentAgent.averageOcupancyRate>currentAgent.maxAverageOccupancyRate)
                            currentAgent.maxAverageOccupancyRate=currentAgent.averageOcupancyRate;                      
                        currentAgent.totalGain = sumGain;
                    } catch (FIPAException ex) {
                        Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    currentAgent.myGUI.updateInfo();
                    currentAgent.updateFileInfo();
                    
                    System.out.println("OCCUPANCY: "+currentAgent.averageOcupancyRate);
                    System.out.println("AVERAGE ESTIMATED TIME: "+currentAgent.averageEstimatedTime);
                    System.out.println("AVERAGE TIME DEVIATION: "+currentAgent.averageTimeDeviation);
                    System.out.println("GAIN: "+currentAgent.totalGain);
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
               }else if(msg.getConversationId() == "time-deviation"){
                    double deviation = Double.parseDouble(msg.getContent());
                    if(currentStats.averageTimeDeviation==-1)
                        currentStats.averageTimeDeviation=deviation;
                    else
                        currentStats.averageTimeDeviation=(currentStats.averageTimeDeviation+deviation)/2;
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
    
    static double getBusGain(DFAgentDescription busAgent)
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
            propertyIterator.next();
        
        //bus is not forced to have gain ***TO SEE AGAIN
        if(propertyIterator.hasNext())
          return Double.parseDouble((String)((Property)propertyIterator.next()).getValue());
        
        return (double)0.0;
    }
    
    public void updateFileInfo(){
        try{
            FileWriter fileWriter = new FileWriter("statsFile.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.printf("Simulation Statistics File\n\n");
            printWriter.printf("Average Estimated Waiting Time: %.2f seconds \n", this.averageEstimatedTime);
            printWriter.printf("Average Time Deviation: %.3f%% \n", this.averageTimeDeviation);
            printWriter.printf("Current Average Bus Occupancy Rate: %.3f%% \n", this.averageOcupancyRate);
            printWriter.printf("Maximum Average Bus Occupancy Rate: %.3f%% \n", this.maxAverageOccupancyRate);
            printWriter.printf("Total financial gain: %s€ \n", this.totalGain);
            printWriter.printf("Financial gain per bus:\n");

            for(Entry<String,Double> curBus : this.allBusesGain.entrySet()){
                printWriter.printf("%s: %.2f€\n", curBus.getKey(),curBus.getValue());
            }

            printWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public double getAverageEstimatedTime() {
        return averageEstimatedTime;
    }

    public double getAverageOcupancyRate() {
        return averageOcupancyRate;
    }

    public double getAverageTimeDeviation() {
        return averageTimeDeviation;
    }

    public double getTotalGain() {
        return totalGain;
    }

    public double getMaxAverageOccupancyRate() {
        return maxAverageOccupancyRate;
    }

    public HashMap<String, Double> getAllBusesGain() {
        return allBusesGain;
    }
    
    
    

    
}
