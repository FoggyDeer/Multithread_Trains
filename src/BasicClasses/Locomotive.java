package BasicClasses;

import Cars.*;

import java.util.ArrayList;
import java.util.regex.Matcher;

import static BasicClasses.Menu.*;

public class Locomotive{
    private final String name;
    private final int id;
    private static int count;

    //Cars ---------------------------------
    private final StaticArrayList<Car> cars;

    private final int max_train_cars_count;
    private double max_pull_weight;
    private final int max_electric_cars_count;

    //Stations ----------------------------------
    private boolean is_on_the_way = false;

    private Station home_station;
    private Station destination_station;
    private Connection current_connection;
    private double traveled_middle_distance = 0;
    private double traveled_path = 0;
    private boolean isSlowingDown = false ;

    private double speed;

    private ArrayList<Station> path = new ArrayList<>();
    private final Object isOnStationMonitor = new Object();
    private int stations_passed_count = 0;
    private double time = 0;
    private double path_distance = 0;
    private Status status = Status.Waiting;

    public enum Status{
        Waiting("Waiting"),
        Running("Running");

        String status;
        Status(String str){
            this.status = str;
        }
    }

    public Locomotive(String name, double max_pull_weight, int max_train_cars_count, int max_electric_cars_count, double start_speed) {
        this(name, max_pull_weight, max_train_cars_count, max_electric_cars_count, start_speed, false);
    }

    public Locomotive(String name, double max_pull_weight, int max_train_cars_count, int max_electric_cars_count, double start_speed, boolean ignoreEvent) {
        this.name = name;
        this.max_pull_weight = max_pull_weight*1000;
        this.max_train_cars_count = max_train_cars_count;
        this.max_electric_cars_count = max_electric_cars_count;
        this.speed = start_speed;
        this.id = ++count;
        this.cars = new StaticArrayList<>(max_train_cars_count);
        if(!ignoreEvent)
            InteractiveMenu.success("Locomotive \"" + this.name + '"', "created");
    }

    public Locomotive(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)));
    }

    public void addCar(Car car) throws Exceptions.OutOfMaxTrainCars, Exceptions.OutOfMaxElectricTrainCars, Exceptions.OutOfMaxPullWeight, Car.Exceptions.IsAlreadyAttachedException {
        if(getCurrentCarsCount() >= max_train_cars_count){
            throw new Exceptions.OutOfMaxTrainCars(max_train_cars_count);
        }
        else if(car.isElectricCar() && getCurrentElectricCarsCount() >= max_electric_cars_count){
            throw new Exceptions.OutOfMaxElectricTrainCars(max_electric_cars_count);
        }
        else if(getCurrentPullWeight() + car.getGrossWeight() > max_pull_weight) {
            throw new Exceptions.OutOfMaxPullWeight(max_pull_weight);
        }
        else {
            cars.add(car);
            car.attachToTrain(this);
            sortCars();
        }
    }

    public void detachCar(Car car) throws Exceptions.DoesNotContainsAnyCarException, Exceptions.DoesNotContainsSuchCarException, Car.Exceptions.IsNotAttachedException {
        if(getCurrentCarsCount() == 0) {
            throw new Exceptions.DoesNotContainsAnyCarException();
        } else {
            int index = cars.indexOf(car);
            if (index >= 0) {
                cars.get(index).detachFromTrain();
                cars.remove(index);
            }
            else
                throw new Exceptions.DoesNotContainsSuchCarException();
        }
    }

    public void detachAllCars() throws Exceptions.DoesNotContainsAnyCarException, Exceptions.DoesNotContainsSuchCarException, Car.Exceptions.IsNotAttachedException {
        while (cars.size() > 0){
            detachCar(cars.get(0));
        }
    }

    public void showAllCars() {
        if(getCurrentCarsCount() == 0){
            System.out.println("No cars added!");
        }
        else {
            System.out.println("\tCars: ");
            for (int i = 0; i < getCurrentCarsCount(); i++) {
                System.out.println("\t\t" + (1+i) + ". " + cars.get(i));
            }
        }
    }

    public void showAllCars(boolean show_cargo) {
        if(getCurrentCarsCount() == 0){
            showAllCars();
        }
        else {
            System.out.println("\nCars: ");
            for (int i = 0; i < getCurrentCarsCount(); i++) {
                System.out.print(Color.BLACK_B);
                System.out.print((1+i) + ". " + cars.get(i));
                if(show_cargo)
                    cars.get(i).showCargo();
                System.out.println();
            }
            System.out.println(Color.DEFAULT);
        }
    }

    public void sortCars(){
        cars.sort((car1,car2)-> Double.compare(car1.getGrossWeight(), car2.getGrossWeight()));
    }

    @Override
    public String toString() {
        return "#" + id + " Name: " + name
                + "  |  " + "Max pull weight: " + convertWeight(max_pull_weight)
                + "  |  Max train cars: " + max_train_cars_count
                + "  |  Max electric cars: " + max_electric_cars_count;
    }

    public boolean isAttachedToTrain() {
        return cars.size() > 0;
    }

    public boolean isOnTheWay() {
        return is_on_the_way;
    }

    public String getName() {
        return name;
    }

    public int getCurrentCarsCount() {
        return cars.size();
    }

    public int getCurrentElectricCarsCount() {
        int count = 0;
        for(Car car : cars){
            if(car.isElectricCar()){
                count++;
            }
        }
        return count;
    }

    public int getCurrentPullWeight() {
        int weight = 0;
        for(Car car : cars){
            weight += car.getGrossWeight();
        }
        return weight;
    }

    public double getMaxPullWeight() {
        return max_pull_weight;
    }

    public StaticArrayList<Car> getCars(){
        return cars;
    }

    public int getId() {
        return id;
    }

    public void run(Station start, Station destination){
        is_on_the_way = true;
        initPath(start, destination);
        RailroadMap.showPathStations(path);

        Thread speedThread = new Thread(speedThread());
        Thread arrivalCheck = new Thread(()->{
            try {
                while (true) {
                    synchronized (isOnStationMonitor) {

                        synchronized (RailroadMap.busyConnections) {
                            while (RailroadMap.busyConnections.contains(current_connection)) {
                                status = Status.Waiting;
                                RailroadMap.busyConnections.notifyAll();
                                RailroadMap.busyConnections.wait();
                            }
                            status = Status.Running;
                            RailroadMap.busyConnections.add(current_connection);
                            RailroadMap.busyConnections.notifyAll();
                        }

                        isOnStationMonitor.notify();
                        isOnStationMonitor.wait();

                        stations_passed_count++;
                        time = 0;
                        traveled_path += traveled_middle_distance;
                        traveled_middle_distance = 0;

                        synchronized (RailroadMap.busyConnections){
                            RailroadMap.busyConnections.remove(current_connection);
                            RailroadMap.busyConnections.notifyAll();
                        }

                        if (stations_passed_count == path.size()-1) {
                            initPath(destination, start);
                            traveled_path = 0;
                            stations_passed_count = 0;
                            status = Status.Waiting;
                            Thread.sleep(30_000);
                        }
                        else {
                            current_connection.shift(path.get(stations_passed_count+1));
                            status = Status.Waiting;
                            Thread.sleep(2000);
                        }
                    }
                }
            } catch (InterruptedException ignored) {}
        });

        Menu.addMenuThread(arrivalCheck);
        Menu.addMenuThread(speedThread);
        arrivalCheck.start();
        speedThread.start();

        success("Train \"" + this.getName() + "\" with " + cars.size() + (cars.size() == 1 ? " car" : " cars"), "ran");
    }

    public void initPath(Station start, Station destination){
        path = RailroadMap.getPath(start, destination);
        path_distance = RailroadMap.getPathDistance(path);
        this.home_station = start;
        this.destination_station = destination;
        current_connection = new Connection(start, path.get(1));
    }

    public Runnable speedThread(){
        return () -> {
            synchronized (isOnStationMonitor){
                try {
                    while (true) {
                        if (status.equals(Status.Running)) {
                            changeSpeed();
                            time++;

                            //sometimes gives bad result because of representation in binary format, so it can lead to rollback distance back
                            double buff = this.traveled_middle_distance;
                            double traveled_middle_distance = ((this.speed * 1000) / 3600 * time) / 1000;

                            if (traveled_middle_distance < buff)
                                traveled_middle_distance += buff - traveled_middle_distance;

                            Thread.sleep(1000);
                            if (traveled_middle_distance >= current_connection.distance()) {
                                if (path.get(stations_passed_count + 1).equals(current_connection.second())) {
                                    this.traveled_middle_distance = current_connection.distance();
                                    isOnStationMonitor.notify();
                                    isOnStationMonitor.wait();
                                }
                            } else
                                this.traveled_middle_distance = traveled_middle_distance;
                        }
                    }
                }  catch (RailroadHazard e) {
                    error(e.getMessage());
                    speed = 199;
                    isSlowingDown = true;
                } catch (InterruptedException exception) {
                }
            }
        };
    }

    public void changeSpeed() throws RailroadHazard {
        int force = getRandomBetween(-1,1, true);
        double speed = this.speed / 100 * 3 * force;
        if(this.speed >= 200){
            throw new RailroadHazard(this);
        }
        else if(isSlowingDown && this.speed > 140){
            this.speed -= 5;
        }
        else {
            isSlowingDown = false;
            if (this.speed + speed < 60)
                this.speed = 60;
            else
                this.speed += speed;
        }
    }

    public void showTrainData(){
        System.out.println();
        System.out.println(Color.BLACK_B +"Locomotive:\n"+ this);
        showAllCars(true);
        showTravelData();
    }

    public void showTravelData(){
        System.out.println(getTravelData());
    }

    public String getTravelData(){
        StringBuilder sb = new StringBuilder(Text.highlighted("Speed: "+roundDouble(speed,2)+"km/h\n"));
        sb.append(Text.highlightedPattern("/G/Home station: ://P/\"" + home_station.getName() +"\"://Y/ ---> ://G/Destination station: ://P/\"" +
                destination_station.getName()+"\":/\n\n"));

        for(int i = 0; i < path.size()-1; i++){
            sb.append(Text.highlightedPattern("/P/\""+path.get(i).getName()+"\":/ /G/- " + roundDouble(path.get(i+1).getConnectionDistance(path.get(i)), 2) + "km - :/"));
        }
        sb.append(Text.highlighted("\""+path.get(path.size()-1).getName()+"\"\n",Color.PURPLE));
        sb.append(Text.highlighted("Traveled between middle stations: " +
                roundDouble(traveled_middle_distance / current_connection.distance() * 100,0) + "%\n"));
        sb.append(Text.highlighted("Traveled between home - destination: " +
                getTraveledDistance() + "%\n"));
        sb.append(Text.highlightedPattern("/G/Current segment: :/" +current_connection+"\n"));
        sb.append(Text.highlightedPattern("/G/Status: :/"+((status == Status.Waiting)?"/Y/Waiting:/":"/P/Running:/\n")));
        return sb.toString();
    }

    public double getTraveledDistance(){
        return roundDouble((traveled_path + traveled_middle_distance) / path_distance * 100,0);
    }

    public static class Exceptions {
        public static class OutOfMaxTrainCars extends Exception{
            OutOfMaxTrainCars(int max){
                super("Maximum count of train cars is: " + max + "!");
            }
        }

        public static class OutOfMaxElectricTrainCars extends Exception{
            OutOfMaxElectricTrainCars(int max){
                super("Maximum count of electric train cars is: " + max + "!");
            }
        }

        public static class OutOfMaxPullWeight extends Exception{
            public OutOfMaxPullWeight(double max){
                super("Maximum pull weight is: " + convertWeight(max) + "!");
            }
        }

        public static class DoesNotContainsAnyCarException extends Exception{
            DoesNotContainsAnyCarException(){
                super("Train do not have any car attached!");
            }
        }

        public static class DoesNotContainsSuchCarException extends Exception{
            DoesNotContainsSuchCarException(){
                super("Train do not have such car attached!");
            }
        }
    }

    public static Car getRandomElectricCar(){
        while (true) {
            switch (getRandomBetween(1, 4)) {
                case 1 -> {
                    return PassengerCar.generate();
                }
                case 2 -> {
                    return MailCar.generate();
                }
                case 3 -> {
                    return RestaurantCar.generate();
                }
                case 4 -> {
                    return RefrigeratedCar.generate();
                }
            }
        }
    }

    public static Car getRandomRegularCar(){
        while (true) {
            switch (getRandomBetween(1, 8)) {
                case 1 -> {
                    return LuggageMailCar.generate();
                }
                case 2 -> {
                    return BasicFreightCar.generate();
                }
                case 3 -> {
                    return HeavyFreightCar.generate();
                }
                case 4 -> {
                    return LiquidMaterialsCar.generate();
                }
                case 5 -> {
                    return GaseousMaterialsCar.generate();
                }
                case 6 -> {
                    return ExplosiveMaterialsCar.generate();
                }
                case 7 -> {
                    return ToxicMaterialsCar.generate();
                }
                case 8 -> {
                    return LiquidToxicMaterialsCar.generate();
                }
            }
        }
    }

    public static Locomotive generate() {
        String[] names = {"TGV", "ICE", "Talgo", "ETR", "CR"};
        String name = getRandomBetween(names)+"-"+getRandomBetween(100,500);
        double max_pull_weight = Double.MAX_VALUE;
        int max_cars_count = getRandomBetween(5,10);
        int max_electric_cars_count = getRandomBetween(0,max_cars_count);
        double start_speed = roundDouble(getRandomBetween(50, 150), 0);
        Locomotive locomotive = new Locomotive(name, max_pull_weight, max_cars_count, max_electric_cars_count, start_speed, true);

        int rand_e_cars_count = getRandomBetween(0, max_electric_cars_count);
        for(int i = rand_e_cars_count; i > 0; i--){
            try {
                locomotive.addCar(getRandomElectricCar());
            }catch (Exception ignored){
                i++;
            }
        }

        for(int i = max_cars_count - rand_e_cars_count; i > 0; i--){
            try {
                locomotive.addCar(getRandomRegularCar());
            }catch (Exception ignored){
                i++;
            }
        }
        double pull_weight = 0;
        for(Car car : locomotive.cars)
            pull_weight += car.getGrossWeight();
        locomotive.max_pull_weight = roundDouble(pull_weight, 2);

        return locomotive;
    }

    public static class RailroadHazard extends Exception{
        RailroadHazard(Locomotive locomotive){
            super("The train crossed 200 km/h!\n" + locomotive);
        }
    }
}

