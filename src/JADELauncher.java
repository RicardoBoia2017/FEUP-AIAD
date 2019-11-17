import Map.Map;
import Stats.StatsAgent;
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
            ac = mainContainer.createNewAgent("pass1", "MainMainAgents.PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] stops = {"4", "5", "100"};
            ac = mainContainer.createNewAgent("pass2", "MainMainAgents.PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }*/

        try {
            Object[] coords = {"20", "10", "4","40","50","0"};
            ac = mainContainer.createNewAgent("bus1", "MainAgents.BusAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"10", "15", "2","40","50","0"};
            ac = mainContainer.createNewAgent("bus2", "MainAgents.BusAgent", coords);
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
            ac = mainContainer.createNewAgent("stop1", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        
        try {
            Object[] coords = {"", 10, 15};
            ac = mainContainer.createNewAgent("stop2", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 30, 20};
            ac = mainContainer.createNewAgent("stop3", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 10, 20};
            ac = mainContainer.createNewAgent("stop4", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 40, 10};
            ac = mainContainer.createNewAgent("stop5", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 30, 5};
            ac = mainContainer.createNewAgent("stop6", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 0, 0};
            ac = mainContainer.createNewAgent("stop7", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 40, 20};
            ac = mainContainer.createNewAgent("stop8", "MainAgents.StopAgent", coords);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        try {
            Object[] coords = {"", 20, 25};
            ac = mainContainer.createNewAgent("stop9", "MainAgents.StopAgent", coords);
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
        executor.scheduleAtFixedRate(new TestLauncher(mainContainer), 0, 5, TimeUnit.SECONDS);

    }

}
