
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;

public class StopAgent extends df{

    private Coordinates coords;

    protected void setup(){

        Object[] coordArgs = getArguments();
        
        //int len = 0;
        //byte[] buffer = new byte[1024];
        
        if (coordArgs != null && coordArgs.length == 3) {
             coords = new Coordinates((int)coordArgs[1],(int)coordArgs[2]);
        
            //registers stop DF in the main DF
            AID parentName = getDefaultDF();
            
            super.setup();
             
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sdFIPA = new ServiceDescription();
            sdFIPA.setName(getLocalName() + "-sub-df");
            sdFIPA.setType("fipa-df");
            sdFIPA.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
            sdFIPA.addOntologies("fipa-agent-management");
            sdFIPA.setOwnership("JADE");
            dfd.addServices(sdFIPA);
            
            ServiceDescription sd = new ServiceDescription();
            sd.setName(getLocalName());
            sd.setType("stop");
            dfd.addServices(sd);
           
            setDescriptionOfThisDF(dfd);
            
            //shows DF list of subscribed agents
            super.showGui();
            
            try {
                DFService.register(this, parentName,dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            
            addParent(parentName, dfd);
        } else {
            // Make the agent terminate
            System.out.println("No stop coordinates specified");
            doDelete();
        }

    }

    
    /*protected void register (DFAgentDescription dfd){
        try{
            DFService.register(this,dfd);
        }catch(FIPAException fe){
            fe.printStackTrace();
        }
    }*/
    
    public Coordinates getCoords() {
        return coords;
    }
    
    

}
