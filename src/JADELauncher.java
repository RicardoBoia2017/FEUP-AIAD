import Map.Map;
import Stats.StatsAgent;
import jade.core.*;
import jade.core.Runtime;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JADELauncher {

    public static void main(String[] args) {

        try {
            File testDir = new File("tests");
            File[] testFiles = testDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            }); //For now just gets the first xml file of the test directory TODO: multiple files/tests

            for (File testFile : testFiles) {
                runTest(testFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runTest(File testFile) {
        System.out.println("Started " + getTestName(testFile));

        try {
            Runtime rt = Runtime.instance();

            Profile p1 = new ProfileImpl();
            ContainerController mainContainer = rt.createMainContainer(p1);

            AgentController ac = null;

            StatsAgent statsAgent = new StatsAgent(getTestName(testFile));
            Map map = new Map();

            try {
                ac = mainContainer.acceptNewAgent("map", map);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

            try {
                ac = mainContainer.acceptNewAgent("stats", statsAgent);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(testFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("bus");
            Element elem;

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                elem = (Element) nNode;

                String name = elem.getElementsByTagName("name").item(0).getTextContent();
                String x = ((Element) elem.getElementsByTagName("coordinates").item(0)).getAttribute("x");
                String y = ((Element) elem.getElementsByTagName("coordinates").item(0)).getAttribute("y");
                String speed = elem.getElementsByTagName("speed").item(0).getTextContent();
                String totalSeats = elem.getElementsByTagName("totalSeats").item(0).getTextContent();
                String price = elem.getElementsByTagName("price").item(0).getTextContent();
                String dishonestyDegree = elem.getElementsByTagName("dishonestyDegree").item(0).getTextContent();
                String priceFlexibility = elem.getElementsByTagName("priceFlexibility").item(0).getTextContent();

                try {
                    Object[] coords = {x, y, speed, totalSeats, price, dishonestyDegree, priceFlexibility};
                    ac = mainContainer.createNewAgent(name, "MainAgents.BusAgent", coords);
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

            nList = doc.getElementsByTagName("stop");
            int stopNumber = nList.getLength();

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                elem = (Element) nNode;

                String name = elem.getElementsByTagName("name").item(0).getTextContent();
                int x = Integer.parseInt(((Element) elem.getElementsByTagName("coordinates").item(0)).getAttribute("x"));
                int y = Integer.parseInt(((Element) elem.getElementsByTagName("coordinates").item(0)).getAttribute("y"));

                try {
                    Object[] coords = {"", x, y};
                    ac = mainContainer.createNewAgent(name, "MainAgents.StopAgent", coords);
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

            try {
                ac = mainContainer.acceptNewAgent("myRMA", new jade.tools.rma.rma());
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

            elem = (Element) doc.getElementsByTagName("passangers").item(0);
            int alpha = Integer.parseInt(elem.getAttribute("alpha"));
            int limit = Integer.parseInt(elem.getAttribute("limit"));
            int interval = Integer.parseInt(elem.getAttribute("interval"));

            //TestLauncher passLauncher = new TestLauncher(mainContainer, alpha, limit);

            //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            //executor.scheduleAtFixedRate(passLauncher, 0, 5, TimeUnit.SECONDS);

            spawnPassengers(mainContainer, alpha, limit, interval, stopNumber);

            while (statsAgent.getTotalNumberOfPassengers() < limit) {
                Thread.sleep(2000);
            }

            //killAllAgents(mainContainer, limit);

            mainContainer.kill();
            statsAgent.getMyGUI().dispose();
            map.getMyGUI().dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Ended " + getTestName(testFile));
    }


    private static void spawnPassengers(ContainerController mainContainer, int alpha, int limit, int interval, int stopNumber) {
        Random rand = new Random();
        for (int i = 0; i < limit; i++) {
            int stop1 = rand.nextInt(stopNumber) + 1;

            int stop2;
            do {
                stop2 = rand.nextInt(stopNumber) + 1;
            } while (stop1 == stop2);

            try {
                AgentController ac;
                Object[] stops = {String.valueOf(stop1), String.valueOf(stop2), String.valueOf(alpha)};
                ac = mainContainer.createNewAgent("p" + i, "MainAgents.PassengerAgent", stops);
                ac.start();
                Thread.sleep(interval);
            } catch (StaleProxyException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void killAllAgents(ContainerController mainContainer, int limit) {
        AMSAgentDescription[] agents;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(new Long(1));
            agents = AMSService.search(new Agent(), new AMSAgentDescription());

            for (int i = 0; i < agents.length; i++) {
                AID agentID = agents[i].getName();
                mainContainer.getAgent(String.valueOf(agentID)).kill();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTestName(File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }
}
