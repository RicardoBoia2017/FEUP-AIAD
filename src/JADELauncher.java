import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JADELauncher {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);

        AgentController ac ;
        try {
            Object[] stops = {"1", "2", "50"};
            ac = mainContainer.createNewAgent("pass1", "PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"15", "10", "1","40","10","5"};
            ac = mainContainer.createNewAgent("bus1", "BusAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"10", "15", "1","30","10","0"};
            ac = mainContainer.createNewAgent("bus2", "BusAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        try {
            ac = mainContainer.acceptNewAgent("map", new Map());
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        try {
            ac = mainContainer.acceptNewAgent("stats", new StatsAgent());
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        /*
        try {
            Object[] stops = {"1", "3"};
            ac = mainContainer.createNewAgent("pass2", "PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }*/
        
        
        try {
            Object[] coords = {"", 10, 10};
            ac = mainContainer.createNewAgent("stop1", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        
        try {
            Object[] coords = {"", 10, 15};
            ac = mainContainer.createNewAgent("stop2", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 40, 20};
            ac = mainContainer.createNewAgent("stop3", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            ac = mainContainer.acceptNewAgent("myRMA", new jade.tools.rma.rma());
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }


    }

}
