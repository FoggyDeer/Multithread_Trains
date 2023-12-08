package CargoClasses;

import BasicClasses.Menu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static BasicClasses.Menu.*;

public class Mail extends Cargo {
    public static final double letterWeight = 0.016;
    private final String text;
    private final String sender;
    private final String receiver;
    private final Type type;
    public enum Type{
        Package("Package"),
        Letter("Letter");

        final String type;
        Type(String str){
            type = str;
        }

        static Type parseType(String str){
            if (Pattern.compile("Package").matcher(str).find())
                return Package;
            return Letter;
        }
    }
    public Mail(String sender, String receiver, String text, Type type, double weight){
        super("Mail",weight);
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
    }

    public Mail(Matcher matcher){
        this(matcher.group(1) + " " + matcher.group(2), matcher.group(3) + " " + matcher.group(4), matcher.group(5), Type.parseType(matcher.group(6)), (matcher.groupCount() == 7 && matcher.group(7) != null && matcher.group(7).length()>0)? Double.parseDouble(matcher.group(7)) : Mail.letterWeight);
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlighted("Sender (Name Surname) | Receiver (Name Surname) | Text (") + Menu.Text.highlighted("\"...\"", Menu.Color.PURPLE) + Menu.Text.highlighted(") | Type (") + Menu.Text.highlighted("Package or Letter", Menu.Color.PURPLE) + Menu.Text.highlighted(") | (If package: Package weight)");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+([a-zA-Z]+)\\s+([a-zA-Z]+)\\s+([a-zA-Z]+)\\s+(\".+\")\\s+(Letter|Package\\s+(\\d+.?\\d*))";
    }

    @Override
    public String toString() {
        return "Type: "+type+(type == Type.Package? ", weight: " + convertWeight(getWeight()) : "") +".\n\tSender: " + sender + " -> Receiver: " + receiver + ".\n\t"+text+"\n";
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public Type getMailType() {
        return type;
    }

    public static Mail generate() {
        Person sender = Person.generate();
        Person receiver = Person.generate();
        Type type = (Type)getRandomBetween(Type.Package, Type.Letter);
        double weight;
        if(type == Type.Letter)
            weight = Mail.letterWeight;
        else
            weight = roundDouble(getRandomBetween(10, 60), 2);
        if(sender != null && receiver != null) {
            String p1 = sender.getName() + " " + sender.getSurname();
            String p2 = receiver.getName() + " " + receiver.getSurname();
            return new Mail(p1, p2, "*****g*a**h****r**h*******sd**?", type, weight);
        }
        return null;
    }
}
