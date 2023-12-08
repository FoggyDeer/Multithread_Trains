package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class ToxicLiquidsTanker extends LiquidBottle{
    private final int toxicity_level;

    public ToxicLiquidsTanker(String liquidName, double weight, double volume, int toxicity_level) {
        super(liquidName, weight, volume);
        this.toxicity_level = Math.min(toxicity_level, 10);
    }

    public ToxicLiquidsTanker(Matcher matcher){
        this(matcher.group(1),Double.parseDouble(matcher.group(2)),Double.parseDouble(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }

    public void calculateFullness(){
        if(isBrokenThrough) {
            fullness -= 2;

            if (fullness <= 0) {
                fullness = 0;
            }
        }
    }

    public static String showConstructorParameters(){
        return LiquidBottle.showConstructorParameters() + Menu.Text.highlighted( " | Toxicity level (max 10)");
    }

    public static String getParametersRegExp(){
        return LiquidBottle.getParametersRegExp() + "\\s+(\\d+)";
    }

    @Override
    public String toString() {
        return super.toString() + ", toxicity level: " + toxicity_level;
    }

    public int getToxicityLevel() {
        return toxicity_level;
    }

    public static ToxicLiquidsTanker generate() {
        String[] cargoNames = {"mercury", "pesticides", "fluorine", "benzene"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(40, 100), 2);
        double volume = roundDouble(getRandomBetween(70, 120), 2);
        int toxicity_level = getRandomBetween(1, 10);
        return new ToxicLiquidsTanker(name, weight, volume, toxicity_level);
    }
}
