package Cars;

import BasicClasses.Locomotive;
import BasicClasses.Menu;
import CargoClasses.LiquidBottle;
import CargoClasses.ToxicLiquidsTanker;

import java.util.regex.Matcher;

public class LiquidToxicMaterialsCar extends HeavyFreightCar implements LiquidMaterialsCarInterface {
    private boolean isTightlyClosed = false;
    private final int max_toxicity_level;
    private Thread tankerThread;
    private int checkProgress = 0;
    private final double max_volume;

    public LiquidToxicMaterialsCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }

    public LiquidToxicMaterialsCar(String sender, double tare_weight, double max_volume, int max_toxicity_level) {
        super("Wagon Na Ciekłe Materiały Toksyczne", sender, tare_weight);
        this.max_volume = max_volume;
        this.max_toxicity_level = max_toxicity_level;
    }

    public LiquidToxicMaterialsCar(String sender, double tare_weight, double max_volume, int max_toxicity_level, boolean ignoreEvent) {
        super("Wagon Na Ciekłe Materiały Toksyczne", sender, tare_weight, ignoreEvent);
        this.max_volume = max_volume;
        this.max_toxicity_level = max_toxicity_level;
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        if(cargo != null && !isTightlyClosed)
            initiateLeak((LiquidBottle) this.cargo);

        System.out.println("\n"+
                Text.disabled("1.Load train car | ", isFull) +
                Text.disabled("2.Show cargo info | 3.Unload train car | ", loadPercentage <= 0) +
                "4.Check tightness | 5.Show tanker volume");

        findInputExpression("([1-5])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> loadTrainCarMenu();
                case 2 -> showCargoInfoMenu();
                case 3 -> unloadTrainCarMenu();
                case 4 -> checkTightnessMenu();
                case 5 -> showTankerVolumeMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void loadTrainCarMenu(){
        prevSection = this::MainMenu;
        System.out.println("\nEnter data in the following order: " + ToxicLiquidsTanker.showConstructorParameters());
        if(isFull){
            error(new HeavyFreightCar.Exceptions.IsFullException().getMessage());
        }
        else {
            findInputExpression(ToxicLiquidsTanker.getParametersRegExp(), (Matcher matcher) -> {
                ToxicLiquidsTanker cargo = new ToxicLiquidsTanker(matcher);
                loadTrainCar(cargo);
            }, this::loadTrainCarMenu);
        }
    }

    public void loadTrainCar(ToxicLiquidsTanker cargo) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        if(this.cargo != null && !this.cargo.getCargoName().equals(cargo.getCargoName())) {
            System.out.println(Text.highlighted("You cannot mix different toxic liquids!", Color.RED));
        }else {
            loadPercentage = 0;
            setIsTightlyClosed(false);
            loadTrainCarMenu(cargo);
            isDamaged = true;

            ToxicLiquidsTanker tanker = (ToxicLiquidsTanker)this.cargo;

            if (((tanker != null)?tanker.getVolume():0) + cargo.getVolume() > max_volume) {
                cargo.setWeight(roundDouble(cargo.getWeight() / cargo.getVolume() * (max_volume-(tanker != null?tanker.getVolume():0)), 3));
                cargo.setVolume(max_volume);
                System.out.println(Text.highlighted("Maximum volume has been exceeded, excess cargo has been removed!", Color.RED));
            }
            this.cargo = cargo;

            if (cargo.getToxicityLevel() > max_toxicity_level)
                System.out.println(Text.highlighted("Warning! The toxicity level exceeds permissible limit! It may cause a leak.", Color.RED));
            else
                System.out.println(Text.highlighted("Conduct an tightness test to avoid leakage."));

            initiateLeak((LiquidBottle) this.cargo);
            increaseNetWeight(roundDouble(cargo.getWeight() / 100 * loadPercentage, 3));

            if (((LiquidBottle) this.cargo).getVolume() >= max_volume)
                isFull = false;
        }
    }

    @Override
    public void initiateLeak(LiquidBottle liquidBottle) {
        if(tankerThread == null) {
            Thread thread = new Thread(() -> {
                try {
                    int diff = ((ToxicLiquidsTanker) cargo).getToxicityLevel() - max_toxicity_level;
                    if (diff <= 0)
                        diff = 1;
                    Thread.sleep(15000 / diff);
                    System.out.println(Text.highlighted("There's a toxin leak, conduct an urgent tightness test!", Color.RED));
                    liquidBottle.brakeThough();

                    while (liquidBottle.getFullness() > 0) {
                        ((ToxicLiquidsTanker) liquidBottle).calculateFullness();
                        updateLeakageVolume();
                        Thread.sleep(1000);
                    }
                    System.out.println(Text.highlighted("Warning! The tanker has completely drained!", Color.RED));
                } catch (InterruptedException e) {
                }
                tankerThread = null;
            });

            Menu.addMenuThread(thread);
            tankerThread = thread;
            thread.start();
        }
    }

    @Override
    public void updateLeakageVolume() {
        ToxicLiquidsTanker tanker = (ToxicLiquidsTanker)cargo;
        if(tanker.getVolume() < max_volume)
            isFull = false;

        reduceNetWeight(tanker.getWeight()/tanker.getVolume()*(tanker.getVolume()-tanker.getLeakedVolume()));
    }

    public void checkTightnessMenu() throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        prevSection = this::MainMenu;
        if(isTightlyClosed())
            System.out.println(Text.highlighted("The train car is closed very tightly :)", Color.GREEN));
        else {
            checkProgress = 0;
            System.out.println(Text.highlighted("Car leak test started!"));
            Thread thread = new Thread(()->{
               while (checkProgress < 100){
                   updateCheckProgress();
                   System.out.println(checkProgress+"%");
                   try {
                       Thread.sleep(500);
                   } catch (InterruptedException e) {
                       System.out.println(Text.highlighted("Car leak test stopped!"));
                       return;
                   }
               }
               isDamaged = false;
               tankerThread = null;
               setIsTightlyClosed(true);
               ((ToxicLiquidsTanker)this.cargo).setBrokenThrough(false);
               System.out.println(checkProgress+"%");
               success("Car leak test","completed");
               System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
            });

            increaseNetWeight(roundDouble(cargo.getWeight() / 100 * loadPercentage, 3));
            Menu.addMenuThread(thread);
            tankerThread.interrupt();
            thread.start();
            findInputExpression("/",(m)->{},()->{});
        }
    }

    public void updateCheckProgress(){
        int checkProgress = this.checkProgress + 5;
        if(checkProgress < 100){
            this.checkProgress += 5;
        }
        else
            this.checkProgress = 100;
    }

    public void showTankerVolumeMenu(){
        prevSection = this::MainMenu;
        if(cargo == null)
            System.out.println("Tanker volume: " + Text.highlighted("0%", Color.RED));
        else {
            ToxicLiquidsTanker tanker = ((ToxicLiquidsTanker) cargo);
            double volume = roundDouble( tanker.getVolume()/max_volume*100, 2);
            if (volume > 80)
                System.out.println("Tanker volume: " + Text.highlighted(volume + "%", Color.GREEN));
            else if (volume > 50)
                System.out.println("Tanker volume: " + Text.highlighted(volume + "%", Color.YELLOW));
            else
                System.out.println("Tanker volume: " + Text.highlighted(volume + "%", Color.RED));
        }
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max volume (m³) | Max toxicity level (max 10)");
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+\\.?\\d*)\\s+(\\d+)";
    }

    @Override
    public String toString() {
        return super.toString() + " | Max volume: "+max_volume+"m³ | Max toxicity level: " + max_toxicity_level + (isDamaged ? Text.highlighted(" Car is not tightly sealed!", Color.RED):"");
    }

    public synchronized boolean isTightlyClosed() {
        return isTightlyClosed;
    }

    public synchronized void setIsTightlyClosed(boolean tightlyClosed) {
        isTightlyClosed = tightlyClosed;
    }

    public static LiquidToxicMaterialsCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        double volume = roundDouble(getRandomBetween(60, 123), 2);
        int max_toxicity_level = getRandomBetween(1, 10);

        LiquidToxicMaterialsCar car = new LiquidToxicMaterialsCar(DefaultSender, weight, volume, max_toxicity_level, true);
        car.isFull = true;
        car.cargo = ToxicLiquidsTanker.generate();
        car.setNettWeight(car.cargo.getWeight());
        return car;
    }
}
