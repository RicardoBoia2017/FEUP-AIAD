package MainAgents;

import jade.core.AID;

public class BusProposal {

    private AID bus;
    private double time;
    private double price;

    BusProposal(AID bus, double time, double price) {
        this.bus = bus;
        this.time = time;
        this.price = price;
    }

    public AID getBus() {
        return bus;
    }

    public double getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }
}
