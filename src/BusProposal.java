import jade.core.AID;

public class BusProposal {

    private AID bus;
    private float time;
    private float price;

    BusProposal(AID bus, float time, float price)
    {
        this.bus = bus;
        this.time = time;
        this.price = price;
    }

    public AID getBus() {
        return bus;
    }

    public float getTime() {
        return time;
    }

    public float getPrice() {
        return price;
    }
}
