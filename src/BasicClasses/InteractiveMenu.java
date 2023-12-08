package BasicClasses;

import BasicClasses.Locomotive.Exceptions.*;
import Cars.*;
import Cars.Car.Exceptions.IsAlreadyAttachedException;
import Cars.Car.Exceptions.IsNotAttachedException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class InteractiveMenu extends Menu {
    private static final ArrayList<Locomotive> locomotives = new ArrayList<>();
    private static final ArrayList<Car> cars = new ArrayList<>();
    private static final ArrayList<Locomotive> trains = new ArrayList<>();

    public static void start(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println(Text.highlightedPattern("(To exit the program type /P/\"exit\":/ or /P/\"quit\":/, to move to previous section type /P/\"/\":/ or press /P/\"Enter\":/)"));
        run();
    }

    public static void MainPanel() {
        prevSection = Menu::close;
        System.out.println("\nChoose a section: 1.Locomotives | 2.Cars | 3.Trains | 4.Stations | 5.Railroad");

        findInputExpression("([1-5])", matcher -> {
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> chooseLocomotiveAction();
                case 2 -> chooseCarAction();
                case 3 -> chooseTrainAction();
                case 4 -> chooseStationAction();
                case 5 -> chooseRailroadAction();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::MainPanel);
    }

    public static void chooseLocomotiveAction(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println("\n1.Create locomotive | " + Text.disabled("2.Show available locomotives | 3.Delete locomotive", !hasAvailableLocomotives()));

        findInputExpression("([1-3])", matcher -> {
            prevSection = InteractiveMenu::chooseLocomotiveAction;
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> createLocomotiveMenu();
                case 2 -> showAvailableLocomotives();
                case 3 -> deleteLocomotiveMenu();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::chooseLocomotiveAction);
    }

    public static void createLocomotiveMenu(){
        prevSection = InteractiveMenu::chooseLocomotiveAction;
        System.out.println("\nEnter data in the following order: "+ Text.highlighted("Name | Max Pull Weight (tons) | Max Train Cars | Max Electric Cars | Start speed"));

        findInputExpression("(\\w+\\-?\\d*)\\s+(\\d+\\.?\\d*)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)", (Matcher matcher) -> {
            if(Integer.parseInt(matcher.group(3)) >= Integer.parseInt(matcher.group(4))) {
                locomotives.add(new Locomotive(matcher));
            }
            else {
                throw new WrongInputException("Total number of cars cannot be less than the number of electric cars!");
            }
        }, InteractiveMenu::createLocomotiveMenu);
    }

    public static void deleteLocomotiveMenu(){
        prevSection = InteractiveMenu::chooseLocomotiveAction;
        if(!hasAvailableLocomotives()) {
            showAvailableLocomotives();
        }
        else{
            System.out.print("\nSelect one item from the list:");
            showAvailableLocomotives();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= getAvailableLocomotivesCount()) {
                    success("Locomotive \"" + locomotives.remove(option-1).getName() + "\"", "removed");
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::deleteLocomotiveMenu);
        }
    }

    public static void chooseCarAction(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println("\n1.Create car | " +
                        Text.disabled("2.Show available cars | 3.Edit car load | 4.Delete car | ", !hasAvailableCars())+
                        Text.disabled("5.Show broken cars", !hasDamagedCars()));

        findInputExpression("([1-5])", matcher -> {
            prevSection = InteractiveMenu::chooseCarAction;
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> createCar();
                case 2 -> showAvailableCars();
                case 3 -> editCarLoad();
                case 4 -> deleteCar();
                case 5 -> showDamagedCars();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::chooseCarAction);
    }

    public static void createCar(){
        prevSection = InteractiveMenu::chooseCarAction;
        System.out.println("\nChoose train car type: ");
        showCarTypes();

        findInputExpression("(\\d+)", (Matcher matcher) -> {
            int option = Integer.parseInt(matcher.group(1));
            if(option > 0 && option <= 12) {
            System.out.print("\nEnter data in the following order: ");
                switch (option) {
                    case 1 -> {
                        System.out.println(PassengerCar.showConstructorParameters());
                        findInputExpression(PassengerCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new PassengerCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 2 -> {
                        System.out.println(MailCar.showConstructorParameters());
                        findInputExpression(MailCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new MailCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 3 -> {
                        System.out.println(LuggageMailCar.showConstructorParameters());
                        findInputExpression(LuggageMailCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new LuggageMailCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 4 -> {
                        System.out.println(RestaurantCar.showConstructorParameters());
                        findInputExpression(RestaurantCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new RestaurantCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 5 -> {
                        System.out.println(BasicFreightCar.showConstructorParameters());
                        findInputExpression(BasicFreightCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new BasicFreightCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 6 -> {
                        System.out.println(HeavyFreightCar.showConstructorParameters());
                        findInputExpression(HeavyFreightCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new HeavyFreightCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 7 -> {
                        System.out.println(RefrigeratedCar.showConstructorParameters());
                        findInputExpression(RefrigeratedCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new RefrigeratedCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 8 -> {
                        System.out.println(LiquidMaterialsCar.showConstructorParameters());
                        findInputExpression(LiquidMaterialsCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new LiquidMaterialsCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 9 -> {
                        System.out.println(GaseousMaterialsCar.showConstructorParameters());
                        findInputExpression(GaseousMaterialsCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new GaseousMaterialsCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 10 -> {
                        System.out.println(ExplosiveMaterialsCar.showConstructorParameters());
                        findInputExpression(ExplosiveMaterialsCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new ExplosiveMaterialsCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 11 -> {
                        System.out.println(ToxicMaterialsCar.showConstructorParameters());
                        findInputExpression(ToxicMaterialsCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new ToxicMaterialsCar(_matcher)), InteractiveMenu::createCar);
                    }
                    case 12 -> {
                        System.out.println(LiquidToxicMaterialsCar.showConstructorParameters());
                        findInputExpression(LiquidToxicMaterialsCar.getParametersRegExp(), (Matcher _matcher) -> cars.add(new LiquidToxicMaterialsCar(_matcher)), InteractiveMenu::createCar);
                    }
                    default -> throw new WrongInputException();
                }
            }else
                throw new WrongInputException();
        }, InteractiveMenu::createCar);
    }

   public static void chooseCar(TemplateFunction<Integer> function, boolean showDamaged){
       prevSection = InteractiveMenu::chooseCarAction;
       if(!hasAvailableCars())
           showAvailableCars();
       else {
           System.out.print("\nSelect one item from the list:");
           if(showDamaged)
               showAllCars();
           else
               showAvailableCars();

           findInputExpression("(\\d+)", (Matcher matcher) -> {
               int option = Integer.parseInt(matcher.group(1));
               if (option > 0 && option <= (showDamaged ? getAllCarsCount() : getAvailableCarsCount())) {
                   function.call(option);
               } else
                   throw new WrongInputException();
           }, () -> chooseCar(function, false));
       }
   }

    public static void editCarLoad(){
        chooseCar((option) -> cars.get(option-1).interact(InteractiveMenu::chooseCarAction), false);
    }

    public static void deleteCar(){
        prevSection = InteractiveMenu::chooseCarAction;
        if(!hasAvailableCars() && !hasDamagedCars()) {
            showAvailableCars();
        }
        else{
            chooseCar((option) -> {
                Car removed = cars.remove(option-1);
                success("Car #" + removed.getId() + " " + removed.getType(), "removed");
            }, true);
        }
    }

    public static void chooseTrainAction(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println("\n"+Text.disabled("1.Create train | ", !hasAvailableLocomotives() || !hasAvailableCars()) + Text.disabled("2.Show available trains | 3.Edit train cars load | 4.Delete train", !hasAvailableTrains()));

        findInputExpression("([1-4])", matcher -> {
            prevSection = InteractiveMenu::chooseTrainAction;
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> createTrain();
                case 2 -> showAvailableTrains();
                case 3 -> editCarsLoad();
                case 4 -> deleteTrain();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::chooseTrainAction);
    }

    public static void createTrain(){
        prevSection = InteractiveMenu::chooseTrainAction;
        if(!hasAvailableLocomotives()) {
            showAvailableLocomotives();
        }
        else if(!hasAvailableCars()){
            showAvailableCars();
        }
        else{
            System.out.print("\nSelect one item from the list:");
            showAvailableLocomotives();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= getAvailableLocomotivesCount()) {
                    Locomotive locomotive = locomotives.get(option - 1);
                    addCars(locomotive);
                    if (locomotive.isAttachedToTrain()) {
                        trains.add(locomotive);
                        locomotive.sortCars();
                        success("Train", "created");
                    }
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::createTrain);
        }
    }

    public static void addCars(Locomotive locomotive) {
        System.out.print("\nSelect elements from the list:");
        showAvailableCars();

        findInputExpression("(\\d+)", InputMode.UNIQUE, (Matcher matcher) -> {
            try {
                List<Integer> list = new ArrayList<>(matcher.results().map(elem -> Integer.parseInt(elem.group())).toList());
                list.sort((i1, i2) -> i1 - i2);
                int availableCars = (int) getAvailableCarsCount();

                //Check if entered numbers is more than available cars
                //or if input contains number that is bigger than count of available car
                if(list.size() > availableCars || list.get(list.size()-1) > availableCars || list.get(0) <= 0)
                    throw new WrongInputException();
                else {
                    for (int i : list) {
                        locomotive.addCar(cars.get(i - 1));
                    }
                }
            }catch (OutOfMaxElectricTrainCars | OutOfMaxPullWeight | OutOfMaxTrainCars | IsAlreadyAttachedException e){
                if(locomotive.isAttachedToTrain())
                    error("Wrong input! " + e.getMessage() + " Redundant cars was removed.");
                else
                    throw new WrongInputException(e.getMessage() + " Redundant cars was removed.");
            }
        }, InteractiveMenu::createTrain);
    }

    public static void editCarsLoad(){
        prevSection = InteractiveMenu::chooseTrainAction;
        if(!hasAvailableTrains()){
            showAvailableTrains();
        }else {
            System.out.print("\nSelect one item from the list:");
            showAvailableTrains();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= getAvailableTrainsCount()) {
                    System.out.print("\nSelect one item from the list:\n");
                    System.out.print(Color.BLACK_B);
                    trains.get(option-1).showAllCars();
                    System.out.print(Color.DEFAULT);

                    findInputExpression("(\\d+)", (Matcher _matcher) -> {
                        int _option = Integer.parseInt(_matcher.group(1));
                        if (_option > 0 && _option <= trains.get(option-1).getCurrentCarsCount()) {
                            trains.get(option-1).getCars().get(_option-1).interact(InteractiveMenu::chooseTrainAction);
                        } else
                            throw new WrongInputException();
                    }, InteractiveMenu::editCarsLoad);
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::editCarsLoad);
        }
    }

    public static void deleteTrain(){
        prevSection = InteractiveMenu::chooseTrainAction;
        if(!hasAvailableTrains()) {
            showAvailableTrains();
        }else {
            System.out.print("\nSelect one item from the list:");
            showAvailableTrains();

            //The locomotive and train cars will not delete, but only will separate from each other.
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= getAvailableTrainsCount()) {
                    Locomotive train = trains.remove(option-1);
                    try {
                        train.detachAllCars();
                    }catch (DoesNotContainsAnyCarException | DoesNotContainsSuchCarException | IsNotAttachedException e){
                        throw new WrongInputException(e.getMessage());
                    }
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::deleteTrain);
        }
    }

    public static void showAvailableLocomotives(){
        if(hasAvailableLocomotives()) {
            locomotives.sort((loc1, loc2) -> Boolean.compare(loc1.isAttachedToTrain(),loc2.isAttachedToTrain()));
            System.out.println();
            System.out.println(Color.BLACK_B+"Locomotives: ");
            for (int i = 0, j = (int) getAvailableLocomotivesCount(); i < j && !locomotives.get(i).isAttachedToTrain(); i++) {
                System.out.println("\t" + (1 + i) + ". " + locomotives.get(i));
            }
            System.out.print(Color.DEFAULT);
        }
        else {
            error("No available locomotives!");
        }
    }

    public static void showAvailableCars(){
        if(hasAvailableCars()) {
            cars.sort((car1, car2) -> car1.compareTo(car2));
            System.out.println();
            System.out.println(Color.BLACK_B+"Cars: ");
            for (int i = 0, j = (int) getAvailableCarsCount(); i < j && !cars.get(i).isAttachedToTrain(); i++) {
                System.out.println("\t" + (1 + i) + ". " + cars.get(i));
            }
            System.out.print(Color.DEFAULT);
        }
        else {
            error("No available cars!");
        }
    }

    private static void showDamagedCars(){
        if(hasDamagedCars()) {
            System.out.println();
            System.out.println(Color.BLACK_B+"Broken Cars: ");
            int i = 1;
            for(Car car : cars){
                if(car.isDamaged())
                    System.out.println("\t" + (i++) + ". " + car);
            }
            System.out.print(Color.DEFAULT);
        }
        else {
            error("No broken cars!");
        }
    }

    private static void showAllCars(){
        if(!hasAvailableCars() && !hasDamagedCars()){
            error("No cars added");
        }
        else {
            System.out.println();
            System.out.println(Color.BLACK_B+"Cars: ");
            int i = 1;
            for(Car car : cars){
                if(!car.isAttachedToTrain())
                    System.out.println("\t" + (i++) + ". " + car);
            }
            System.out.print(Color.DEFAULT);
        }
    }

    public static void showAvailableTrains(){
        if(hasAvailableTrains()) {
            System.out.println();
            System.out.println(Color.BLACK_B+"Trains: ");
            for (int i = 0, j = (int) getAvailableTrainsCount(); i < j && !trains.get(i).isOnTheWay(); i++) {
                System.out.println((i+1)+".\t" + "Locomotive:\n\t\t" + trains.get(i));
                trains.get(i).showAllCars();
                System.out.println("------------------------------------------------------------------------------------------------------------------------");
            }
            System.out.print(Color.DEFAULT);
        }else {
            error("No available trains!");
        }
    }

    public static void showRunningTrains(){
        if(hasTrainsOnTheWay()) {
            System.out.println();
            System.out.println(Color.BLACK_B+"Trains: ");
            for (int i = 0; i < RailroadMap.trainsOnTheWay.size(); i++) {
                System.out.println((i+1)+".\t" + "Locomotive:\n\t\t" + RailroadMap.trainsOnTheWay.get(i));
            }
            System.out.print(Color.DEFAULT);
        }else {
            error("No running trains!");
        }
    }

    public static void showCarTypes(){
        System.out.println(Color.BLACK_B+"""
                \t1.Wagon pasażerski      (needs electricity)
                \t2.Wagon pocztowy        (needs electricity)
                \t3.Wagon bagażowo-pocztowy
                \t4.Wagon restauracyjny   (needs electricity)
                \t5.Wagon towarowy podstawowy
                \t6.Wagon towarowy ciężki
                \t7.Wagon chłodniczy      (needs electricity)
                \t8.Wagon na materiały ciekłe
                \t9.Wagon na materiały gazowe
                \t10.Wagon na materiały wybuchowe
                \t11.Wagon na materiały toksyczne
                \t12.Wagon na ciekłe materiały toksyczne""");
        System.out.print(Color.DEFAULT);
    }

    public static long getAvailableLocomotivesCount(){
        return locomotives.stream().filter(elem -> !elem.isAttachedToTrain()).count();
    }

    public static long getAvailableCarsCount(){
        return cars.stream().filter(elem -> !elem.isAttachedToTrain() && !elem.isDamaged()).count();
    }

    public static long getAllCarsCount(){
        return cars.stream().filter(elem -> !elem.isAttachedToTrain()).count();
    }

    public static long getAvailableTrainsCount(){
        return trains.stream().filter(elem -> elem.isAttachedToTrain() && !elem.isOnTheWay()).count();
    }

    public static long getOnTheWayTrainsCount(){
        return RailroadMap.trainsOnTheWay.size();
    }

    public static boolean hasAvailableLocomotives(){
        return locomotives.stream().anyMatch(elem -> !elem.isAttachedToTrain());
    }

    public static boolean hasAvailableCars(){
        return cars.stream().anyMatch(elem -> !elem.isAttachedToTrain() && !elem.isDamaged());
    }

    public static boolean hasDamagedCars(){
        return cars.stream().anyMatch(elem -> !elem.isAttachedToTrain() && elem.isDamaged());
    }

    public static boolean hasAvailableTrains(){
        return trains.stream().anyMatch(elem -> !elem.isOnTheWay());
    }

    public static boolean hasTrainsOnTheWay(){
        return RailroadMap.trainsOnTheWay.size() > 0;
    }

    //Stations

    public static void chooseStationAction(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println("\n1.Create station | " + Text.disabled("2.Edit station connections", RailroadMap.stations.size() < 2) + Text.disabled(" | 3.Show available stations | 4.Delete station", RailroadMap.stations.size() == 0));

        findInputExpression("([1-4])", matcher -> {
            prevSection = InteractiveMenu::chooseStationAction;
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> createStation();
                case 2 -> editStationConnections();
                case 3 -> RailroadMap.showAllStations(false);
                case 4 -> deleteStation();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::chooseStationAction);
    }

    public static void createStation(){
        prevSection = InteractiveMenu::chooseStationAction;
        System.out.println("Enter station name: ");

        findInputExpression("([^\s]+)", (Matcher matcher) ->{
            if(RailroadMap.stations.containsKey(matcher.group(1))) {
                error("Station with name \"" + matcher.group(1) + "\" already exists!");
            }
            else
                RailroadMap.stations.put(matcher.group(1), new Station(matcher.group(1)));
        }, InteractiveMenu::createStation);
    }

    public static void editStationConnections(){
        prevSection = InteractiveMenu::chooseStationAction;
        if(RailroadMap.stations.size() < 2) {
            error("To make a connection you need at least 2 stations created! (only " + RailroadMap.stations.size() + " exist)");
        }
        else {
            System.out.println("\n1.Connect stations | " + Text.disabled("2.Remove connection", isNotConnected()));

            findInputExpression("([1-2])", (Matcher matcher) -> {
                switch (Integer.parseInt(matcher.group(1))) {
                    case 1 -> connectStations();
                    case 2 -> disconnectStations();
                    default -> throw new WrongInputException();
                }
            }, InteractiveMenu::editStationConnections);
        }
    }

    public static void connectStations(){
        prevSection = InteractiveMenu::editStationConnections;
        System.out.println("\nEnter numbers in the following order (can not be same station): " + Text.highlighted("First Station, Second Station, Distance"));
        RailroadMap.showAllStations(false);

        findInputExpression("(\\d+)\\s+(\\d+)\\s+(\\d+\\.?\\d*)", (Matcher matcher) -> {
            int option_1 = Integer.parseInt(matcher.group(1));
            int option_2 = Integer.parseInt(matcher.group(2));
            double distance = Double.parseDouble(matcher.group(3));
            if(distance == 0){
                throw new WrongInputException("Distance must be longer than 0");

            } else if (option_1 > 0 && option_2 > 0 && option_1 <= RailroadMap.stations.size() && option_2 <= RailroadMap.stations.size()) {
                Object[] arr = RailroadMap.stations.values().toArray();
                try {
                    ((Station) arr[option_1-1]).addConnection(((Station) arr[option_2-1]), distance);
                    success("Connection \"" + ((Station) arr[option_1-1]).getName() + "\" <---> \"" + ((Station) arr[option_2-1]).getName()+"\"", "created");
                } catch (Station.isSameStationException | Station.HasSameStationException e) {
                    throw new WrongInputException(e.getMessage());
                }
            } else
                throw new WrongInputException();
        }, InteractiveMenu::connectStations);
    }

    public static void disconnectStations(){
        prevSection = InteractiveMenu::editStationConnections;
        if(isNotConnected()){
            error("No stations connected!");
        } else {
            System.out.print("\nSelect one item from the list:");
            RailroadMap.showAllStations(false);

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= RailroadMap.stations.size()){
                    Object[] arr = RailroadMap.stations.values().toArray();
                    chooseConnection((Station) arr[option-1]);
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::disconnectStations);
        }
    }

    public static boolean isNotConnected(){
        boolean hasConnection = false;
        Iterator<Station> iterator = RailroadMap.stations.values().iterator();
        while (!hasConnection && iterator.hasNext())
            if(iterator.next().hasAnyConnection()){
                hasConnection = true;
            }
        return !hasConnection;
    }

    public static void chooseConnection(Station station){
        System.out.print("\nSelect one connection from the list:");
        station.showConnections();

        findInputExpression("(\\d+)", (Matcher matcher) -> {
            int option = Integer.parseInt(matcher.group(1));
            if(option > 0 && option <= station.getConnections().size()){
                Object[] arr = station.getConnections().keySet().toArray();
                station.removeConnection((Station) arr[option-1]);
                success("Connection \"" + station.getName() + "\" <---> \"" + ((Station) arr[option-1]).getName() + "\"", "removed");
            } else
                throw new WrongInputException();
        }, InteractiveMenu::disconnectStations);
    }

    public static void deleteStation() {
        prevSection = InteractiveMenu::chooseStationAction;
        if(RailroadMap.stations.size() == 0){
            RailroadMap.showAllStations(false);
        } else {
            System.out.println();
            System.out.print("Select one item from the list:");
            RailroadMap.showAllStations(false);

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= RailroadMap.stations.size()){
                    Object[] arr = RailroadMap.stations.values().toArray();
                    Object[] set = ((Station) arr[option-1]).getConnections().keySet().toArray();
                    for(Object elem : set){
                        ((Station) arr[option-1]).removeConnection((Station) elem);
                    }
                    RailroadMap.stations.remove(((Station) arr[option-1]).getName());
                    success("Station \"" + ((Station) arr[option-1]).getName() + "\"", "removed");
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::deleteStation);
        }
    }

    //Railroad

    public static void chooseRailroadAction(){
        prevSection = InteractiveMenu::MainPanel;
        System.out.println("\n"+
                Text.disabled("1.Generate station | ", RailroadMap.isGenerated()) +
                "2.Generate train | " +
                Text.disabled("3.Start the train | ", trains.size() == 0) +
                Text.disabled("4.Show trains info", trains.size() == 0 || !hasTrainsOnTheWay())
        );

        findInputExpression("([1-4])", matcher -> {
            switch (Integer.parseInt(((Matcher) matcher).group(1))) {
                case 1 -> generateStations();
                case 2 -> generateTrainMenu();
                case 3 -> startTrainMenu();
                case 4 -> showTrainsInfoMenu();
                default -> throw new WrongInputException();
            }
        }, InteractiveMenu::chooseRailroadAction);
    }

    public static void generateStations(){
        prevSection = InteractiveMenu::chooseRailroadAction;
        if(RailroadMap.generate()) {
            RailroadMap.printRailroad();
            success("Railroad map", "generated");
        } else
            error("Railroad map is already generated!");
    }

    public static void generateTrainMenu(){
        prevSection = InteractiveMenu::chooseRailroadAction;
        Locomotive train = Locomotive.generate();
        locomotives.add(train);
        success("Train \"" + train.getName() + "\" with " + train.getCurrentCarsCount() + (train.getCurrentCarsCount() == 1 ? " car" : " cars"), "generated");
    }

    public static void generateTrains(int count){
        for(int i = 0; i < count; i++){
            trains.add(Locomotive.generate());
        }
    }

    public static void startTrainMenu(){
        prevSection = InteractiveMenu::chooseRailroadAction;
        if(!RailroadMap.isGenerated() && RailroadMap.stations.size() == 0){
            error("Railroad map is not generated!");
        }else if(!hasAvailableTrains()){
            showAvailableTrains();
        }
        else if(RailroadMap.stations.size() < 2){
            error("Only 1 station created!");
        }
        else {
            System.out.print("\nSelect starting station:");
            RailroadMap.showAllStations(true);

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= RailroadMap.stations.size()){
                    Object[] arr = RailroadMap.stations.values().toArray();
                    ((Station)arr[option-1]).setAvailable(false);

                    selectDestinationStation(((Station)arr[option-1]));
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::startTrainMenu);
        }
    }

    public static void selectDestinationStation(Station first){
        prevSection = () -> resetTrainStart(first, null);
        System.out.print("\nSelect destination station:");
        RailroadMap.showAllStations(true);

        findInputExpression("(\\d+)", (Matcher matcher) -> {
            int option = Integer.parseInt(matcher.group(1));
            if(option > 0 && option <= RailroadMap.stations.size()-1){
                Object[] arr = RailroadMap.stations.values().toArray();
                Arrays.sort(arr, (s1, s2) -> (((Station) s2).isAvailable() ? 1 : 0) - (((Station) s1).isAvailable() ? 1 : 0));
                ((Station)arr[option-1]).setAvailable(false);

                selectTrain(first, ((Station)arr[option-1]));
            } else
                throw new WrongInputException();
        }, ()->selectDestinationStation(first));
    }

    public static void selectTrain(Station first, Station second){
        prevSection = () -> resetTrainStart(first, second);
        System.out.print("\nSelect one item from the list:");
        showAvailableTrains();

        findInputExpression("(\\d+)", (Matcher matcher) -> {
            int option = Integer.parseInt(matcher.group(1));
            if (option > 0 && option <= getAvailableTrainsCount()) {
                System.out.println(first.shortInfo() + " " + second.shortInfo());

                RailroadMap.addTrain(trains.remove(option-1)).run(first, second);
                startLogging();

            } else
                throw new WrongInputException();
        }, ()->selectTrain(first, second));
    }

    public static void resetTrainStart(Station s1, Station s2){
        prevSection = InteractiveMenu::chooseRailroadAction;
        if(s1 != null)
            s1.setAvailable(true);
        if(s2 != null)
            s2.setAvailable(true);
    }

    public static void showTrainsInfoMenu(){
        prevSection = InteractiveMenu::chooseRailroadAction;
        if(!hasTrainsOnTheWay()){
            error("No trains on the way!");
        } else {
            System.out.print("\nSelect one item from the list:");
            showRunningTrains();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= getOnTheWayTrainsCount()) {
                    RailroadMap.trainsOnTheWay.get(option-1).showTrainData();
                } else
                    throw new WrongInputException();
            }, InteractiveMenu::showTrainsInfoMenu);
        }
    }

    public static void startLogging(){
            Thread logger = new Thread(()->{
                FileWriter fileWriter;
                StringBuilder log = new StringBuilder();
                try {
                    while (isRunning()){
                        fileWriter = new FileWriter("src\\App.txt");
                            ArrayList<Locomotive> locomotives = new ArrayList<>(RailroadMap.trainsOnTheWay.stream().toList());
                            locomotives.sort((l1, l2) -> (int) (l2.getTraveledDistance() - l1.getTraveledDistance()));

                            for(Locomotive locomotive : locomotives){
                                log.append("Locomotive: ").append(locomotive+"\n\n").append(locomotive.getTravelData()).append("Cars: \n");
                                for(Car car : locomotive.getCars()){
                                    log.append("\n").append(car).append(car.getCargoInfo()).append("\n");
                                }
                                log.append("\n\n--------------------------------------------------------------------------------------------------------------------\n");
                                log.append("********************************************************************************************************************\n");
                                log.append("--------------------------------------------------------------------------------------------------------------------\n\n\n");
                            }
                            String str = log.toString().replaceAll(Menu.Color.getAllColorsRegex(), "");
                            log.delete(0, log.length());

                            fileWriter.write(str);
                            fileWriter.close();
                            Thread.sleep(5000);
                    }
                } catch (InterruptedException | IOException e) {
                    System.out.println("Something went wrong!");
                }
            });
            logger.start();
    }
}

