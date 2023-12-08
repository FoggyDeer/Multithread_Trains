package Cars;

import BasicClasses.Limit;
import BasicClasses.Menu;
import BasicClasses.StaticArrayList;
import CargoClasses.Container;
import CargoClasses.ProductContainer;

import java.util.regex.Matcher;

import static java.lang.Thread.currentThread;

public class RefrigeratedCar extends BasicFreightCar {
    private final double min_temperature;
    private double currentTemperature;
    private boolean isTemperatureNormalized = false;

    public RefrigeratedCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)), Double.parseDouble(matcher.group(4)));
    }

    public RefrigeratedCar(String sender, double tare_weight, int max_containers, double min_temperature) {
        super("Wagon Chłodniczy", sender, tare_weight, max_containers);
        this.min_temperature = min_temperature;
    }

    public RefrigeratedCar(String sender, double tare_weight, int max_containers, double min_temperature, boolean ignoreEvent) {
        super("Wagon Chłodniczy", sender, tare_weight, max_containers, ignoreEvent);
        this.min_temperature = min_temperature;
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add container | ", containers.size() >= max_containers) +
                Text.disabled("2.Show containers | 3.Delete container | 4.Calculate acceptable temperature | 5.Normalize temperature", containers.size() == 0));

        findInputExpression("([1-5])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> this.addContainerMenu();
                case 2 -> showContainersMenu();
                case 3 -> deleteContainerMenu();
                case 4 -> showAcceptableTemperatureMenu();
                case 5 -> normalizeTemperatureMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    public void addContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() >= max_containers) {
            error(new BasicFreightCar.Exceptions.OutOfMaxContainersCountException("product container",max_containers).getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + ProductContainer.showConstructorParameters());
            findInputExpression(ProductContainer.getParametersRegExp(), (Matcher matcher) -> {
                ProductContainer container = new ProductContainer(matcher);
                if(containers.size() == 0 || (container.getMaxTemperature() > calculateAcceptableTemperature().getMin() &&
                        container.getMinTemperature() < calculateAcceptableTemperature().getMax())) {
                    addContainer(container);
                    success("Container with " + container.getCargoName() + " (" + convertWeight(container.getWeight()) + ")", "created");
                }else {
                    error(new RefrigeratedCar.Exceptions.OutOfTemperatureLimitBoundsException().getMessage());
                }
            }, this::addContainerMenu);
        }
    }

    public void showAcceptableTemperatureMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new BasicFreightCar.Exceptions.NoContainersAddedException().getMessage());
        } else {
            System.out.println("The most optimal temperature is: " + calculateAcceptableTemperature().getMedian()+" C°");
        }
    }

    public Limit calculateAcceptableTemperature(){
        if(containers.size() > 0) {
            StaticArrayList<Container> containers1 = new StaticArrayList<>(containers) ;

            containers1.sort((cont1, cont2) -> (int) (((ProductContainer) cont1).getMaxTemperature() - ((ProductContainer) cont2).getMaxTemperature()));
            double max = ((ProductContainer) containers1.get(0)).getMaxTemperature();

            containers1.sort((cont1, cont2) -> (int) (((ProductContainer) cont2).getMinTemperature() - ((ProductContainer) cont1).getMinTemperature()));
            double min = ((ProductContainer) containers1.get(0)).getMinTemperature();

            return new Limit(min, max);
        }
        else
            return new Limit(0, 0);
    }

    public void normalizeTemperatureMenu(){
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.CannotObserveTemperatureException().getMessage());
        } else if(isTemperatureNormalized){
            System.out.println(Text.highlighted(new Exceptions.TemperatureNormalizedException().getMessage()));
        }
        else {
            Limit limit = calculateAcceptableTemperature();
            currentTemperature = limit.getMedian();

            Thread thread = new Thread(() -> {
                boolean isCritical = false;
                Limit acceptableTemp = calculateAcceptableTemperature();
                double scale = Math.abs(limit.getMax()-limit.getMedian())/(2*Math.sqrt(Math.abs(limit.getMax()-limit.getMedian())));
                while (!isTemperatureNormalized) {
                    isCritical = changeCurrentTemperature(scale, acceptableTemp, isCritical, currentThread());
                    scale += 0.01;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                success("Temperature", "normalized");
                System.out.println("Type \"/\" or press \"Enter\" to return to previous panel");
            });
            Menu.addMenuThread(thread);
            thread.start();
            findInputExpression("/",(Matcher matcher)->{},()->{});
        }
    }

    public boolean changeCurrentTemperature(double scale, Limit acceptable, boolean isCritical, Thread thread){
        double amplitude = Math.abs(acceptable.getDistanceToCenter(currentTemperature));
        if(amplitude > (acceptable.getMax() - acceptable.getMedian())/3*2.1){
            System.out.println(Text.highlighted(currentTemperature+" C°", Color.RED));
            isCritical = true;
        }else if(amplitude > (acceptable.getMax() - acceptable.getMedian())/2.5){
            System.out.println(Text.highlighted(currentTemperature+" C°", Color.YELLOW));
        }
        else
            System.out.println(Text.highlighted(currentTemperature+" C°", Color.GREEN));
        System.out.println("Optimal temperature: "+acceptable.getMedian() + " C°");


        if(isCritical) {
            currentTemperature = roundDouble(currentTemperature+(acceptable.getDistanceToCenter(currentTemperature) / 3), 2);
            if(Math.abs(acceptable.getDistanceToCenter(currentTemperature)) < 0.2)
                currentTemperature = roundDouble(acceptable.getMedian(), 2);
        }else {
            currentTemperature = roundDouble(currentTemperature+(Math.random() < 0.5 ? -1 : 1)*Math.random()*scale, 2);
        }

        if(acceptable.getMedian() == currentTemperature && isCritical) {
            isTemperatureNormalized = true;
            isCritical = false;
        }

        return isCritical;
    }

    @Override
    public String toString(){
        return super.toString() + " | Min temperature: " + min_temperature;
    }

    public static String getParametersRegExp(){
        return BasicFreightCar.getParametersRegExp() + "\\s+(-?\\d+\\.?\\d*)";
    }

    public static String showConstructorParameters(){
        return BasicFreightCar.getConstructorParameters() + Text.highlighted(" | Min temperature");
    }

    @Override
    public boolean isElectricCar() {
        return true;
    }

    public static class Exceptions{

        public static class OutOfTemperatureLimitBoundsException extends Exception {
            OutOfTemperatureLimitBoundsException() {
                super("Container temperature limits are not compatible with added containers!");
            }
        }
        public static class CannotObserveTemperatureException extends Exception {
            CannotObserveTemperatureException() {
                super("You cannot observe temperature of empty car!");
            }
        }

        public static class TemperatureNormalizedException extends Exception {
            TemperatureNormalizedException() {
                super("Temperature is already normalized.");
            }
        }
    }

    public static RefrigeratedCar generate(){
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_containers = getRandomBetween(20, 45);
        int min_temperature = -40;
        RefrigeratedCar car = new RefrigeratedCar(DefaultSender, weight, max_containers, min_temperature, true);
        for(int i = car.max_containers; i > 0; i--){
            try {
                car.addContainer(ProductContainer.generate());
            }catch (Exception ex){
                i++;
            }
        }
        return car;
    }

}
