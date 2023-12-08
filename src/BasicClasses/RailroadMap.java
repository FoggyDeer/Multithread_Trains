package BasicClasses;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public abstract class RailroadMap extends Menu{
    public static Map<String, Station> stations = new LinkedHashMap<>();
    public static final Set<Connection> busyConnections = new HashSet<>();
    private static boolean isGenerated = false;
    public static final ArrayList<Locomotive> trainsOnTheWay = new ArrayList<>();

    public static void addStation(Station station){
        stations.put(station.getName(), station);
    }

    public static Locomotive addTrain(Locomotive locomotive){
        trainsOnTheWay.add(locomotive);
        return locomotive;
    }

    public static void showAllStations(boolean simplified){
        if(RailroadMap.stations.size() !=0) {
            System.out.println();
            System.out.println(Color.BLACK_B +"Stations: ");
            int i = 0;
            for (Station station : RailroadMap.stations.values()) {
                if(station.isAvailable())
                    System.out.println((1 + i++) + ". " + ((simplified)?station.shortInfo():station));
            }
            System.out.print(Color.DEFAULT);
        } else {
            error("No stations created!");
        }
    }

    public static void connectAll() {
        double random_distance;
        for(Station station : stations.values()) {
            for(int i = 0; i < 5; i++){
                random_distance = getRandomDistance();
                try {
                    station.addConnection(getRandomStation(), random_distance);
                }catch (Station.isSameStationException | Station.HasSameStationException e){
                }
            }
        }

        List<Station> stations_list = stations.values().stream().toList();
        for(int i = 1; i < stations.size(); i++){
            try {
                stations_list.get(i).addConnection(stations_list.get(i-1),getRandomDistance());
            }catch (Exception ignored){
            }
        }
    }

    public static ArrayList<Station> getPath(Station start, Station destination){
        ArrayList<Station> path = new ArrayList<>();
        path.add(start);
        return findPath(start, destination, path);
    }

    public static ArrayList<Station> getPath(String start, String destination){
        ArrayList<Station> path = new ArrayList<>();
        path.add(stations.get(start));
        return findPath(stations.get(start), stations.get(destination), path);
    }

    private static ArrayList<Station> findPath(Station start, Station destination, ArrayList<Station> currentPath){
        //Checking if already have connected
        for(Station station : start.getConnections().keySet()){
            if(station.equals(destination)){
                currentPath.add(station);
                return currentPath;
            }
        }

        for (Map.Entry<Station, Double> entry : start.getConnections().entrySet()) {
            if (entry.getKey().equals(destination)) {
                currentPath.add(entry.getKey());
                return currentPath;
            } else if (!entry.getKey().equals(currentPath.get(0)) && !currentPath.contains(entry.getKey()) && entry.getKey().hasAnyConnection()) {
                currentPath.add(entry.getKey());

                if((currentPath = findPath(entry.getKey(), destination, currentPath)).get(currentPath.size()-1).equals(destination))
                    return currentPath;
            }
        }

        currentPath.remove(currentPath.size() - 1);
        return currentPath;
    }

    public static void printRailroad(){
        if(stations.size() == 0){
            error("Railroad not created!");
        }
        else {
            showAllStations(false);
        }
    }

    public static void showPathStations(ArrayList<Station> stations){
        System.out.print(Color.BLACK_B);
        System.out.println("◉ \""+stations.get(0).getName()+"\"");
        for(int i = 1; i < stations.size()-1; i++){
            System.out.println("◎ \""+stations.get(i).getName()+"\"");
        }
        System.out.println("◉ \""+stations.get(stations.size()-1).getName()+"\"");
        System.out.println(Color.DEFAULT);
    }

    public static double getRandomDistance(){
        //kilometers
        return Math.random() * 3 + 0.5;
    }

    public static Station getRandomStation(){
        List<Station> stations_list = stations.values().stream().toList();
        return stations_list.get((int)(Math.random()*stations_list.size()));
    }

    public static double getPathDistance(ArrayList<Station> stations){
        double distance = 0;
        for(int i = 1; i < stations.size(); i++){
            distance += stations.get(i-1).getConnectionDistance(stations.get(i));
        }
        return roundDouble(distance, 2);
    }

    public static void startRailroadTracking(){
        Thread thread = new Thread(()->{
            while (Menu.isRunning()){
                for(Locomotive locomotive : trainsOnTheWay){
                    locomotive.showTravelData();
                    System.out.println("-------------------------------------");
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        Menu.addMenuThread(thread);
        thread.start();
    }

    public static boolean generate() {
        if (!isGenerated) {
            isGenerated = true;
            try {
                File file = new File("src\\StationsNames.txt");
                Scanner fileScanner = new Scanner(file);
                while (fileScanner.hasNext()) {
                    RailroadMap.addStation(new Station(fileScanner.nextLine(), true));
                }
                connectAll();
                fileScanner.close();

                return true;
            } catch (FileNotFoundException e) {
                error("File with stations names not found!");
            }
        }
        return false;
    }

    public static void generate(String[] names){
        for (String name : names) {
            RailroadMap.addStation(new Station(name, true));
        }
        connectAll();
    }

    public static boolean isGenerated() {
        return isGenerated;
    }
}