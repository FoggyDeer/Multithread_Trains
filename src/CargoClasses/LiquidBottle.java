package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.*;

public class LiquidBottle extends Bottle{
    private double lastCheckedVolume;
    private double volume;
    private final int id;
    private static int count;

    public LiquidBottle(String liquidName, double weight, double volume) {
        super("Liquid bottle", liquidName, weight*volume);
        this.volume = volume;
        lastCheckedVolume = volume;
        id = ++count;
    }

    public LiquidBottle(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)));
    }

    public void calculateFullness(Thread thread){
        if(isBrokenThrough) {
            fullness -= 2;

            if (fullness <= 0) {
                fullness = 0;
                thread.interrupt();
            }
        }
    }

    public double getLeakedVolume(){
        double volume = this.volume/100*fullness;
        if(volume < 0)
            volume = 0;
        return roundDouble(this.volume - volume, 3);
    }

    public double getRecentlyLeakedVolume(){
        double buff = lastCheckedVolume - this.volume/100*fullness;
        lastCheckedVolume -= buff;
        return roundDouble(buff, 3);
    }

    @Override
    public String toString() {
        return "Liquid: " + cargoName + " " + fullness +"% (" + convertVolume(getVolume()) + ")"+ (isBrokenThrough? Menu.Text.highlighted(" is broken though!"):"")+ Menu.Color.BLACK_B;
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Liquid name | Weight per m³ (kg) | Volume (m³)");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+(\\d+.?\\d*)\\s+(\\d+.?\\d*)";
    }

    public double getVolume() {
        return roundDouble(volume/100*fullness, 3);
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LiquidBottle))
            return false;
        return ((LiquidBottle) obj).getId() == this.id;
    }

    @Override
    public int hashCode() {
        return ((Integer)id).hashCode();
    }

    public int getId() {
        return id;
    }

    public static LiquidBottle generate() {
        String[] cargoNames = {"cryogenic liquid", "oil", "mineral water"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(40, 100), 2);
        double volume = roundDouble(getRandomBetween(70, 120), 2);
        return new LiquidBottle(name, weight, volume);
    }
}
