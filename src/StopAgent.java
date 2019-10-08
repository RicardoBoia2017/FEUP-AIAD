
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class StopAgent extends Agent{

    private Coordinates coords;

    protected void setup(){
        Object[] args = getArguments();
        
        if (args != null && args.length == 2) {
             coords = new Coordinates((int)args[0],(int)args[1]);
        
        
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType("stop");
            dfd.addServices(sd);

            register(dfd);
        } else {
            // Make the agent terminate
            System.out.println("No stop coordinates specified");
            doDelete();
        }
    }
    
    protected void register (DFAgentDescription dfd){
        try{
            DFService.register(this,dfd);
        }catch(FIPAException fe){
            fe.printStackTrace();
        }
    }
    
    public Coordinates getCoords() {
        return coords;
    }
    
    

}
