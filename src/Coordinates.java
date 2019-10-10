public class Coordinates {

    private int x;
    private int y;

    public Coordinates() {
    }

    
    
    Coordinates(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX() {return this.x;}

    public int getY() {return this.y;}

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Coordinates))
            return false;
        Coordinates other = (Coordinates)o;
        return this.x == other.x && this.y == other.y;
    }
}
