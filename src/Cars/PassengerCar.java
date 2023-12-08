package Cars;

import BasicClasses.Condition;
import BasicClasses.Locomotive;
import BasicClasses.StaticArrayList;
import CargoClasses.Person;
import CargoClasses.Person.Ticket;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;


public class PassengerCar extends Car implements CarType {
    private final int max_passengers_count;
    private final StaticArrayList<Person> passengers;
    private final int vip_seats_count;
    private final Map<Integer, Person> seats = new LinkedHashMap<>();

    public PassengerCar(String sender, Double tare_weight, Integer max_passengers_count, Integer vip_seats_count) throws Exceptions.VipSeatsCountException {
        super("Wagon Pasażerski", sender, tare_weight);
        this.max_passengers_count = max_passengers_count;
        if(vip_seats_count > max_passengers_count)
            throw new Exceptions.VipSeatsCountException();
        this.vip_seats_count = vip_seats_count;
        passengers = new StaticArrayList<>(max_passengers_count);
    }

    public PassengerCar(String sender, Double tare_weight, Integer max_passengers_count, Integer vip_seats_count, boolean ignoreEvent) throws Exceptions.VipSeatsCountException {
        super("Wagon Pasażerski", sender, tare_weight, ignoreEvent);
        this.max_passengers_count = max_passengers_count;
        if(vip_seats_count > max_passengers_count)
            throw new Exceptions.VipSeatsCountException();
        this.vip_seats_count = vip_seats_count;
        passengers = new StaticArrayList<>(max_passengers_count);
    }

    public PassengerCar(Matcher matcher) throws Exceptions.VipSeatsCountException {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }

    public void addPassenger(Person person) throws Exceptions.NoFreeSeatsException, Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight, Exceptions.PassengerIsAlreadyAddedException {
        try {
            if(containsPassenger(person)){
                throw new Exceptions.PassengerIsAlreadyAddedException();
            }else {
                increaseNetWeight(person.getWeight());
                passengers.add(person);
            }
        } catch (ArrayIndexOutOfBoundsException e){
            throw new Exceptions.NoFreeSeatsException();
        }
    }

    public Person removePassenger(int index){
        Person passenger = passengers.remove(index);
        seats.remove(passenger.getSeatNumber());
        reduceNetWeight(passenger.getWeight());
        return passenger;
    }

    public boolean containsPassenger(Person person){
        return passengers.contains(person);
    }

    public void MainMenu(){
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add passenger | ", passengers.size() >= max_passengers_count) +
                Text.disabled("2.Show all passengers | ", passengers.size() == 0) +
                Text.disabled("3.Show VIP passengers", getVipPassengersCount() == 0) +
                Text.disabled(" | 4.Remove passenger | 5.Show count of children ", passengers.size() == 0) +
                Text.disabled("| 6.Show seats info", false));

        findInputExpression("([1-6])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> addPassengerMenu();
                case 2 -> showAllPassengers();
                case 3 -> showVipPassengers();
                case 4 -> removePassengerMenu();
                case 5 -> showChildrenMenu();
                case 6 -> showAllSeatsInfo();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    @Override
    public void showCargo() {
        showPassengers((passenger) -> true);
    }

    @Override
    public String getCargoInfo() {
        StringBuilder sb = new StringBuilder("\nPassengers: ");
        for (int i = 0; i < passengers.size(); i++) {
            sb.append("\n\t").append(i + 1).append(". ").append(passengers.get(i));
        }
        return sb.toString();
    }

    public void addPassengerMenu(){
        prevSection = this::MainMenu;
        if(passengers.size() >= max_passengers_count){
            error(new Exceptions.NoFreeSeatsException().getMessage());
        }
        else {
            System.out.println("\nEnter data in the following order: " + Person.showConstructorParameters());
            findInputExpression(Person.getParametersRegExp(), (Matcher matcher) -> {
                Person person = new Person(matcher);
                try {
                    reserveSeat(person);
                    success("Passenger " + person.getName() + " " + person.getSurname(), "added");
                } catch (Exceptions.NoFreeSeatsException | Car.Exceptions.CarIsOnTheWayException |
                         Locomotive.Exceptions.OutOfMaxPullWeight | Exceptions.PassengerIsAlreadyAddedException |
                         Exceptions.OutOfMaxVipSeatsException e) {
                    throw new WrongInputException(e.getMessage());
                }
            }, this::addPassengerMenu);
        }
    }

    public void showPassengers(Condition<Person> condition){
        if(passengers.size() == 0){
            error(new Exceptions.NoPassengersInCarException().getMessage());
        }
        else {
            System.out.print(Color.BLACK_B);
            System.out.println("\nPassengers: ");
            for (int i = 0; i < passengers.size(); i++) {
                if (condition.check(passengers.get(i)))
                    System.out.println("\t"+(i+1) + ". " + passengers.get(i));
            }
            System.out.print(Color.DEFAULT);
        }
    }

    public void showAllPassengers(){
        prevSection = this::MainMenu;
        showPassengers((passenger) -> true);
    }

    public void showVipPassengers(){
        prevSection = this::MainMenu;
        if(getVipPassengersCount() == 0){
            error(new Exceptions.NoVipPassengersInCarException().getMessage());
        }
        else {
            System.out.print(Color.BLACK_B);
            System.out.print(Text.highlighted("VIP "));
            showPassengers((passenger) -> passenger.getTicket() == Ticket.VIP);
        }
    }

    public void removePassengerMenu(){
        prevSection = this::MainMenu;
        if(!hasPassengers()) {
            showAllPassengers();
        }else {
            System.out.print("Select one person from the list:\n");
            showAllPassengers();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= passengers.size()) {
                    Person person = removePassenger(option - 1);
                    success("Passenger " + person.getName() + " " + person.getSurname(), "removed");
                } else
                    throw new WrongInputException();
            }, this::removePassengerMenu);
        }
    }

    public void showChildrenMenu(){
        prevSection = this::MainMenu;
        StringBuilder sb = new StringBuilder(this.getType()).append(" contains ");
        long C_count = getChildrenCount();
        long T_count = getTeenagersCount();
        sb.append(C_count).append((C_count == 1 ? " child" : " children"));
        if(C_count > 0)
            sb.append(", where ").append(T_count).append(" of them ").append((T_count == 1 ? "is teenager":"are teenagers"));
        System.out.println(sb);
    }

    public void reserveSeat(Person person) throws Car.Exceptions.CarIsOnTheWayException, Exceptions.PassengerIsAlreadyAddedException, Locomotive.Exceptions.OutOfMaxPullWeight, Exceptions.NoFreeSeatsException, Exceptions.OutOfMaxVipSeatsException, Exceptions.NoFreeRegularSeatsException {
        if(person.getTicket() == Ticket.VIP && getVipPassengersCount() >= vip_seats_count){
            throw new Exceptions.OutOfMaxVipSeatsException(vip_seats_count);
        }
        else if(person.getTicket() == Ticket.Regular && max_passengers_count - vip_seats_count - getRegularPassengersCount() <= 0){
            throw new Exceptions.NoFreeRegularSeatsException();
        }
        else {
            int seat_index = getAvailableSeat(person.getTicket());
            if(seat_index > 0) {
                seats.put(seat_index, person);
                addPassenger(person);
                person.setSeatNumber(seat_index);
            }
            else
                throw new Exceptions.NoFreeSeatsException();
        }
    }

    public int getAvailableSeat(Ticket ticket){
        Collection<Integer> set = seats.keySet();
        if(ticket == Ticket.VIP) {
            for (int i = 1; i <= vip_seats_count; i++)
                if (!set.contains(i))
                    return i;
        } else {
            for (int i = vip_seats_count + 1; i <= max_passengers_count; i++)
                if (!set.contains(i))
                    return i;
        }
        return -1;
    }

    public void showAllSeatsInfo(){
        prevSection = this::MainMenu;
        Collection<Integer> keys = seats.keySet();

        System.out.println();
        System.out.println(Color.BLACK_B+"Seats: ");
        System.out.println("VIP:");
        for(int i = 1; i <= vip_seats_count; i++){
            showSeatInfo(i, keys);
        }
        System.out.println("\nRegular:");
        for(int i = vip_seats_count+1; i <= max_passengers_count; i++){
            showSeatInfo(i, keys);
        }
    }

    public void showSeatInfo(int index, Collection<Integer> keys){
        System.out.print("\t#" + (index) + " ");
        if(keys.contains(index)) {
            int age = seats.get(index).getAge();
            System.out.println(seats.get(index).getName() + " " + seats.get(index).getSurname() + ", " + age + (age == 1 ? " year" : " years"));
        }else
            System.out.println("empty");
    }

    public long getChildrenCount(){
        return passengers.stream().filter((passenger) -> passenger.getAge() < 18).count();
    }

    public long getTeenagersCount(){
        return passengers.stream().filter((passenger) -> passenger.getAge() < 18 && passenger.getAge() > 12).count();
    }

    public boolean hasPassengers(){
        return passengers.size() > 0;
    }

    public int getVipPassengersCount(){
        return (int)passengers.stream().filter((passenger) -> passenger.getTicket() == Ticket.VIP).count();
    }

    public int getRegularPassengersCount(){
        return (int)passengers.stream().filter((passenger) -> passenger.getTicket() == Ticket.Regular).count();
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max passenger count | VIP seats count");
    }

    @Override
    public String toString(){
        return super.toString() + " | Max passenger count: " + max_passengers_count + ", VIP seats: " + vip_seats_count;
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+)\\s+(\\d+)";
    }

    @Override
    public boolean isElectricCar() {
        return true;
    }

    public static class Exceptions{
        public static class NoFreeSeatsException extends Exception{
            public NoFreeSeatsException(){
                super("There is no free seat for a new passenger!");
            }
        }
        public static class PassengerIsAlreadyAddedException extends Exception{
            public PassengerIsAlreadyAddedException(){
                super("This passenger has already been added to the car!");
            }
        }
        public static class OutOfMaxVipSeatsException extends Exception{
            public OutOfMaxVipSeatsException(int count){
                super("Maximum pull weight is: " + count + "!");
            }
        }
        public static class VipSeatsCountException extends Exception{
            public VipSeatsCountException(){
                super("VIP seats count cannot be greater than total number of seats!");
            }
        }
        public static class NoFreeRegularSeatsException extends Exception{
            public NoFreeRegularSeatsException(){
                super("There are no free regular seats in car!");
            }
        }
        public static class NoVipPassengersInCarException extends Exception{
            public NoVipPassengersInCarException(){
                super("There are no VIP passengers in the train car!");
            }
        }
        public static class NoPassengersInCarException extends Exception{
            public NoPassengersInCarException(){
                super("There are no passengers in the train car!");
            }
        }
    }

    public static PassengerCar generate(){
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_passengers_count = getRandomBetween(50, 100);
        int vip_seats_count = getRandomBetween(0, 50);
        try {
            PassengerCar car = new PassengerCar(DefaultSender, weight, max_passengers_count, vip_seats_count, true);
            for(int i = vip_seats_count; i > 0; i--){
                Person person = Person.generate();
                if(person.getTicket() == Ticket.Regular)
                    i++;
                else if(car.containsPassenger(person))
                    i++;
                else
                    car.addPassenger(person);
            }
            for(int i = max_passengers_count-vip_seats_count; i > 0; i--){
                Person person = Person.generate();
                if(person.getTicket() == Ticket.VIP)
                    i++;
                else if(car.containsPassenger(person))
                    i++;
                else
                    car.addPassenger(person);
            }
            return car;
        }catch (Exception ignored){
            ignored.printStackTrace();
        }

        return null;
    }
}
