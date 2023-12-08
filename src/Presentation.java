import BasicClasses.Locomotive;
import BasicClasses.Menu;
import BasicClasses.RailroadMap;
import BasicClasses.Station;
import CargoClasses.*;
import Cars.*;

import java.util.ArrayList;

import static BasicClasses.InteractiveMenu.startLogging;

public class Presentation {

    public static void main(String[] args) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight, ProductContainer.Exceptions.MinMaxException, InterruptedException {

        //Those methods create new stations and put them into stations Map in RailroadMap class (ignoreEvent - true: doesn't write anything in console)
        RailroadMap.addStation(new Station("Warszawa", true));
        RailroadMap.addStation(new Station("Łódź", true));
        RailroadMap.addStation(new Station("Lublin", true));
        RailroadMap.addStation(new Station("Gdańsk", true));

        RailroadMap.showAllStations(false);


        //This method connects two created stations together (station cannot connect to itself)
        try {
            RailroadMap.stations.get("Warszawa").addConnection(RailroadMap.stations.get("Łódź"), 0.6);
            RailroadMap.stations.get("Łódź").addConnection(RailroadMap.stations.get("Lublin"), 0.6);
            RailroadMap.stations.get("Lublin").addConnection(RailroadMap.stations.get("Gdańsk"), 0.6);
        }catch (Station.isSameStationException | Station.HasSameStationException e){
            System.out.println("Stations are already connected");
        }
        RailroadMap.showAllStations(false);

        //Those methods disconnecting two connected stations
        RailroadMap.stations.get("Warszawa").removeConnection(RailroadMap.stations.get("Łódź"));
        RailroadMap.stations.get("Łódź").removeConnection(RailroadMap.stations.get("Lublin"));
        RailroadMap.stations.get("Lublin").removeConnection(RailroadMap.stations.get("Gdańsk"));

        RailroadMap.showAllStations(false);

        //RailroadMap.getPath() searching for path from first to second station end returns path as arrayList
        ArrayList<Station> stations = RailroadMap.getPath("Warszawa", "Gdańsk");
        for(Station station : stations){
            System.out.print("\""+station.getName()+"\"" + " --> ");
        }
        System.out.println("\n");


        {
            try {
                RailroadMap.stations.get("Warszawa").addConnection(RailroadMap.stations.get("Łódź"), 0.6);
                RailroadMap.stations.get("Łódź").addConnection(RailroadMap.stations.get("Lublin"), 0.6);
                RailroadMap.stations.get("Lublin").addConnection(RailroadMap.stations.get("Gdańsk"), 0.6);
            }catch (Station.isSameStationException | Station.HasSameStationException e){
                System.out.println("Stations are already connected");
            }
            RailroadMap.showAllStations(false);
        }

        //Methods generating a train and start it
        RailroadMap.addTrain(Locomotive.generate()).run(RailroadMap.stations.get("Warszawa"), RailroadMap.stations.get("Lublin"));
        RailroadMap.addTrain(Locomotive.generate()).run(RailroadMap.stations.get("Warszawa"), RailroadMap.stations.get("Łódź"));

        //Shows travel info about every running train
        RailroadMap.startRailroadTracking();

        //This method generates a stations end connections between them (names for stations are in file "StationsNames.txt")
        RailroadMap.generate();
        RailroadMap.showAllStations(false);

        System.out.println();

        //Example of locomotive constructor (name, max_pull_weight, max_cars_count, max_electric_cars_count, start speed)
        Locomotive locomotive = new Locomotive("ICE-237", 100000.0, 10, 4, 100);
        System.out.println(locomotive);

        //Adding a random passenger car to locomotive
        try {
            locomotive.addCar(PassengerCar.generate());
        } catch (Locomotive.Exceptions.OutOfMaxTrainCars |
                Locomotive.Exceptions.OutOfMaxElectricTrainCars |
                Locomotive.Exceptions.OutOfMaxPullWeight |
                Car.Exceptions.IsAlreadyAttachedException e){
            System.out.println(e.getMessage());
        }

        //showing cars info
        locomotive.showAllCars();

        //showing cargo info
        locomotive.getCars().get(0).showCargo();

        //Setting a 250km/h speed to invoke RailroadHazardException
        Locomotive locomotive1 = new Locomotive("ICE-237", 100000.0, 10, 4, 250);
        locomotive1.run(RailroadMap.getRandomStation(), RailroadMap.getRandomStation());
        RailroadMap.startRailroadTracking();

        //Another locomotive exceptions
        Locomotive locomotive2 = new Locomotive("ICE-237", 10000, 0, 0, 250);
        for (int i = 0 ; i < 3; i++){
            try {
                locomotive2.addCar(PassengerCar.generate());
                locomotive2.addCar(HeavyFreightCar.generate());
            } catch (Locomotive.Exceptions.OutOfMaxTrainCars |
                     Locomotive.Exceptions.OutOfMaxElectricTrainCars |
                     Locomotive.Exceptions.OutOfMaxPullWeight |
                     Car.Exceptions.IsAlreadyAttachedException e) {
                System.out.println(e.getMessage());
            }
        }
        RailroadMap.startRailroadTracking();

        //Writing data to App.txt file
        startLogging();

        //Passenger car methods
        try {
            PassengerCar passengerCar = new PassengerCar("PJATK", 100.0, 80, 20);
            passengerCar.showAllPassengers();
            passengerCar.showVipPassengers();
            passengerCar.showAllSeatsInfo();
            passengerCar.reserveSeat(new Person("Michał", "Wiśniewski", 23, 'm', 60, Person.Ticket.Regular));
            passengerCar.reserveSeat(new Person("Michał", "Wiśniewski", 27, 'm', 32, Person.Ticket.VIP));

            passengerCar.showAllPassengers();
            passengerCar.showVipPassengers();
            passengerCar.showAllSeatsInfo();
        }catch (PassengerCar.Exceptions.PassengerIsAlreadyAddedException | Menu.WrongInputException ex){
            System.out.println(ex.getMessage());
        }catch (Exception e){}


        //Mail car methods
        MailCar mailCar = new MailCar("PJATK", 100.0, 80_000, 0.5);
        System.out.println(mailCar);
        for(int i = 0; i < 3; i++)
            mailCar.addMail(Mail.generate());

        mailCar.showMailsFromSenderMenu();
        System.out.println(mailCar);

        mailCar.removeMailMenu();
        System.out.println(mailCar.getCargoInfo());
        System.out.println("\n"+mailCar);

        //Luggage-Mail car methods
        LuggageMailCar luggageMailCar = new LuggageMailCar("PJATK", 100.0, 80_000, 60, 0.5);
        for(int i = 0; i < 6; i++)
            luggageMailCar.addMail(Mail.generate());
        System.out.println(luggageMailCar.getUniqueNames());
        System.out.println(luggageMailCar.getUniqueOwnersCount());
        System.out.println(luggageMailCar.getCargoInfo());

        //Restaurant car methods
        RestaurantCar restaurantCar = new RestaurantCar("PJATK", 100, "Polish", 40, 120);
        restaurantCar.showCargo();
        restaurantCar.addMenuCategory("Sunrise");
        for(int i = 0; i < 7; i++)
            restaurantCar.addDish("Sunrise", Dish.generate());

        restaurantCar.showMenu();
        System.out.println(restaurantCar);

        System.out.println("Drinks count: " + restaurantCar.getDrinksCount());
        System.out.println("Dishes count: " + restaurantCar.getDishesCount());

        BasicFreightCar basicFreightCar = new BasicFreightCar("PJATK", 100, 13);
        System.out.println(basicFreightCar);
        for(int i = 0; i < 7; i++)
            basicFreightCar.addContainer(Container.generate());

        System.out.println(basicFreightCar);
        basicFreightCar.showCargo();

        basicFreightCar.sortCargoByName();
        basicFreightCar.showCargo();

        basicFreightCar.sortCargoByWeight();
        basicFreightCar.showCargo();

        //Heavy freight car methods
        HeavyFreightCar heavyFreightCar = new HeavyFreightCar("PJATK", 100);
        Cargo cargo = Cargo.generate();
        heavyFreightCar.loadTrainCarMenu(cargo);
        System.out.println(heavyFreightCar);
        heavyFreightCar.showCargo();

        heavyFreightCar.unloadTrainCar(cargo);
        System.out.println(heavyFreightCar);
        heavyFreightCar.showCargo();

        //refrigerated car methods
        RefrigeratedCar refrigeratedCar = new RefrigeratedCar("PJAtk", 100, 100, -40);
        refrigeratedCar.addContainer(new ProductContainer("Fish", 40, -30, 4));
        refrigeratedCar.addContainer(new ProductContainer("Fruits", 40, 0, 12));

        System.out.println(refrigeratedCar.calculateAcceptableTemperature());
        refrigeratedCar.normalizeTemperatureMenu();

        //liquid materials Car methods
        LiquidMaterialsCar liquidMaterialsCar = new LiquidMaterialsCar("PJATK", 100, 100);
        for(int i = 0; i < 7; i++)
            liquidMaterialsCar.addContainer(LiquidBottle.generate());

        Thread thread = new Thread(()->{
            liquidMaterialsCar.initiateLeakMenu(); //<------------------
            for(int i = 0; i < 10; i++){
                try {
                    Thread.sleep(1000);
                    System.out.println("Leakage volume: "+liquidMaterialsCar.getLeakageVolume());
                    System.out.println("Humidity level: "+liquidMaterialsCar.getHumidityLevel());
                } catch (InterruptedException e) {
                    return;
                }
            }
            //You need to wait a little, ventilating is not fast
            liquidMaterialsCar.ventilateCarMenu(); //<------------------
            System.out.println("Leakage volume: "+liquidMaterialsCar.getLeakageVolume());
            System.out.println("Humidity level: "+liquidMaterialsCar.getHumidityLevel());

        });
        thread.start();

        GaseousMaterialsCar gaseousMaterialsCar = new GaseousMaterialsCar("PJATK", 100, 100);
        for(int i = 0; i < 7; i++)
            gaseousMaterialsCar.addContainer(GasBottle.generate());

        gaseousMaterialsCar.initiateLeakMenu();
        gaseousMaterialsCar.initiateLeakMenu();

        gaseousMaterialsCar.checkBottlesMenu();
        Thread.sleep(1000);
        gaseousMaterialsCar.showContainersMenu();
        System.out.println(gaseousMaterialsCar);

        //Explosive materials car methods
        ExplosiveMaterialsCar explosiveMaterialsCar = new ExplosiveMaterialsCar("PJATK", 100);
        ExplosiveCargo explosiveCargo = new ExplosiveCargo("bomb", 1, 20);
        explosiveMaterialsCar.loadTrainCar(explosiveCargo);
        explosiveMaterialsCar.findDamageCargoMenu();
        explosiveMaterialsCar.neutralizeDamagedCargoMenu();
        explosiveMaterialsCar.showCargo();

        explosiveMaterialsCar.unloadTrainCar(explosiveCargo);
        System.out.println(explosiveMaterialsCar);

        //Toxic materials car methods
        ToxicMaterialsCar toxicMaterialsCar = new ToxicMaterialsCar("PJATK", 100, 1);
        toxicMaterialsCar.loadTrainCar(new ToxicCargo("cadmium", 10, 10));
        System.out.println(toxicMaterialsCar.getIntegrityLevel());
        toxicMaterialsCar.repairWallsMenu();

        //Toxic liquid materials car
        LiquidToxicMaterialsCar liquidToxicMaterialsCar = new LiquidToxicMaterialsCar("PJATK", 100, 120, 1);
        liquidToxicMaterialsCar.loadTrainCar(new ToxicLiquidsTanker("mercury", 200, 90, 10));
        for(int i = 0; i < 6; i++){
            Thread.sleep(1000);
            liquidToxicMaterialsCar.showTankerVolumeMenu();
        }
        liquidToxicMaterialsCar.checkTightnessMenu();
        liquidToxicMaterialsCar.showTankerVolumeMenu();
        System.out.println(liquidToxicMaterialsCar);

    }
}