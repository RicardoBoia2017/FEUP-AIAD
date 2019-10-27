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

        AgentController ac1;
        try {
            Object[] stops = {"1", "2", "50"};
            ac1 = mainContainer.createNewAgent("pass1", "PassengerAgent", stops);
            ac1.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }


        AgentController ac2;
        try {
            Object[] coords = {"15", "10", "1","40","10","5"};
            ac2 = mainContainer.createNewAgent("bus1", "BusAgent", coords);
            ac2.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac6;
        try {
            Object[] coords = {"10", "15", "1","30","10","0"};
            ac6 = mainContainer.createNewAgent("bus2", "BusAgent", coords);
            ac6.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        AgentController ac8;
        try {
            ac8 = mainContainer.acceptNewAgent("map", new Map());
            ac8.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        AgentController ac9;
        try {
            ac9 = mainContainer.acceptNewAgent("stats", new StatsAgent());
            ac9.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        
        /*AgentController ac9;
        try {
            Object[] stops = {"1", "3"};
            ac9 = mainContainer.createNewAgent("pass2", "PassengerAgent", stops);
            ac9.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }*/
        
        AgentController ac4;
        try {
            Object[] coords = {"", 10, 10};
            ac4 = mainContainer.createNewAgent("stop1", "StopAgent", coords);
            ac4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac5;
        try {
            Object[] coords = {"", 10, 15};
            ac5 = mainContainer.createNewAgent("stop2", "StopAgent", coords);
            ac5.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        AgentController ac7;
        try {
            Object[] coords = {"", 40, 20};
            ac7 = mainContainer.createNewAgent("stop3", "StopAgent", coords);
            ac7.start();
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
