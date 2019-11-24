
import Map.Map;
import Stats.StatsAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.io.File;
import java.io.FilenameFilter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JADELauncher {

    public static void main(String[] args) {

        Runtime rt = Runtime.instance();

        Profile p1 = new ProfileImpl();
        ContainerController mainContainer = rt.createMainContainer(p1);

        AgentController ac;

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
            File testDir = new File("tests");
            File testFile = testDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            })[0]; //For now just gets the first xml file of the test directory TODO: multiple files/tests

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

                try {
                    Object[] coords = {x, y, speed, totalSeats, price, dishonestyDegree};
                    ac = mainContainer.createNewAgent(name, "MainAgents.BusAgent", coords);
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

            nList = doc.getElementsByTagName("stop");

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

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new TestLauncher(mainContainer, alpha, limit), 0, 5, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
