package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class ProductContainer extends Container {
    private final double min_temperature;
    private final double max_temperature;

    public ProductContainer(Matcher matcher) throws Exceptions.MinMaxException {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)));
    }

    public ProductContainer(String productName, double weight, double min_temperature, double max_temperature) throws Exceptions.MinMaxException {
        super("Product Container", productName, weight);
        if (min_temperature > max_temperature) {
            throw new Exceptions.MinMaxException();
        } else{
            this.min_temperature = min_temperature;
            this.max_temperature = max_temperature;
        }
    }

    public static String showConstructorParameters(){
        return Container.showConstructorParameters() + Menu.Text.highlighted(" | Min temperature (C°) | Max temperature (C°)");
    }

    public static String getParametersRegExp(){
        return Container.getParametersRegExp()+"\\s+(-?\\d+.?\\d*)\\s+(-?\\d+.?\\d*)";
    }

    @Override
    public String toString() {
        return super.toString()+". Temperature: min - "+min_temperature+" C°, max - "+max_temperature + " C°";
    }

    public double getMaxTemperature() {
        return max_temperature;
    }

    public double getMinTemperature() {
        return min_temperature;
    }

    public static ProductContainer generate() {
        String[] cargoNames = {"fish", "pork", "beef", "fruit", "ice", "vegetables"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(40, 400), 2);
        int min_temp = getRandomBetween(-30, -5);
        int max_temp = getRandomBetween(5, 30);
        try {
            return new ProductContainer(name, weight, min_temp, max_temp);
        }catch (Exception ignored){}
        return null;
    }

    public static class Exceptions{
        public static class MinMaxException extends Exception{
            public MinMaxException() {
                super("Min temperature cannot be greater than max temperature!");
            }
        }
    }
}
