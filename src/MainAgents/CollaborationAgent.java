package MainAgents;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;

public class CollaborationAgent extends df{

    protected void setup() {

        AID parentName = getDefaultDF();

        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sdFIPA = new ServiceDescription();
        sdFIPA.setName("collaboration-sub-df");
        sdFIPA.setType("fipa-df");
        sdFIPA.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
        sdFIPA.addOntologies("fipa-agent-management");
        sdFIPA.setOwnership("JADE");
        dfd.addServices(sdFIPA);

        ServiceDescription sd = new ServiceDescription();
        sd.setName("col");
        sd.setType("collaboration");
        dfd.addServices(sd);

        setDescriptionOfThisDF(dfd);

        super.showGui();

        try {
            DFService.register(this, parentName, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addParent(parentName, dfd);
    }

}
