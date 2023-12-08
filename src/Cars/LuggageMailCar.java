package Cars;

import BasicClasses.Locomotive;
import BasicClasses.StaticArrayList;
import CargoClasses.Cargo;
import CargoClasses.Luggage;
import CargoClasses.Mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class LuggageMailCar extends Car implements MailCarInterface{
    private final int max_mails_count;
    private final double max_luggage_weight;
    private final double price_for_kg;
    private final StaticArrayList<Mail> mails;
    private final ArrayList<Luggage> luggage;

    public LuggageMailCar(String sender, double tare_weight, int max_mails_count, double max_luggage_weight, double price_for_kg) {
        super("Wagon Bagażowo-Pocztowy", sender, tare_weight);
        this.max_mails_count = max_mails_count;
        this.max_luggage_weight = max_luggage_weight;
        this.price_for_kg = price_for_kg;
        mails = new StaticArrayList<>(max_mails_count);
        luggage = new ArrayList<>();
    }

    public LuggageMailCar(String sender, double tare_weight, int max_mails_count, double max_luggage_weight, double price_for_kg, boolean ignoreEvent) {
        super("Wagon Bagażowo-Pocztowy", sender, tare_weight, ignoreEvent);
        this.max_mails_count = max_mails_count;
        this.max_luggage_weight = max_luggage_weight;
        this.price_for_kg = price_for_kg;
        mails = new StaticArrayList<>(max_mails_count);
        luggage = new ArrayList<>();
    }

    public LuggageMailCar(Matcher matcher) {
        this(matcher.group(1),Double.parseDouble(matcher.group(2)),Integer.parseInt(matcher.group(3)),Integer.parseInt(matcher.group(4)), Double.parseDouble(matcher.group(5)));
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add Mail | ", mails.size() >= max_mails_count)+
                "2.Add Luggage | " +
                Text.disabled("3.Show mails | ", mails.size() == 0) +
                Text.disabled("4.Show luggage | ", luggage.size() == 0)+
                Text.disabled("5.Show owner's cargo (sorted by weight) | ", luggage.size()==0 && mails.size()==0)+
                Text.disabled("6.Remove mail | ", mails.size() == 0)+
                Text.disabled("7.Remove all person's cargo (mails, luggage)", luggage.size()==0));
        findInputExpression("([1-7])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> addMailMenu();
                case 2 -> addLuggageMenu();
                case 3 -> showMailsMenu();
                case 4 -> showLuggageMenu();
                case 5 -> showAllOwnersCargoMenu();
                case 6 -> removeMailMenu();
                case 7 -> removeAllPersonsCargoMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    @Override
    public void showCargo() {
        showMails();
        showLuggage();
    }

    @Override
    public String getCargoInfo() {
        StringBuilder sb = new StringBuilder("\nMails: ");
        for (int i = 0; i < mails.size(); i++) {
            sb.append("\n\t").append(i + 1).append(". ").append(mails.get(i));
        }
        sb.append("\nLuggage: ");
        for (int i = 0; i < luggage.size(); i++) {
            sb.append("\n\t").append(i + 1).append(". ").append(luggage.get(i));
        }
        return sb.toString();
    }

    public void addMailMenu() {
        prevSection = this::MainMenu;
        if (mails.size() >= max_mails_count) {
            error(new Exceptions.NoPlaceForMailException().getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + Mail.showConstructorParameters());
            findInputExpression(Mail.getParametersRegExp(), (Matcher matcher) -> {
                Mail mail = new Mail(matcher);
                addMail(mail);
                success(mail.getMailType()+" from " + mail.getSender() + " to " + mail.getReceiver() + " " +getMailInfo(mail), "added");
            }, this::addMailMenu);
        }
    }

    public void addMail(Mail mail) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        increaseNetWeight(mail.getWeight());
        mails.add(mail);
    }

    public void addLuggageMenu() {
        prevSection = this::MainMenu;
        System.out.println("\nEnter data in the following order: " + Luggage.showConstructorParameters());
        findInputExpression(Luggage.getParametersRegExp(), (Matcher matcher) -> {
            Luggage item = new Luggage(matcher);
            double price_index = 1;
            if(item.getWeight() > max_luggage_weight){
                error(new Exceptions.OutOfMaxLuggageWeightException(max_luggage_weight).getMessage());
                price_index = 1.5;
            }
            addLuggage(item, price_index);
            success(item.getOwner() + "'s " + item.getLuggageType() + " ("+ convertWeight(item.getWeight())+")", "added");
        }, this::addLuggageMenu);
    }

    public void addLuggage(Luggage item, double price_index) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        increaseNetWeight(item.getWeight());
        item.setPrice(item.getWeight()*price_for_kg*price_index);
        luggage.add(item);
    }

    public void showMailsMenu(){
        prevSection = this::MainMenu;
        if (mails.size() == 0) {
            error(new Exceptions.NoMailsSentException().getMessage());
        }else {
            showMails();
        }
    }

    public void showMails(){
        System.out.print("\n"+Color.BLACK_B);
        System.out.println("Mails: ");
        for(int i = 1; i <= mails.size(); i++){
            Mail mail = mails.get(i-1);
            System.out.println("\t"+i + ". " + mail.getMailType() + ". " + mail.getSender() + " -> " + mail.getReceiver() + " " +getMailInfo(mail));
        }
        System.out.print(Color.DEFAULT);
    }

    public void showLuggageMenu(){
        prevSection = this::MainMenu;
        if (luggage.size() == 0) {
            error(new Exceptions.NoLuggageException().getMessage());
        }else {
            showLuggage();
        }
    }

    public void showLuggage(){
        System.out.print("\n"+Color.BLACK_B);
        System.out.println("Luggage: ");
        for(int i = 1; i <= luggage.size(); i++){
            Luggage item = luggage.get(i-1);
            System.out.println("\t"+i + ". " + item.getLuggageType() + ". " + item.getOwner() + " " + item.getPrice() +"$ ("+convertWeight(item.getWeight())+(item.getWeight()>max_luggage_weight?". Price has risen":"") +")");
        }
        System.out.print(Color.DEFAULT);
    }

    public void showAllOwnersCargoMenu(){
        prevSection = this::MainMenu;
        if(luggage.size() + mails.size() == 0){
            error("No mails and luggage added!");
        }
        else {
            System.out.print("Select one option from the list:\n");
            showAllOwners();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= getUniqueOwnersCount()){
                    showAllOwnersCargo(getUniqueNames().get(option-1));
                }
                else
                    throw new WrongInputException();
            }, this::showAllOwnersCargoMenu);
        }
    }

    public void showAllOwners(){
        List<String> names = getUniqueNames();
        System.out.print(Color.BLACK_B);
        System.out.println("Owners|Senders:");
        for(int i = 0; i < names.size(); i++)
            System.out.println("\t"+(i+1)+". "+names.get(i));
        System.out.print(Color.DEFAULT);
    }

    public List<String> getUniqueNames(){
        Set<String> cargos = luggage.stream().map(item -> item.getOwner()).collect(Collectors.toSet());
        cargos.addAll(mails.stream().map(mail -> mail.getSender()).collect(Collectors.toSet()));
        return new ArrayList<>(cargos);
    }

    public int getUniqueOwnersCount(){
        Set<String> names = luggage.stream().map(item -> item.getOwner()).collect(Collectors.toSet());
        names.addAll(mails.stream().map(mail -> mail.getSender()).collect(Collectors.toSet()));
        return names.size();
    }

    public void showAllOwnersCargo(String person){
        ArrayList<Cargo> cargos = new ArrayList<>(luggage.stream().filter(item -> item.getOwner().equals(person)).map(cargo -> (Cargo)cargo).toList());
        cargos.addAll(mails.stream().filter(mail -> mail.getSender().equals(person)).map(cargo -> (Cargo)cargo).toList());
        cargos.sort((cargo1, cargo2) -> (int) (cargo2.getWeight() - cargo1.getWeight()));
        System.out.println("Cargos: ");
        System.out.print(Color.BLACK_B);
        for(Cargo cargo : cargos){
            System.out.println(cargo);
        }
        System.out.print(Color.DEFAULT);
    }

    public void removeMailMenu() {
        prevSection = this::MainMenu;
        if (mails.size() == 0) {
            error(new Exceptions.NoMailsSentException().getMessage());
        }
        else {
            System.out.print("Select one option from the list:\n");
            showMailsMenu();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= mails.size()){
                    Mail mail = mails.remove(option-1);
                    reduceNetWeight(mail.getWeight());
                    success(mail.getMailType()+" from " + mail.getSender() + " to " + mail.getReceiver() + " " + getMailInfo(mail), "removed");
                }
                else
                    throw new WrongInputException();
            }, this::removeMailMenu);
        }
    }

    public void removeAllPersonsCargoMenu(){
        prevSection = this::MainMenu;
        if(luggage.size() + mails.size() == 0){
            error("No mails and luggage added!");
        }
        else {
            System.out.print("Select one option from the list:\n");
            showAllOwners();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= getUniqueOwnersCount()){
                    String name = getUniqueNames().get(option-1);
                    removeAllOwnersCargo(name);
                    success(name+"'s all cargo", "removed");
                }
                else
                    throw new WrongInputException();
            }, this::showAllOwnersCargoMenu);
        }
    }

    public void removeAllOwnersCargo(String name){
        List<Luggage> luggage = this.luggage.stream().filter(_luggage -> _luggage.getOwner().equals(name)).toList();
        List<Mail> mails = this.mails.stream().filter(mail -> mail.getSender().equals(name)).toList();
        for(Luggage item : luggage){
            this.luggage.remove(item);
            reduceNetWeight(item.getWeight());
        }
        for(Mail mail : mails){
            this.mails.remove(mail);
            reduceNetWeight(mail.getWeight());
        }
    }

    @Override
    public String toString(){
        return super.toString() + " | Max mails count: " + max_mails_count + " | Max luggage weight: " + max_luggage_weight + " | Price for kg: " + price_for_kg + "$";
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+)\\s+(\\d+\\.?\\d*)\\s+(\\d+\\.?\\d*)";
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max mails count | Max luggage weight (for 1 item) | Price for kg");
    }

    @Override
    public boolean isElectricCar() {
        return false;
    }

    public static class Exceptions{
        public static class NoPlaceForMailException extends Exception{
            public NoPlaceForMailException(){
                super("No place for new mail!");
            }
        }

        public static class NoMailsSentException extends Exception{
            public NoMailsSentException(){
                super("No mails was sent!");
            }
        }
        public static class NoLuggageException extends Exception{
            public NoLuggageException(){
                super("No luggage was added!");
            }
        }

        public static class OutOfMaxLuggageWeightException extends Exception{
            public OutOfMaxLuggageWeightException(double max){
                super("Weight is larger than max allowed: "+ convertWeight(max) +"! Luggage price will increase!");
            }
        }
    }

    public static LuggageMailCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_mails_count = getRandomBetween(50_000, 100_000);
        double max_luggage_weight = getRandomBetween(10, 60);
        double price_for_kg = roundDouble(getRandomBetween(1.0, 20.0), 2);
        LuggageMailCar car = new LuggageMailCar(DefaultSender, weight, max_mails_count, max_luggage_weight, price_for_kg, true);
        for(int i = 100; i > 0; i--){
            try {
                car.addMail(Mail.generate());
            }catch (Exception ignored){}
        }
        for(int i = 100; i > 0; i--){
            try {
                Luggage luggage =Luggage.generate();
                double price_index = 1;
                if(luggage.getWeight() > max_luggage_weight){
                    price_index = 1.5;
                }
                car.addLuggage(luggage, price_index);
            }catch (Exception ignored){}
        }
        return car;
    }
}
