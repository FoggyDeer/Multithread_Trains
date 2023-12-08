package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.*;
import static BasicClasses.Menu.getRandomBetween;

public class Luggage extends Cargo {
    private final String owner;
    private double price;
    private final Type type;
    public enum Type{
        Bag("Bag"),
        Suitcase("Suitcase"),
        Box("Box");

        final String type;
        Type(String str){
            type = str;
        }

        static Type parseType(String str){
            if (str.equals("Bag"))
                return Bag;
            else if (str.equals("Suitcase"))
                return Suitcase;
            else
                return Box;
        }
    }
    public Luggage(String owner, Type type, double weight) {
        super("Luggage", weight);
        this.type = type;
        this.owner = owner;
    }

    public Luggage(Matcher matcher) {
        this(matcher.group(1)+" "+matcher.group(2), Type.parseType(matcher.group(3)), Double.parseDouble(matcher.group(4)));
    }

    @Override
    public String toString() {
        return type + ". Owner: " + owner + " (" + convertWeight(getWeight()) + ") " + price +"$";
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Owner (Name Surname) | Type (") + Menu.Text.highlighted("Bag",Menu.Color.PURPLE) + ", " +Menu.Text.highlighted("Suitcase",Menu.Color.PURPLE) + ", " +Menu.Text.highlighted("Box",Menu.Color.PURPLE) + Menu.Text.highlighted(") | Weight");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+([a-zA-Z]+)\\s+(Bag|Suitcase|Box)\\s+(\\d+.?\\d*)";
    }

    public String getOwner() {
        return owner;
    }

    public Type getLuggageType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Menu.roundDouble(price, 3);
    }

    public static Luggage generate() {
        Person person = Person.generate();
        if(person != null) {
            String owner = person.getName() + " " + person.getSurname();
            Type type = (Type)getRandomBetween(Type.Bag, getRandomBetween(Type.Box, Type.Suitcase));
            double weight = roundDouble(getRandomBetween(10, 60), 2);
            return new Luggage(owner, type, weight);
        }
        return null;
    }
}
