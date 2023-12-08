package CargoClasses;

import BasicClasses.Menu;

public abstract class Bottle extends Container{
    protected int fullness = 100;
    protected boolean isBrokenThrough = false;

    public Bottle(String bottleName, String gasName, double weight) {
        super(bottleName, gasName, weight);
    }

    public void brakeThough(){
        isBrokenThrough = true;
    }

    public abstract void calculateFullness(Thread thread);

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Bottle weight");
    }

    public static String getParametersRegExp(){
        return "(\\d+.?\\d*)";
    }

    public int getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = fullness;
    }

    public boolean isBrokenThrough() {
        return isBrokenThrough;
    }

    public void setBrokenThrough(boolean bool){
        this.isBrokenThrough = true;
    }
}
