import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.df;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JADELauncher {

	public static void main(String[] args) {
		Runtime rt = Runtime.instance();

		Profile p1 = new ProfileImpl();
		//p1.setParameter(...);
		ContainerController mainContainer = rt.createMainContainer(p1);

		AgentController ac1;
		try {
                        Object[] stops = {"1","2"};
			ac1 = mainContainer.createNewAgent("pass1", "PassengerAgent", stops);
			ac1.start();
		} catch (StaleProxyException e) {
                    e.printStackTrace();
		}

                
                AgentController ac2;
		try {
                        Object[] stops = {1,2};
			ac2 = mainContainer.acceptNewAgent("bus1", new BusAgent());
			ac2.start();
		} catch (StaleProxyException e) {
                    e.printStackTrace();
		}
                
                AgentController ac4;
		try {
                        Object[] coords = {"",20,30};
			ac4 = mainContainer.createNewAgent("1", "StopAgent", coords);
			ac4.start();
		} catch (StaleProxyException e) {
                    e.printStackTrace();
		}
                
                AgentController ac5;
		try {
                        Object[] coords = {"",40,50};
			ac5 = mainContainer.createNewAgent("2", "StopAgent", coords);
			ac5.start();
		} catch (StaleProxyException e) {
                    e.printStackTrace();
		}

                
		AgentController ac3;
		try {
			ac3 = mainContainer.acceptNewAgent("myRMA", new jade.tools.rma.rma());
			ac3.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} 
 
               
	}

}
