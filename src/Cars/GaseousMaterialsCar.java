package Cars;

import BasicClasses.Menu;
import CargoClasses.Container;
import CargoClasses.GasBottle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class GaseousMaterialsCar extends BasicFreightCar{
    private final ArrayList<Thread> gasBottlesThreads = new ArrayList<>();
    private final Set<String> leakedGasses = new HashSet<>();

    public GaseousMaterialsCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    public GaseousMaterialsCar(String sender, double tare_weight, int max_containers) {
        super("Wagon Na Materiały Gazowe", sender, tare_weight, max_containers);
    }

    public GaseousMaterialsCar(String sender, double tare_weight, int max_containers,boolean ignoreEvent) {
        super("Wagon Na Materiały Gazowe", sender, tare_weight, max_containers, ignoreEvent);
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add gas bottle | ", containers.size() >= max_containers || isDamaged) +
                Text.disabled("2.Show gas bottles | 3.Delete gas bottle | 4.Sort gas bottles by gas name | 5.Check integrity of bottles | ", containers.size() == 0)+
                Text.disabled("6.Initiate a leak", allBottlesBrokenThrough()));

        findInputExpression("([1-6])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))) {
                case 1 -> addContainerMenu();
                case 2 -> showContainersMenu();
                case 3 -> deleteContainerMenu();
                case 4 -> sortCargoByName();
                case 5 -> checkBottlesMenu();
                case 6 -> initiateLeakMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void addContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() >= max_containers) {
            error(new BasicFreightCar.Exceptions.OutOfMaxContainersCountException("gas bottle",max_containers).getMessage());
        } else if(isDamaged) {
            error(new Exceptions.CarExplodedException().getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + GasBottle.showConstructorParameters());
            findInputExpression(GasBottle.getParametersRegExp(), (Matcher matcher) -> {
                GasBottle gasBottle = new GasBottle(matcher);
                containers.add(gasBottle);
                increaseNetWeight(gasBottle.getWeight());
                success("Gas Bottle with " + gasBottle.getCargoName(), "created");
            }, this::addContainerMenu);
        }
    }

    public void checkBottlesMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoGasBottlesAddedException().getMessage());
        } else {
            long brokenThrough = containers.stream().filter(container -> ((GasBottle) container).isBrokenThrough()).count();

            if (checkBottles()) {
                System.out.println(Text.highlighted("All gas bottles exploded!", Color.RED));
            } else if (brokenThrough > 0) {
                System.out.println(Text.highlighted(brokenThrough + " gas bottle" + (brokenThrough > 1 ? "s are" : " is") + " broken though!"));
            } else {
                System.out.println(Text.highlighted("All gas bottles are intact :)", Color.GREEN));
            }
        }
    }

    public boolean checkBottles(){
        boolean exploded = containers.stream().anyMatch(container -> ((GasBottle)container).isExploded());
        if(exploded){
            for (Container container : containers) {
                GasBottle gasBottle = ((GasBottle) container);
                gasBottle.setExploded();
                gasBottle.setFullness(0);
                reduceNetWeight(container.getWeight());
            }
        }
        return exploded;
    }

    public void initiateLeakMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoGasBottlesAddedException().getMessage());
        } else if(allBottlesBrokenThrough()){
            error(new Exceptions.AllBottlesBrokenThroughException().getMessage());
        } else {

            List<GasBottle> bottles = containers.stream().map(container -> (GasBottle) container).filter(bottle -> !bottle.isBrokenThrough()).toList();
            GasBottle gasBottle = bottles.get((int) (Math.random() * (bottles.size())));

            if(leakedGasses.add(gasBottle.getCargoName())) {
                System.out.println(Text.highlighted("Warning! A " + gasBottle.getCargoName() + " leak has occurred!"));
            }

            if(!gasBottle.isBrokenThrough()) {
                gasBottle.brakeThough();
                Thread thread = new Thread(()->{
                    try {
                        while (gasBottle.getFullness() > 0 && !gasBottle.isExploded()){
                            gasBottle.calculateFullness(Thread.currentThread());
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        if (!isDamaged){
                            if (gasBottle.isExploded()) {
                                isDamaged = true;
                                checkBottles();
                                for (Thread _thread : gasBottlesThreads) {
                                    _thread.interrupt();
                                }
                                System.out.println(Text.highlighted("Gas bottle with " + gasBottle.getCargoName() + " has been exploded!", Color.RED));
                            }
                        }
                    }
                });
                Menu.addMenuThread(thread);
                gasBottlesThreads.add(thread);
                thread.start();
            }
        }
    }

    public boolean allBottlesBrokenThrough(){
        for(Container elem : containers){
            if(!((GasBottle)elem).isBrokenThrough())
                return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return super.toString() + (isDamaged ?Text.highlighted(" is exploded!", Color.RED):"")+Color.BLACK_B;
    }

    public static String getParametersRegExp(){
        return BasicFreightCar.getParametersRegExp();
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max gas bottles count (max 80 tons)");
    }

    public static class Exceptions{
        public static class NoGasBottlesAddedException extends Exception{
            NoGasBottlesAddedException(){
                super("No gas bottles added!");
            }
        }
        public static class CarExplodedException extends Exception{
            CarExplodedException(){
                super("Train car is exploded!");
            }
        }
        public static class AllBottlesBrokenThroughException extends Exception{
            AllBottlesBrokenThroughException(){
                super("All gas bottles are broken through!");
            }
        }
    }

    public static GaseousMaterialsCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_containers = getRandomBetween(20, 45);
        GaseousMaterialsCar car = new GaseousMaterialsCar(DefaultSender, weight, max_containers, true);
        for (int i = max_containers; i > 0; i--){
            try {
                car.addContainer(GasBottle.generate());
            }catch (Exception ignored){}
        }
        return car;
    }
}