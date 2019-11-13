import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Map extends Agent{
        public static final int REFRESH_RATE = 10;
    
        private java.util.Map<String,Coordinates> busList = new LinkedHashMap<>();
        private java.util.Map<String,Coordinates> stopList = new LinkedHashMap<>();

    //TODO: periodically pools the DF for the data
        protected void setup() {

            MapGUI myGUI = new MapGUI(this);
            myGUI.setVisible(true);
            
            addBehaviour(new TickerBehaviour(this, REFRESH_RATE) {
                  protected void onTick() {
                    //Periodically collects all stops and buses location
                    DFAgentDescription busTemplate = BusAgent.getTemplate("bus-agency","JADE-bus-agency");
                    DFAgentDescription stopTemplate = BusAgent.getTemplate("stop",null);
                    try {      
                        DFAgentDescription[] allBuses = DFService.search(myAgent, busTemplate);
                        DFAgentDescription[] allStops = DFService.search(myAgent, stopTemplate);
                        
                        for(DFAgentDescription bus : allBuses){
                            busList.put(bus.getName().getLocalName(), Map.getAgentCoordinates(bus));
                        }
                        
                        for(DFAgentDescription stop : allStops){
                            stopList.put(stop.getName().getLocalName(), Map.getAgentCoordinates(stop));
                        }
                    } catch (FIPAException ex) {
                        Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    
                  }
              });
            
        }

    static private Coordinates getAgentCoordinates(DFAgentDescription agent)
    {
        Iterator serviceIterator = agent.getAllServices();

        ServiceDescription serviceAgent = (ServiceDescription) serviceIterator.next();
        
        if(serviceIterator.hasNext())
            serviceAgent = (ServiceDescription)serviceIterator.next();

        Iterator propertyIterator = serviceAgent.getAllProperties();
        Coordinates agentCoords = new Coordinates();
        agentCoords.setX(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));
        agentCoords.setY(Integer.parseInt((String)(((Property)propertyIterator.next()).getValue())));

        return agentCoords;
    }

    public java.util.Map<String, Coordinates> getBusList() {
        return busList;
    }

    public java.util.Map<String, Coordinates> getStopList() {
        return stopList;
    }
    
    
}