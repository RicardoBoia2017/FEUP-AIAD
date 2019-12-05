package MainAgents;

import jade.core.AID;

import java.util.ArrayList;

public class StopDetails {

    private Coordinates coords;
    private ArrayList<AID> leavingPassengers;
    private String name;

    StopDetails(String name, Coordinates coords) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public static StopDetails getFirstStopByName(String stopName, ArrayList<StopDetails> stopList) {
        for (StopDetails currStop : stopList) {
            if (currStop.getName().equals(stopName)) {
                return currStop;
            }
        }
        return null;
    }

    public static StopDetails getLastStopByName(String stopName, ArrayList<StopDetails> stopList) {
        StopDetails ret = null;
        for (StopDetails currStop : stopList) {
            if (currStop.getName().equals(stopName)) {
                ret = currStop;
            }
        }
        return ret;
    }

    public static Boolean checkIfStartEndInOrder(String startName, String endName, ArrayList<StopDetails> stopList) {
        Boolean isStartPresent = false;
        for (StopDetails currStop : stopList) {
            if (currStop.getName().equals(startName)) {
                isStartPresent = true;
            } else if (currStop.getName().equals(endName)) {
                if (isStartPresent) {
                    return true;
                }
            }
        }
        return false;
    }

}
