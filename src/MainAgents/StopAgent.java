package MainAgents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;

public class StopAgent extends df {

    private Coordinates coords;

    protected void setup() {

        Object[] coordArgs = getArguments();

        if (coordArgs != null && coordArgs.length == 3) {
            coords = new Coordinates((int) coordArgs[1], (int) coordArgs[2]);

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
            Property coordCol = new Property();
            coordCol.setName("Col");
            coordCol.setValue(coords.getX());
            Property coordLine = new Property();
            coordLine.setName("Line");
            coordLine.setValue(coords.getY());
            sd.addProperties(coordCol);
            sd.addProperties(coordLine);
            dfd.addServices(sd);

            setDescriptionOfThisDF(dfd);

            //super.showGui();

            try {
                DFService.register(this, parentName, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            addParent(parentName, dfd);
        } else {
            System.out.println("No stop coordinates specified");
            doDelete();
        }

    }

    public Coordinates getCoords() {
        return coords;
    }


}
