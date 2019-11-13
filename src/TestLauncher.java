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

        System.out.println("Run");

        int stop1 = (int) (Math.random() * 3) + 1;
        int stop2;
        System.out.println("22 " + stop1);

        do {
            stop2 = (int) (Math.random() * 3) + 1;
            System.out.println(stop2);
        } while(stop1 == stop2);
        System.out.println("27");

        try {
            AgentController ac;
            Object[] stops = {String.valueOf(stop1), String.valueOf(stop2), "50"};
            ac = mainContainer.createNewAgent("p" + i, "PassengerAgent", stops);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        System.out.println("37");

        i++;
    }
}
