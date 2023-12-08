package Cars;

import BasicClasses.Menu;
import CargoClasses.Cargo;

import java.util.regex.Matcher;

public class HeavyFreightCar extends Car{
    protected Cargo cargo;
    protected boolean isFull = false;
    protected int loadPercentage = 0;

    public HeavyFreightCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)));
    }

    public HeavyFreightCar(String sender, double tare_weight) {
        super("Wagon Towarowy Ciężki", sender, tare_weight);
    }

    protected HeavyFreightCar(String sender, double tare_weight, boolean ignoreEvent) {
        super("Wagon Towarowy Ciężki", sender, tare_weight, ignoreEvent);
    }

    protected HeavyFreightCar(String type, String sender, double tare_weight) {
        super(type, sender, tare_weight);
    }

    protected HeavyFreightCar(String type, String sender, double tare_weight, boolean ignoreEvent) {
        super(type, sender, tare_weight, ignoreEvent);
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Load train car | ", isFull) +
                Text.disabled("2.Show cargo info | 3.Unload train car | ", loadPercentage <= 0)+
                "4.Show load percentage");

        findInputExpression("([1-4])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> loadTrainCarMenu();
                case 2 -> showCargoInfoMenu();
                case 3 -> unloadTrainCarMenu();
                case 4 -> showLoadPercentageMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void loadTrainCarMenu(){
        prevSection = this::MainMenu;
        System.out.println("\nEnter data in the following order: " + Cargo.showConstructorParameters());
        if(isFull){
            error(new Exceptions.IsFullException().getMessage());
        }
        else {
            findInputExpression(Cargo.getParametersRegExp(), (Matcher matcher) -> {
                Cargo cargo = new Cargo(matcher);
                this.cargo = cargo;
                loadPercentage = 0;
                increaseNetWeight(roundDouble(cargo.getWeight() / 100 * loadPercentage, 3));
                loadTrainCarMenu(this.cargo);
            }, this::loadTrainCarMenu);
        }
    }

    public void loadTrainCarMenu(Cargo cargo) {
        Thread thread = new Thread(()->{
            System.out.println("Wait...");
            while (loadPercentage < 100){
                System.out.println(loadPercentage+"%");
                loadPercentage+=5;
                try {
                    Thread.sleep((int)(cargo.getWeight()/200));
                } catch (InterruptedException e) {
                    cargo.setWeight(roundDouble(cargo.getWeight()/100*loadPercentage,3));
                    setNettWeight(cargo.getWeight());
                    System.out.println(Text.highlighted("Cargo loading stopped."));
                    return;
                }
            }
            isFull = true;
            this.cargo = cargo;
            setNettWeight(cargo.getWeight());
            System.out.println(loadPercentage+"%");
            success("Cargo \""+cargo.getCargoName()+"\" (" + convertWeight(cargo.getWeight()) + ")", "loaded");
            System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
        });

        Menu.addMenuThread(thread);
        thread.start();
        findInputExpression("/",(Matcher matcher)->{},()->{});
    }

    public void showCargoInfoMenu(){
        prevSection = this::MainMenu;
        if(cargo == null){
            error(new Exceptions.NoCargoLoadedException().getMessage());
        }else {
            showCargo();
        }
    }

    @Override
    public void showCargo() {
        System.out.print(Color.BLACK_B);
        System.out.println(getCargoInfo());
        System.out.print(Color.DEFAULT);
    }

    @Override
    public String getCargoInfo() {
        return "\nCargo:\t"+cargo;
    }

    public void unloadTrainCarMenu() {
        prevSection = this::MainMenu;
        if (cargo == null || cargo.getWeight() <= 0) {
            error(new Exceptions.NoCargoLoadedException().getMessage());
        } else {
            unloadTrainCar(this.cargo);
            if (loadPercentage <= 0) {
                cargo = null;
                isFull = false;
            }
        }
    }

    public void unloadTrainCar(Cargo cargo){
        Thread thread = new Thread(()->{
            System.out.println("Wait...");
            while (loadPercentage > 0){
                System.out.println(100-loadPercentage+"%");
                loadPercentage-=5;
                reduceNetWeight(roundDouble(cargo.getWeight()/100*loadPercentage,3));
                try {
                    Thread.sleep((int)(cargo.getWeight()/200));
                } catch (InterruptedException e) {
                    cargo.setWeight(roundDouble(cargo.getWeight()/100*(100-loadPercentage),3));
                    setNettWeight(cargo.getWeight());
                    System.out.println(Text.highlighted("Cargo unloading stopped."));
                    return;
                }
            }
            setNettWeight(cargo.getWeight());
            System.out.println(100-loadPercentage+"%");
            success("Cargo \""+cargo.getCargoName()+"\" (" + convertWeight(cargo.getWeight()) + ")", "unloaded");
            System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
        });
        Menu.addMenuThread(thread);
        thread.start();
        findInputExpression("/",(Matcher matcher)->{},()->{});
    }

    public void showLoadPercentageMenu(){
        prevSection = this::MainMenu;
        System.out.println(loadPercentage+"%");
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp();
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters();
    }

    @Override
    public boolean isElectricCar() {
        return false;
    }

    public static class Exceptions{
        public static class IsFullException extends Exception{
            IsFullException(){
                super("Train carriage is full!");
            }
        }

        public static class NoCargoLoadedException extends Exception {
            NoCargoLoadedException() {
                super("Train carriage is empty!");
            }
        }
    }

    public static HeavyFreightCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        HeavyFreightCar car = new HeavyFreightCar(DefaultSender, weight, true);
        car.isFull = true;
        car.cargo = Cargo.generate();
        car.setNettWeight(car.cargo.getWeight());
        return car;
    }
}