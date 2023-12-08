package Cars;

import BasicClasses.*;
import org.jetbrains.annotations.NotNull;

public abstract class Car extends MenuElement implements CarType, Comparable<Car>{
    public static final String DefaultSender = "PJATK";
    private final String sender;
    private final String type;
    private double nett_weight; //Weight without package
    private final double tare_weight; //Weight of package
    protected boolean isDamaged = false;

    protected Function lastMenuReferenceFunction;

    private final int id;
    private static int count;

    private Locomotive locomotive;

    public Car(String type, String sender, double tare_weight) {
        this(type, sender, tare_weight, false);
    }

    public Car(String type, String sender, double tare_weight, boolean ignoreEvent) {
        this.sender = sender;
        this.tare_weight = tare_weight*1000;
        this.type = type;
        this.nett_weight = 0;
        id = count++;
        if(!ignoreEvent)
            InteractiveMenu.success(this.type, "created");
    }

    @Override
    public void exit() {
        Menu.closeMenuThreads();
        prevSection = lastMenuReferenceFunction;
    }

    @Override
    public void interact(Function function) {
        lastMenuReferenceFunction = function;
        MainMenu();
    }

    public abstract void showCargo();

    public abstract String getCargoInfo();

    public double getGrossWeight() {
        return nett_weight + tare_weight;
    }

    public String getType() {
        return type;
    }

    public static String getConstructorParameters(){
        return Text.highlighted("Sender | Tare weight (tons)");
    }

    @Override
    public  String toString(){
        return "#" + id + " Typ: " + type + " | Sender: " + sender + " | Net weight: " + convertWeight(nett_weight) + " | Tare weight: " + convertWeight(tare_weight);
    }

    public static String getParametersRegExp(){
        return "(\\w+)\\s+(\\d+)";
    }

    public boolean isAttachedToTrain() {
        return locomotive != null;
    }

    public void attachToTrain(Locomotive locomotive) throws Exceptions.IsAlreadyAttachedException {
        if(isAttachedToTrain()) {
            throw new Exceptions.IsAlreadyAttachedException();
        }
        else
            this.locomotive = locomotive;
    }

    public void detachFromTrain() throws Exceptions.IsNotAttachedException {
        if(isAttachedToTrain()) {
            this.locomotive = null;
        }
        else
            throw new Exceptions.IsNotAttachedException();
    }

    public void increaseNetWeight(double additionalWeight) throws Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight{
        if(locomotive == null){
            nett_weight = roundDouble(nett_weight+additionalWeight, 3);
        }
        else if(locomotive.isOnTheWay()){
            throw new Exceptions.CarIsOnTheWayException();
        } else if (locomotive.getCurrentPullWeight() + additionalWeight <= locomotive.getMaxPullWeight()){
            throw new Locomotive.Exceptions.OutOfMaxPullWeight(locomotive.getMaxPullWeight());
        }
        else {
            nett_weight = roundDouble(nett_weight+additionalWeight, 3);
        }
    }

    public void reduceNetWeight(double weight){
        nett_weight = roundDouble(nett_weight-weight, 3);
        if(nett_weight < 0)
            nett_weight = 0;
    }

    public void setNettWeight(double weight){
        nett_weight = roundDouble(weight,3);
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(@NotNull Car car) {
        return sumBoolean(this.isDamaged, this.isAttachedToTrain()) - sumBoolean(car.isDamaged(), car.isAttachedToTrain());
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public static class Exceptions{
        public static class IsAlreadyAttachedException extends Exception {
            public IsAlreadyAttachedException() {
                super("Car is already attached to train!");
            }
        }
        public static class IsNotAttachedException extends Exception {
            public IsNotAttachedException() {
                super("Car is not attached to train!");
            }
        }
        public static class CarIsOnTheWayException extends Exception{
            public CarIsOnTheWayException(){
                super("Train car load cannot be changed during the journey!");
            }
        }
    }
}

