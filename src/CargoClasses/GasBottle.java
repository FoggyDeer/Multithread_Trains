package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.*;
import static BasicClasses.Menu.getRandomBetween;

public class GasBottle extends Bottle{
    private boolean isExploded = false;

    public GasBottle(String gasName, double weight) {
        super("Gas bottle", gasName, weight);
    }

    public GasBottle(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)));
    }

    public void calculateFullness(Thread thread){
        if(isBrokenThrough) {
            if (Math.random() > 0.09)
                fullness -= 6;
            else {
                isExploded = true;
            }

            if (fullness <= 0 || isExploded) {
                fullness = 0;
                thread.interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return "Gas: " + cargoName + " " + fullness +"% (" + convertWeight(getWeight()) + ")"+ (isExploded?Menu.Text.highlighted(" is exploded!", Menu.Color.RED):"")+ Menu.Color.BLACK_B;
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Gas name | ")+Bottle.showConstructorParameters();
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+"+Bottle.getParametersRegExp();
    }

    synchronized public boolean isExploded() {
        return isExploded;
    }

    public void setExploded() {
        isExploded = true;
    }

    public static GasBottle generate() {
        String[] cargoNames = {"nitrogen", "acetylene", "propane", "butane", "krypton", "ozone", "ethylene"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(40, 100), 2);
        return new GasBottle(name, weight);
    }
}
