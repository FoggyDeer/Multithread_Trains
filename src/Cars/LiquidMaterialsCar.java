package Cars;

import BasicClasses.Menu;
import CargoClasses.Container;
import CargoClasses.LiquidBottle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class LiquidMaterialsCar extends BasicFreightCar implements LiquidMaterialsCarInterface {
    private final Map<LiquidBottle,Thread> liquidBottlesThreads = new HashMap<>();
    private double humidityLevel = 40;
    private boolean tooMuchMoisture = false;
    private boolean isVentilating = false;
    private double leakageVolume = 0;

    public LiquidMaterialsCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    public LiquidMaterialsCar(String sender, double tare_weight, int max_containers) {
        super("Wagon Na Materiały Ciekłe", sender, tare_weight, max_containers);
    }

    public LiquidMaterialsCar(String sender, double tare_weight, int max_containers, boolean ignoreEvent) {
        super("Wagon Na Materiały Ciekłe", sender, tare_weight, max_containers, ignoreEvent);
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add liquid bottle | ", containers.size() >= max_containers || tooMuchMoisture) +
                Text.disabled("2.Show bottles | 3.Delete bottle | 4.Sort bottles by liquid name | 5.Check bottles integrity | ", containers.size() == 0)+
                Text.disabled("6.Initiate a leak | ", allBottlesBrokenThrough())+
                "7.Ventilate the car | 8.Show humidity level | 9.Show leakage volume");

        findInputExpression("([1-9])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))) {
                case 1 -> addContainerMenu();
                case 2 -> showContainersMenu();
                case 3 -> deleteContainerMenu();
                case 4 -> sortCargoByName();
                case 5 -> checkBottlesMenu();
                case 6 -> initiateLeakMenu();
                case 7 -> ventilateCarMenu();
                case 8 -> showHumidityLevelMenu();
                case 9 -> showLeakageVolumeMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void addContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() >= max_containers) {
            error(new BasicFreightCar.Exceptions.OutOfMaxContainersCountException("liquid bottle",max_containers).getMessage());
        } else if(getHumidityLevel() > 65) {
            error(new Exceptions.HumidityExcessException().getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + LiquidBottle.showConstructorParameters());
            findInputExpression(LiquidBottle.getParametersRegExp(), (Matcher matcher) -> {
                LiquidBottle liquidBottle = new LiquidBottle(matcher);
                containers.add(liquidBottle);
                increaseNetWeight(liquidBottle.getWeight());
                success("Liquid Bottle with " + liquidBottle.getCargoName(), "created");
            }, this::addContainerMenu);
        }
    }

    public void deleteContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoLiquidBottlesAddedException().getMessage());
        } else {
            System.out.print("Select one Liquid Bottle from the list:\n");
            showContainersMenu();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= containers.size()){
                    Container container = containers.remove(option-1);
                    reduceNetWeight(container.getWeight());
                    if(liquidBottlesThreads.get((LiquidBottle) container) != null) {
                        liquidBottlesThreads.get((LiquidBottle) container).interrupt();
                        liquidBottlesThreads.remove((LiquidBottle) container);
                    }
                    success(container.getName()+" with "+container.getCargoName()+" ("+ convertWeight(container.getWeight())+")", "removed");
                }
                else
                    throw new WrongInputException();
            }, this::deleteContainerMenu);
        }
    }

    public void checkBottlesMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoLiquidBottlesAddedException().getMessage());
        } else {
            ArrayList<Container> brokenThrough = new ArrayList<>(containers);
            StringBuilder sb =new StringBuilder();
            int brokenThroughCount = 0;
            for(Container elem : brokenThrough){
                if(((LiquidBottle)elem).isBrokenThrough()) {
                    sb.append(((LiquidBottle) elem).getLeakedVolume()).append(" of ").append(elem.getCargoName()).append(", ");
                    brokenThroughCount++;
                }
            }
            if (brokenThroughCount > 0) {
                System.out.println(Text.highlighted(brokenThrough.size() + " liquid bottle" + (brokenThroughCount > 1 ? "s are" : " is") + " broken though! Leaked "+ sb.substring(0, sb.length()-2) + "!"));
            } else {
                System.out.println(Text.highlighted("All liquid bottles are intact :)", Color.GREEN));
            }
        }
    }

    public void initiateLeakMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new GaseousMaterialsCar.Exceptions.NoGasBottlesAddedException().getMessage());
        } else if(allBottlesBrokenThrough()){
            error(new  Exceptions.AllBottlesBrokenThroughException().getMessage());
        }
        else {
            List<LiquidBottle> bottles = containers.stream().map(container -> (LiquidBottle) container).filter(bottle -> !bottle.isBrokenThrough()).toList();
            LiquidBottle liquidBottle = bottles.get((int) (Math.random() * (bottles.size())));

            if(!liquidBottle.isBrokenThrough()) {
                liquidBottle.brakeThough();
                initiateLeak(liquidBottle);
            }
            updateHumidityLevel();
        }
    }

    public boolean allBottlesBrokenThrough(){
        for(Container elem : containers){
            if(!((LiquidBottle)elem).isBrokenThrough())
                return false;
        }
        return true;
    }

    public void ventilateCarMenu() {
        prevSection = this::MainMenu;
        if(!isVentilating) {
            Thread thread = new Thread(() -> {
                setIsVentilating(true);
                while (getHumidityLevel() > 40 || getLeakageVolume() > 0) {
                    if (getHumidityLevel() > 40)
                        setHumidityLevel(getHumidityLevel() - 1);

                    if (getLeakageVolume() > 0)
                        setLeakageVolume(getLeakageVolume() - 1);

                    if(humidityLevel <= 60) {
                        isDamaged = false;
                        tooMuchMoisture = false;
                    }
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        if (isVentilating()) setIsVentilating(false);
                        return;
                    }
                }
                setIsVentilating(false);
                System.out.println(Text.highlighted("Ventilation is completed.", Color.GREEN));
            });
            System.out.println(Text.highlighted("Ventilation started."));
            Menu.addMenuThread(thread);
            thread.start();
        }
    }

    public void initiateLeak(LiquidBottle liquidBottle){
        Thread thread1 = new Thread(()->{
            while (liquidBottle.getFullness() > 0){

                liquidBottle.calculateFullness(Thread.currentThread());
                updateLeakageVolume();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if(liquidBottle.getVolume() == 0)
                System.out.println(Text.highlighted("Warning! The " + liquidBottle.getCargoName() + " has completely drained from the bottle!"));
        });
        Menu.addMenuThread(thread1);
        liquidBottlesThreads.put(liquidBottle,thread1);
        thread1.start();
    }

    public void updateLeakageVolume(){
        double leakageVolume = 0;
        for(Container elem : containers){
            leakageVolume += ((LiquidBottle)elem).getRecentlyLeakedVolume();
        }
        setLeakageVolume(getLeakageVolume()+leakageVolume);
    }

    public void updateHumidityLevel(){
        Thread thread2 = new Thread(() -> {
            while (getHumidityLevel() < 100){
                if(liquidBottlesThreads.size() == 0 && getLeakageVolume() <= 0)
                    return;

                setHumidityLevel(getHumidityLevel()+0.1);
                if(getHumidityLevel() > 60) {
                    isDamaged = true;
                    tooMuchMoisture = true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if(!isVentilating && getHumidityLevel() >= 100) System.out.println(Text.highlighted("The moisture level has reached a critical point!", Color.RED));
        });
        Menu.addMenuThread(thread2);
        thread2.start();
    }

    public void showHumidityLevelMenu(){
        prevSection = this::MainMenu;
        System.out.print("Humidity level is ");
        if(getHumidityLevel() > 80)
            System.out.print(Text.highlighted(getHumidityLevel()+"%", Color.RED));
        else if(getHumidityLevel() > 60)
            System.out.print(Text.highlighted(getHumidityLevel()+"%", Color.YELLOW));
        else
            System.out.print(Text.highlighted(getHumidityLevel()+"%", Color.GREEN));
        System.out.println();
    }

    public void showLeakageVolumeMenu(){
        prevSection = this::MainMenu;
        if(leakageVolume == 0)
            System.out.println(Text.highlighted(convertVolume(leakageVolume), Color.GREEN));
        else
            System.out.println(Text.highlighted(convertVolume(leakageVolume),Color.RED));
    }

    public static String getParametersRegExp(){
        return BasicFreightCar.getParametersRegExp();
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max liquid bottles count (max 80 tons)");
    }

    @Override
    public String toString() {
        return super.toString() + (tooMuchMoisture?Text.highlighted(" high humidity level", Color.RED):"")+Color.BLACK_B;
    }

    public synchronized double getHumidityLevel() {
        return humidityLevel;
    }

    public synchronized void setHumidityLevel(double value) {
        this.humidityLevel = roundDouble(value, 2);
    }

    public synchronized boolean isVentilating(){
        return isVentilating;
    }

    public synchronized void setIsVentilating(boolean value){
        this.isVentilating = value;
    }

    public synchronized double getLeakageVolume() {
        return leakageVolume;
    }

    public synchronized void setLeakageVolume(double leakageVolume) {
        this.leakageVolume = leakageVolume > 0 ? roundDouble(leakageVolume, 3) : 0;
    }

    public static class Exceptions{
        public static class NoLiquidBottlesAddedException extends Exception{
            NoLiquidBottlesAddedException(){
                super("No liquid bottles added!");
            }
        }
        public static class HumidityExcessException extends Exception{
            HumidityExcessException(){
                super("Too much moisture! You must ventilate the train car!");
            }
        }
        public static class AllBottlesBrokenThroughException extends Exception{
            AllBottlesBrokenThroughException(){
                super("All liquid bottles are broken through!");
            }
        }
    }

    public static LiquidMaterialsCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_containers = getRandomBetween(20, 45);
        LiquidMaterialsCar car = new LiquidMaterialsCar(DefaultSender, weight, max_containers, true);
        for (int i = max_containers; i > 0; i--){
            try {
                car.addContainer(LiquidBottle.generate());
            }catch (Exception ignored){}
        }
        return car;
    }
}
