import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class TestLauncher implements Runnable {

    private ContainerController mainContainer;
    private int i = 0;

    TestLauncher(ContainerController mainContainer)
    {
        this.mainContainer = mainContainer;
    }

    @Override
    public void run() {

        int stop1 = (int) (Math.random() * 9) + 1;

        int stop2;

        do {
            stop2 = (int) (Math.random() * 9) + 1;
        } while(stop1 == stop2);

        try {
            AgentController ac;
            Object[] stops = {String.valueOf(stop1), String.valueOf(stop2), "50"};
            ac = mainContainer.createNewAgent("p" + i, "MainAgents.PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        i++;
    }
}
