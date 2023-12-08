package Cars;

import BasicClasses.Locomotive;
import BasicClasses.Menu;
import CargoClasses.Cargo;
import CargoClasses.ToxicCargo;

import java.util.regex.Matcher;

public class ToxicMaterialsCar extends HeavyFreightCar{
    private int max_toxicity_level;
    private double integrity_level = 100;
    private Thread corrosionThread;
    private boolean isRepairing = false;


    public ToxicMaterialsCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    public ToxicMaterialsCar(String sender, double tare_weight, int max_toxicity_level) {
        super("Wagon Na Materiały Toksyczne", sender, tare_weight);
        this.max_toxicity_level = Math.min(max_toxicity_level, 10);
    }

    public ToxicMaterialsCar(String sender, double tare_weight, int max_toxicity_level, boolean ignoreEvent) {
        super("Wagon Na Materiały Toksyczne", sender, tare_weight, ignoreEvent);
        this.max_toxicity_level = Math.min(max_toxicity_level, 10);
    }

    @Override
    public void exit() {
        if(corrosionThread != null)
            corrosionThread.interrupt();
        super.exit();
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Load train car | ", isFull) +
                Text.disabled("2.Show cargo info | 3.Unload train car | ", loadPercentage <= 0) +
                "4.Show load percentage" +
                Text.disabled(" | 5.Fix and upgrade damaged walls | ", getIntegrityLevel() == 100 || isDamaged) +
                "6.Show integrity level");

        findInputExpression("([1-6])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> loadTrainCarMenu();
                case 2 -> showCargoInfoMenu();
                case 3 -> unloadTrainCarMenu();
                case 4 -> showLoadPercentageMenu();
                case 5 -> repairWallsMenu();
                case 6 -> showIntegrityLevelMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void loadTrainCarMenu(){
        prevSection = this::MainMenu;
        System.out.println("\nEnter data in the following order: " + ToxicCargo.showConstructorParameters());
        if(isFull){
            error(new HeavyFreightCar.Exceptions.IsFullException().getMessage());
        }
        else {
            findInputExpression(ToxicCargo.getParametersRegExp(), (Matcher matcher) -> {
                ToxicCargo cargo = new ToxicCargo(matcher);
                loadTrainCar(cargo);
            }, this::loadTrainCarMenu);
        }
    }

    public void loadTrainCar(ToxicCargo cargo) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        this.cargo = cargo;
        loadPercentage = 0;
        if(max_toxicity_level < cargo.getToxicityLevel())
            System.out.println(Text.highlighted("Warning! The toxicity level exceeds permissible limit! Car has started to deteriorate."));
        corrosion();
        loadTrainCarMenu(this.cargo);
        increaseNetWeight(roundDouble(cargo.getWeight() / 100 * loadPercentage, 3));
    }

    public void corrosion(){
        ToxicCargo toxicCargo = ((ToxicCargo)cargo);
        if(toxicCargo.getToxicityLevel() > max_toxicity_level){
            int diff = toxicCargo.getToxicityLevel() - max_toxicity_level;
            double corrosion_force = (1 + diff/10.0+Math.pow(diff, 2)/(Math.pow(10, 2))+Math.pow(diff, 3)/(Math.pow(10, 3))+Math.pow(diff, 4)/(Math.pow(10,4)))/4;

            Thread thread = new Thread(corrosionThread(corrosion_force));

            corrosionThread = thread;
            thread.start();
        }
    }

    public Runnable corrosionThread(double corrosion_force){
        return () -> {
            while (getIntegrityLevel() > 0){
                if(!isRepairing())
                    updateIntegrity(corrosion_force);
                if(cargo == null)
                    Thread.currentThread().interrupt();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
            System.out.println(Text.highlighted("Walls of the car are completely destroyed", Color.RED));
        };
    }

    public void updateIntegrity(double force){
        double damage_level = getIntegrityLevel() - force;
        if(damage_level > 0) {
            setIntegrityLevel(damage_level);
            if(damage_level < 60 && !isDamaged) {
                isDamaged = true;
                System.out.println(Text.highlighted("The level of damage is critical - car is not repairable!", Color.RED));
            }
        }
        else
            setIntegrityLevel(0);
    }

    public void repairWallsMenu(){
        prevSection = this::MainMenu;
        if(isDamaged){
            error(new Exceptions.CarIsTooDamagedException().getMessage());
        }
        else if(getIntegrityLevel() >= 100){
            System.out.println(Text.highlighted("The car is not damaged :)", Color.GREEN));
        }
        else {
            System.out.println(Text.highlighted("Repairing started.", Color.YELLOW));
            max_toxicity_level = ((ToxicCargo)cargo).getToxicityLevel();
            setRepairing(true);
            System.out.println("Integrity level: ");

            Thread thread = new Thread(()->{
                while (getIntegrityLevel() < 100){
                    System.out.println(getIntegrityLevel()+"%");
                    updateRepairLevel();
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        setRepairing(false);
                        System.out.println(Text.highlighted("Repair has been stopped, corrosion will continue!"));
                        return;
                    }
                }
                System.out.println(getIntegrityLevel()+"%");
                setRepairing(false);
                corrosionThread.interrupt();
                success("Car", "repaired");
                System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
            });

            Menu.addMenuThread(thread);
            thread.start();
            findInputExpression("/", (m)->{}, ()->{});
        }
    }

    public void updateRepairLevel(){
        double integrityLevel = getIntegrityLevel()+2;
        if(integrityLevel < 100) {
            setIntegrityLevel(integrityLevel);
        }
        else
            setIntegrityLevel(100);
    }

    public void showIntegrityLevelMenu(){
        prevSection = this::MainMenu;
        if(getIntegrityLevel() > 80)
            System.out.println(Text.highlighted("Integrity level: "+getIntegrityLevel()+"%", Color.GREEN));
        else if(getIntegrityLevel() > 60)
            System.out.println(Text.highlighted("Integrity level: "+getIntegrityLevel()+"%", Color.YELLOW));
        else
            System.out.println(Text.highlighted("Integrity level: "+getIntegrityLevel()+"%", Color.RED));
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max toxicity level (max 10)");
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+)";
    }

    @Override
    public String toString() {
        return super.toString() + " | Max toxicity level: " + max_toxicity_level + (isDamaged ? Text.highlighted(" walls are damaged!", Color.RED):"")+Color.BLACK_B;
    }

    public synchronized double getIntegrityLevel() {
        return integrity_level;
    }

    public synchronized boolean isRepairing() {
        return isRepairing;
    }

    public synchronized void setRepairing(boolean repairing) {
        isRepairing = repairing;
    }

    public synchronized void setIntegrityLevel(double value) {
        this.integrity_level = roundDouble(value, 2);
    }

    public static class Exceptions{
        public static class CarIsTooDamagedException extends Exception{
            CarIsTooDamagedException(){
                super("The level of damage is critical - car is not repairable!");
            }
        }
    }

    public static ToxicMaterialsCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_toxicity_level = getRandomBetween(1, 10);
        ToxicMaterialsCar car = new ToxicMaterialsCar(DefaultSender, weight, max_toxicity_level, true);
        car.isFull = true;
        car.cargo = Cargo.generate();
        car.setNettWeight(car.cargo.getWeight());
        return car;
    }
}
