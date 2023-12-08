package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static BasicClasses.Menu.*;

public class Cargo {
    protected final String cargoName;
    private double weight;
    private final Type type;
    private final int cargoUnitsCount;

    public enum Type{
        Uniform("Uniform"),
        Mixed("Mixed");

        final String type;
        Type(String str){
            type = str;
        }

        static Type parseType(String str){
            if (Pattern.compile("Mixed").matcher(str).find())
                return Mixed;
            return Uniform;
        }
    }

    public Cargo(String cargoName, double weight){
        this.cargoName = cargoName;
        this.weight = weight;
        this.type = Type.Uniform;
        this.cargoUnitsCount = 1;
    }

    public Cargo(String cargoName, double weight, Type type, int count){
        this.cargoName = cargoName;
        this.weight = weight*count*1000;
        this.type = type;
        this.cargoUnitsCount = count;
    }

    public Cargo(Matcher matcher){
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Type.parseType(matcher.group(3)), (matcher.groupCount() == 4 && matcher.group(4) != null && matcher.group(4).length()>0)? Integer.parseInt(matcher.group(4)) : 1);
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlightedPattern("/Y/Cargo name | Weight (tons) | Cargo type (://P/Uniform or Mixed://Y/) | (Number of cargo units, if Mixed):/");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+(\\d+.?\\d*)\\s+(Uniform|Mixed\\s+(\\d+))";
    }

    public static String showShortConstructorParameters(){
        return Menu.Text.highlighted("Cargo name | Weight (tons)");
    }

    public static String getShortParametersRegExp(){
        return "([a-zA-Z]+)\\s+(\\d+.?\\d*)";
    }

    @Override
    public String toString() {
        return cargoName+" ("+convertWeight(weight)+")";
    }

    public String getCargoName(){
        return cargoName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double val) {
        this.weight = val;
    }

    public int getCargoUnitsCount() {
        return cargoUnitsCount;
    }

    public Type getType() {
        return type;
    }

    public static Cargo generate() {
        String[] cargoNames = {"copper ore", "stones", "iron ore", "steel beams", "sand", "coal", "cars", "tanks"};
        String name = getRandomBetween(cargoNames);
        Type type = (Type) getRandomBetween(Type.Uniform, Type.Mixed);
        double weight = roundDouble(getRandomBetween(3, 15), 2);
        int cargoUnits = 1;
        if(type == Type.Mixed){
            cargoUnits = getRandomBetween(1, 10);
        }
        return new Cargo(name, weight, type, cargoUnits);
    }
}
