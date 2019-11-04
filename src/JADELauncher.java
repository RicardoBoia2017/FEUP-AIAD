import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JADELauncher {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);

        AgentController ac;
        /*try {
            Object[] stops = {"1", "2", "50"};
            ac = mainContainer.createNewAgent("pass1", "PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] stops = {"4", "5", "100"};
            ac = mainContainer.createNewAgent("pass2", "PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }*/

        try {
            Object[] coords = {"20", "10", "2","40","10","5"};
            ac = mainContainer.createNewAgent("bus1", "BusAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"10", "15", "2","30","10","5"};
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
            Object[] coords = {"", 30, 20};
            ac = mainContainer.createNewAgent("stop3", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 10, 20};
            ac = mainContainer.createNewAgent("stop4", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 40, 10};
            ac = mainContainer.createNewAgent("stop5", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 30, 5};
            ac = mainContainer.createNewAgent("stop6", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 0, 0};
            ac = mainContainer.createNewAgent("stop7", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 40, 20};
            ac = mainContainer.createNewAgent("stop8", "StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 20, 25};
            ac = mainContainer.createNewAgent("stop9", "StopAgent", coords);
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

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new TestLauncher(mainContainer), 0, 10, TimeUnit.SECONDS);

    }

}
