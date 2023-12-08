package CargoClasses;

import BasicClasses.Menu;
import BasicClasses.Menu.*;

import java.util.regex.Matcher;

import static BasicClasses.Menu.getRandomBetween;
import static BasicClasses.Menu.roundDouble;

public class Person{
    private final String name;
    private final String surname;
    private final int age;
    private final Gender gender;
    private final double weight;
    private int seat_number;

    private final Ticket ticket;

    public enum Ticket{
        Regular("Regular"),
        VIP("VIP");

        final String ticket;
        Ticket(String str){
            ticket = str;
        }

        static Ticket parseTicket(String str){
            if (str.equals("VIP"))
                return VIP;
            return Regular;
        }
    }

    enum Gender{
        Male("Male"),
        Female("Female");

        final String gender;
        Gender(String str){
            gender = str;
        }
    }

    public Person(String name, String surname, int age, char gender, double weight, Ticket ticket) throws WrongInputException {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.gender = switch (gender){
            case 'm': yield Gender.Male;
            case 'f': yield Gender.Female;
            default:
                throw new WrongInputException("Unexpected value: " + gender);
        };
        this.weight = weight;
        this.ticket = ticket;
    }

    public Person(Matcher matcher) throws WrongInputException {
        this(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3)), matcher.group(4).toLowerCase().charAt(0), Double.parseDouble(matcher.group(5)), Ticket.parseTicket(matcher.group(6)));
    }

    public static String showConstructorParameters(){
        return Menu.Text.highlightedPattern("/Y/Name | Surname | Age | Gender (://P/M or F://Y/) | Weight | Ticket (://P/Regular or VIP://Y/):/");
    }

    public static String getParametersRegExp(){
        return "([a-zA-Z]+)\\s+([a-zA-Z]+)\\s+(\\d+)\\s+([mfMF])\\s+(\\d+\\.?\\d*)\\s+(Regular|VIP)";
    }

    @Override
    public String toString() {
        return name+ " " + surname + ", " + age + (age == 1 ? " year, " : " years, ") + gender + ", " + weight + "kg. Ticket: " + ticket;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Person))
            return false;
        return ((Person) obj).getName().equals(this.name)
                && ((Person) obj).getSurname().equals(this.surname)
                && ((Person) obj).getAge() == age
                && ((Person) obj).getGender() == gender
                && getWeight() == weight;
    }

    @Override
    public int hashCode() {
        return (name+surname+age+gender+weight).hashCode();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public Gender getGender() {
        return gender;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setSeatNumber(int index){
        this.seat_number = index;
    }

    public int getSeatNumber() {
        return seat_number;
    }

    public static Person generate(){
        String[] Fnames = {"Lena", "Ala", "Maria", "Katarzyna", "Zofia", "Hanna", "Oliwia", "Alicja", "Zuzanna"};
        String[] Mnames = {"Kacper", "Jakub", "Filip", "Jan", "Andrzej", "Stanisław", "Franciszek", "Aleksander", "Michał"};
        String[] Fsurnames = {"Nowak", "Kowalska", "Wiśniewska", "Lewandowska", "Woźniak"};
        String[] Msurnames = {"Nowak", "Kowalski", "Wiśniewski", "Lewandowski", "Woźniak"};
        String name;
        String surname;

        char gender = getRandomBetween('m', 'f');
        double weight;
        if(gender == 'f'){
            name = getRandomBetween(Fnames);
            surname = getRandomBetween(Fsurnames);
        }else {
            name = getRandomBetween(Mnames);
            surname = getRandomBetween(Msurnames);
        }

        int age = getRandomBetween(5, 90);
        if(age > 15)
            weight = roundDouble(getRandomBetween(40, 100), 2);
        else
            weight = roundDouble(getRandomBetween(25, 50), 2);

        Ticket ticket = (Ticket) getRandomBetween(Ticket.Regular, Ticket.VIP);
        try {
            return new Person(name, surname, age, gender, weight, ticket);
        }catch (Exception ignored){}
        return null;
    }
}
