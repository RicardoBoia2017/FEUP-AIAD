package MainAgents;

import jade.core.AID;
import java.util.ArrayList;

public class StopDetails {

    private Coordinates coords;
    private ArrayList<AID> leavingPassengers;

    StopDetails(Coordinates coords)
    {
        this.leavingPassengers = new ArrayList();
        this.coords = coords;
    }

    public void setLeavingPassenger(AID leavingPassenger) {
        this.leavingPassengers.add(leavingPassenger);
    }

    public Coordinates getCoords() {
        return coords;
    }

    public ArrayList<AID> getLeavingPassengers() {
        return leavingPassengers;
    }

    
}
