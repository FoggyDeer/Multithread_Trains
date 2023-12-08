package Cars;

import BasicClasses.Locomotive;
import CargoClasses.Cargo;
import CargoClasses.ExplosiveCargo;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class ExplosiveMaterialsCar extends HeavyFreightCar{
    private final ArrayList<ExplosiveCargo> explosiveCargos = new ArrayList<>();
    private boolean isNeutralized = false;
    private boolean damageCargoDetected = false;

    public ExplosiveMaterialsCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)));
    }

    public ExplosiveMaterialsCar(String sender, double tare_weight) {
        super("Wagon Na Materiały Wybuchowe", sender, tare_weight);
    }

    public ExplosiveMaterialsCar(String sender, double tare_weight, boolean ignoreEvent) {
        super("Wagon Na Materiały Wybuchowe", sender, tare_weight, ignoreEvent);
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Load train car | ", isFull) +
                Text.disabled("2.Show cargo info | 3.Unload train car | ", loadPercentage <= 0) +
                "4.Show load percentage | " +
                Text.disabled("5.Find damaged cargo | ", cargo == null || cargo.getType() == Cargo.Type.Uniform)+
                Text.disabled("6.Neutralize damaged cargo", cargo == null || cargo.getType() == Cargo.Type.Uniform || isNeutralized || !damageCargoDetected));

        findInputExpression("([1-6])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> loadTrainCarMenu();
                case 2 -> showCargoInfoMenu();
                case 3 -> unloadTrainCarMenu();
                case 4 -> showLoadPercentageMenu();
                case 5 -> findDamageCargoMenu();
                case 6 -> neutralizeDamagedCargoMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void loadTrainCarMenu(){
        prevSection = this::MainMenu;
        System.out.println("\nEnter data in the following order: " + ExplosiveCargo.showConstructorParameters());
        if(isFull){
            error(new HeavyFreightCar.Exceptions.IsFullException().getMessage());
        }
        else {
            findInputExpression(ExplosiveCargo.getParametersRegExp(), (Matcher matcher) -> {
                this.cargo = new ExplosiveCargo(matcher);
                loadPercentage = 0;
                isNeutralized = false;
                damageCargoDetected = false;

                loadTrainCar(this.cargo);
            }, this::loadTrainCarMenu);
        }
    }

    public void loadTrainCar(Cargo cargo) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        loadTrainCarMenu(cargo);
        increaseNetWeight(roundDouble(cargo.getWeight() / 100 * loadPercentage, 3));
        isFull = true;

        for(int i = 0; i < cargo.getCargoUnitsCount(); i++){
            ExplosiveCargo c = new ExplosiveCargo((ExplosiveCargo) cargo);
            if(Math.random() < 0.10)
                c.setDamaged(true);
            explosiveCargos.add(c);
        }
        explosiveCargos.get((int)(Math.random() * explosiveCargos.size())).setDamaged(true);
    }

    public void findDamageCargoMenu() {
        prevSection = this::MainMenu;

        if(cargo == null){
            error(new HeavyFreightCar.Exceptions.NoCargoLoadedException().getMessage());
        }
        else if(cargo.getType() == Cargo.Type.Uniform){
            error(new Exceptions.IsNotExplosiveException().getMessage());
        }
        else {
            long damaged = explosiveCargos.stream().filter(cargo -> cargo.isDamaged()).count();
            StringBuilder sb = new StringBuilder("Objects ");
            for (int i = 0; i < explosiveCargos.size(); i++) {
                if (explosiveCargos.get(i).isDamaged())
                    sb.append("№").append(i).append(" ").append(explosiveCargos.get(i)).append(", ");
            }
            if (damaged > 0) {
                System.out.println(Text.highlighted(sb.substring(0, sb.length() - 2) + (damaged == 1 ? " was damaged!" : " were damaged!"), Color.RED));
                damageCargoDetected = true;
            } else
                System.out.println(Text.highlighted("Every cargo is intact :)", Color.GREEN));
        }
    }

    public void neutralizeDamagedCargoMenu(){
        prevSection = this::MainMenu;
        if(cargo == null){
            error(new HeavyFreightCar.Exceptions.NoCargoLoadedException().getMessage());
        }
        else if(cargo.getType() == Cargo.Type.Uniform){
            error(new Exceptions.IsNotExplosiveException().getMessage());
        }
        else if(isNeutralized){
            error(new Exceptions.IsNeutralizedException().getMessage());
        }

        else if(!damageCargoDetected){
            error(new Exceptions.DamageNotDetectedException().getMessage());
        }
        else {
            Thread thread = new Thread(()->{
                int progress = 0;
                while (progress < 100){
                    System.out.println(progress+"%");
                    progress += 5;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println(Text.highlighted("Cargo is not neutralized!"));
                        return;
                    }
                }
                System.out.println(progress+"%");

                for(ExplosiveCargo explosiveCargo : explosiveCargos){
                    explosiveCargo.setDamaged(false);
                }
                isNeutralized = true;
                success("Damaged cargo \"" + cargo.getCargoName()+"\"", "neutralized");
                System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
            });
            thread.start();
            findInputExpression("/", (m)->{}, ()->{});
        }
    }

    public static class Exceptions{
        public static class IsNotExplosiveException extends Exception{
            IsNotExplosiveException(){
                super("Cargo is not explosive!");
            }
        }
        public static class IsNeutralizedException extends Exception {
            IsNeutralizedException() {
                super("Cargo is already neutralized!");
            }
        }
        public static class DamageNotDetectedException extends Exception {
            DamageNotDetectedException() {
                super("Damaged cargo is not detected!");
            }
        }
    }

    public static ExplosiveMaterialsCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        ExplosiveMaterialsCar car = new ExplosiveMaterialsCar(DefaultSender, weight, true);
        car.isFull = true;
        car.cargo = Cargo.generate();
        car.setNettWeight(car.cargo.getWeight());
        return car;
    }
}