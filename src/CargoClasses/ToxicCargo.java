package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class ToxicCargo extends Cargo{
    private final int toxicity_level;

    public ToxicCargo(Matcher matcher){
        this(matcher.group(1),Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    public ToxicCargo(String cargoName, double weight, int toxicity_level){
        super(cargoName, weight, Type.Uniform,1);
        this.toxicity_level = Math.min(toxicity_level, 10);
    }

    public static String showConstructorParameters(){
        return Cargo.showShortConstructorParameters() + Menu.Text.highlighted( " | Toxicity level (max 10)");
    }

    public static String getParametersRegExp(){
        return Cargo.getShortParametersRegExp() + "\\s+(\\d+)";
    }

    @Override
    public String toString() {
        return super.toString() + ", toxicity level: " + toxicity_level;
    }

    public int getToxicityLevel() {
        return toxicity_level;
    }

    public static ToxicCargo generate() {
        String[] cargoNames = {"arsenic", "cadmium", "asbestos", "lead"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(3, 15), 2);
        int toxicity_level = getRandomBetween(1, 10);

        return new ToxicCargo(name, weight, toxicity_level);
    }
}
