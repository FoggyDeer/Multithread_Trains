package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class ExplosiveCargo extends Cargo{
    private boolean isDamaged = false;

    public ExplosiveCargo(String cargoName, double weight, int count) {
        super(cargoName, weight, Type.Mixed, count);
    }

    public ExplosiveCargo(ExplosiveCargo cargo){
        this(cargo.cargoName, cargo.getWeight()/1000/(cargo.getCargoUnitsCount()*cargo.getCargoUnitsCount()), cargo.getCargoUnitsCount());
    }

    public ExplosiveCargo(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)));
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Cargo name | Unit weight (tons) | Number of cargo units");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+(\\d+.?\\d*)\\s+(\\d+)";
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void setDamaged(boolean damaged) {
        isDamaged = damaged;
    }

    public static ExplosiveCargo generate() {
        String[] cargoNames = {"missiles", "TNT", "RDX", "dynamite", "tetryl", "mines"};
        String name = getRandomBetween(cargoNames);
        double weight = roundDouble(getRandomBetween(3, 15), 2);
        int unitsCount = getRandomBetween(10, 20);
        return new ExplosiveCargo(name, weight, unitsCount);
    }
}
