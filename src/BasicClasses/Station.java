package BasicClasses;

import java.util.*;

public class Station{
    private final String name;
    private Map<Station, Double> connections = new LinkedHashMap<>();
    private boolean isAvailable = true;

    public <T> Station(T name) {
        this(name, false);
    }

    public <T> Station(T name, boolean ignoreEvent) {
        this.name = name.toString();
        if (!ignoreEvent)
            InteractiveMenu.success(this.getClass().getSimpleName() + " \"" + name + "\"", "created");
    }

    public String getName() {
        return name;
    }

    public Map<Station, Double> getConnections() {
        return connections;
    }

    public void setConnections(Map<Station, Double> map) {
        this.connections = map;
    }

    private void connect(Station station, Double distance) throws isSameStationException, HasSameStationException {
        if(!this.equals(station)){
            boolean hasSameStation = false;
            for (Station elem : this.getConnections().keySet()){
                if (station.equals(elem)) {
                    hasSameStation = true;
                    break;
                }
            }
            if(!hasSameStation) {
                connections.put(station, distance);
            }
            else
                throw new HasSameStationException();
        }
        else
            throw new isSameStationException();
    }

    public void addConnection(Station station, Double distance) throws isSameStationException, HasSameStationException {
            connect(station, distance);
            station.connect(this, distance);
    }

    public void removeConnection(Station station){
        if(RailroadMap.trainsOnTheWay.size() > 0){
            Menu.error("You cannot disconnect stations while any train is on the way!");
        }
        else {
            Map<Station, Double> buff = station.getConnections();
            buff.remove(this);
            station.setConnections(buff);
            connections.remove(station);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Station))
            return false;
        if(((Station)obj).getName().equals(this.name) && this.getConnections().size() == ((Station)obj).getConnections().size()){
            boolean hasSameConnections = true;
            for(Map.Entry<Station, Double> elem : connections.entrySet()){
                boolean isSameStation = false;
                for(Map.Entry<Station, Double> elem1 : ((Station) obj).getConnections().entrySet()){
                    if(elem.getKey().getName().equals(elem1.getKey().getName()) && elem.getValue().equals(elem1.getValue())) {
                        isSameStation = true;
                        break;
                    }
                }
                if(!isSameStation) {
                    hasSameConnections = false;
                    break;
                }
            }
            return hasSameConnections;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String shortInfo(){
        return  "\"" + this.name + "\" connections - " + connections.size();
    }

    @Override
    public String toString(){
        String str = "\"" + this.name + "\" connections - " + connections.size();
        if(connections.size() > 0) {
            str+=": " + connectionsToString();
        }
        return str;
    }

    public String connectionsToString() {
        StringBuilder str = new StringBuilder("\n" + InteractiveMenu.Color.BLACK_B);
        Iterator<Station> keyIterator = connections.keySet().iterator();
        Iterator<Double> valueIterator = connections.values().iterator();
        for(int i = 0; keyIterator.hasNext() && valueIterator.hasNext(); i++) {
            str.append("\t\t").append(i + 1).append(". \"").append(keyIterator.next().getName()).append("\", ").append(valueIterator.next()).append(" km\n");
        }
        return  str.substring(0, str.length()-1);
    }

    public boolean isConnected(Station station){
        return connections.containsKey(station);
    }

    public void showConnections() {
        System.out.println(connectionsToString());
        System.out.print(InteractiveMenu.Color.DEFAULT);
    }
    public boolean hasAnyConnection(){
        return connections.size() > 0;
    }

    public int getConnectionsCount() {
        return connections.size();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public double getConnectionDistance(Station station){
        return connections.get(station);
    }

    public class isSameStationException extends Exception{
        isSameStationException(){
            super("You cannot connect a station to itself!");
        }
    }

    public class HasSameStationException extends Exception{
        HasSameStationException(){
            super("Stations are already connected!");
        }
    }
}

