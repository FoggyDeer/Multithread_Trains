package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class Container extends Cargo{
    private final String name;
    public Container(String name, String cargoName, double weight) {
        super(cargoName, weight);
        this.name = name;
    }

    public Container(Matcher matcher){
        this("Container", matcher.group(1), Double.parseDouble(matcher.group(2)));
    }

    public String getCargoName() {
        return cargoName;
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Product name (")+ Menu.Text.highlighted("\"...\"", Menu.Color.PURPLE)+Menu.Text.highlighted(") | Container weight (kg)");
    }

    public static String getParametersRegExp(){
        return "(\".+\")\\s+(\\d+.?\\d*)";
    }

    public String getName() {
        return name;
    }

    public static Container generate() {
        String[] cargoNames = {"plastics", "paper", "textiles", "fertilizers", "wheat grains", "buckwheat grains", "car parts"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(3, 15), 2);
        return new Container("Container", name, weight);
    }
}
