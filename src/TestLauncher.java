import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class TestLauncher implements Runnable {

    private ContainerController mainContainer;
    private int i = 0;
    private int alpha;
    private int limit;

    TestLauncher(ContainerController mainContainer,int alpha,int limit)
    {
        this.mainContainer = mainContainer;
        this.alpha=alpha;
        this.limit=limit;
    }

    @Override
    public void run() {

        if(i >= limit)
            return;

        int stop1 = (int) (Math.random() * 9) + 1;

        int stop2;

        do {
            stop2 = (int) (Math.random() * 9) + 1;
        } while(stop1 == stop2);

        try {
            AgentController ac;
            Object[] stops = {String.valueOf(stop1), String.valueOf(stop2), String.valueOf(alpha)};
            ac = mainContainer.createNewAgent("p" + i, "MainAgents.PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        i++;
    }
}
