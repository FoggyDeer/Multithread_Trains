package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class Dish{
    private final Type type;
    private final String name;
    private final double price;
    private final String description;

    public enum Type{
        Drink("Drink"),
        Food("Food");

        final String type;
        Type(String str){
            type = str;
        }

        static Type parseType(String str){
            if (str.equals("Drink"))
                return Drink;
            return Food;
        }
    }


    Dish(Type type, String name, double price, String description){
        this.name = name;
        this.price = price;
        this.description = description;
        this.type = type;
    }

    public Dish(Matcher matcher){
        this(Type.parseType(matcher.group(1)), matcher.group(2), Double.parseDouble(matcher.group(3)), matcher.group(4));
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Type (") + Menu.Text.highlighted("Food or Drink", Menu.Color.PURPLE) + Menu.Text.highlighted(") | Name | Price | Description (")+ Menu.Text.highlighted("\"...\"", Menu.Color.PURPLE)+ Menu.Text.highlighted(")");
    }

    public static String getParametersRegExp(){
        return "(Food|Drink)\\s+([a-zA-Z]+)\\s+(\\d+\\.?\\d*)\\s+(\".*\")";
    }

    @Override
    public String toString() {
        return type + ". " + name + " " + price + "$:\n\t\t"+description+"";
    }

    public Type getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getPriseString(){
        return price+"$";
    }

    public static Dish generate() {
        String[] dishNames = {"rice", "steak", "fish", "salad", "pancake"};
        String[] drinkNames = {"juice", "water", "vine", "lemonade"};
        String name;
        Type type = (Type) getRandomBetween(Type.Food, Type.Drink);
        if(type.equals(Type.Food)){
            name = getRandomBetween(dishNames);
        }else {
            name = getRandomBetween(drinkNames);
        }
        double price = roundDouble(getRandomBetween(5, 18), 2);
        return new Dish(type, name, price, ".......");
    }
}
