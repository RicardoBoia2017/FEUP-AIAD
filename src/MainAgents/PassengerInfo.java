package MainAgents;

import jade.lang.acl.ACLMessage;

public class PassengerInfo {

    private ACLMessage reply;
    private int busDistance;
    private double busTime;
    private double busPrice;
    private int responsesLeft;

    PassengerInfo(ACLMessage reply, int busDistance, double busTime, double busPrice, int responsesLeft)
    {
        this.reply = reply;
        this.busDistance = busDistance;
        this.busTime = busTime;
        this.busPrice = busPrice;
        this.responsesLeft = responsesLeft;
    }

    public ACLMessage getReply() {
        return reply;
    }

    public int getBusDistance() {
        return busDistance;
    }

    public double getBusTime() {
        return busTime;
    }

    public double getBusPrice() {
        return busPrice;
    }

    public int getResponsesLeft() {
        return responsesLeft;
    }

    public void decrementResponsesLeft()
    {
        responsesLeft--;
    }
}
