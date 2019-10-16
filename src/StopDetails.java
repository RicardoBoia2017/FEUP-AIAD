public class StopDetails {

    private Coordinates coords;
    private int leavingPassengers = 0;

    StopDetails(Coordinates coords, int leavingPassengers)
    {
        this.coords = coords;
        this.leavingPassengers = leavingPassengers;
    }

    public void setLeavingPassengers(int leavingPassengers) {

        if(leavingPassengers == 1)
            this.leavingPassengers++;

        else
            this.leavingPassengers = leavingPassengers;
    }

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public int getLeavingPassengers() {
        return leavingPassengers;
    }
}
