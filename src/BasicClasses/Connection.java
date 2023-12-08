package BasicClasses;

public class Connection {
    private Station first;
    private Station second;
    private double distance;

    public Connection(Station first, Station second){
        this.first = first;
        this.second = second;
        this.distance = first.getConnectionDistance(second);
    }

    public void shift(Station station){
        this.first = this.second;
        this.second = station;
        this.distance = first.getConnectionDistance(this.second);
    }

    public Station first(){
        return first;
    }

    public Station second(){
        return second;
    }

    public double distance(){
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Connection))
            return false;

        return ((Connection) obj).first.equals(this.first) && ((Connection) obj).second.equals(this.second);
    }

    @Override
    public int hashCode() {
        return (this.first.getName() + this.second.getName()).hashCode() ;
    }

    @Override
    public String toString() {
        return Menu.Text.highlightedPattern( "/P/\""+first.getName()+ "\"://G/ - ://P/\"" + second.getName()+"\":/");
    }
}
