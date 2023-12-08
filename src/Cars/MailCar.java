package Cars;

import BasicClasses.Locomotive;
import BasicClasses.StaticArrayList;
import CargoClasses.Mail;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MailCar extends Car implements MailCarInterface{
    private final int max_mails_count;
    private final double price_for_kg;
    private final StaticArrayList<Mail> mails;

    public MailCar(String sender, double tare_weight, int max_mails_count, double price_for_kg) {
        super("Wagon Pocztowy", sender, tare_weight);
        this.max_mails_count = max_mails_count;
        this.price_for_kg = price_for_kg;
        mails = new StaticArrayList<>(max_mails_count);
    }

    public MailCar(String sender, double tare_weight, int max_mails_count, double price_for_kg, boolean ignoreEvent) {
        super("Wagon Pocztowy", sender, tare_weight, ignoreEvent);
        this.max_mails_count = max_mails_count;
        this.price_for_kg = price_for_kg;
        mails = new StaticArrayList<>(max_mails_count);
    }

    public MailCar(Matcher matcher) {
        this(matcher.group(1),Double.parseDouble(matcher.group(2)),Integer.parseInt(matcher.group(3)), Double.parseDouble(matcher.group(4)));
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add Mail | ", mails.size() >= max_mails_count) +
                Text.disabled("2.Show senders, mails, price | 3.Show mails | 4.Show mails from Sender | 5.Remove mail", mails.size() == 0));
        findInputExpression("([1-5])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> addMailMenu();
                case 2 -> showMailsDetailsMenu();
                case 3 -> showMailsMenu();
                case 4 -> showMailsFromSenderMenu();
                case 5 -> removeMailMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    @Override
    public void showCargo() {
        showMails();
    }

    @Override
    public String getCargoInfo() {
        StringBuilder sb = new StringBuilder("\nMails: ");
        for(int i = 1; i <= mails.size(); i++){
            Mail mail = mails.get(i-1);
            sb.append("\n\t").append(i).append(". ").append(mail.getMailType()).append(". ").append(mail.getSender()).append(" -> ").append(mail.getReceiver()).append(" ").append(getMailInfo(mail));
        }
        return sb.toString();
    }

    public void addMailMenu() {
        prevSection = this::MainMenu;
        if (mails.size() >= max_mails_count) {
            error(new Exceptions.OutOfMailCountException().getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + Mail.showConstructorParameters());
            findInputExpression(Mail.getParametersRegExp(), (Matcher matcher) -> {
                Mail mail = new Mail(matcher);
                increaseNetWeight(mail.getWeight());
                mails.add(mail);
                success(mail.getMailType()+" from " + mail.getSender() + " to " + mail.getReceiver() + " " +getMailInfo(mail), "added");
            }, this::addMailMenu);
        }
    }

    public void addMail(Mail mail) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        increaseNetWeight(mail.getWeight());
        mails.add(mail);
    }

    public void showMailsDetailsMenu(){
        prevSection = this::MainMenu;
        if (mails.size() == 0) {
            error(new Exceptions.NoMailsSentException().getMessage());
        }else {
            Set<String> senders = mails.stream().map((mail -> mail.getSender())).collect(Collectors.toSet());
            int i = 1;
            System.out.print(Color.BLACK_B);
            for(String sender : senders) {
                System.out.println("\t"+(i++)+". " + sender + " - " + getSenderMailsCount(sender, mails) + " mails. Price: " + roundDouble(price_for_kg *getSenderMailsWeight(sender, mails),3) + "$");
            }
            System.out.print(Color.DEFAULT);
        }
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
        System.out.print(Color.BLACK_B);
        System.out.println(getCargoInfo());
        System.out.print(Color.DEFAULT);
    }

    public void showMailsFromSenderMenu(){
        prevSection = this::MainMenu;
        if (mails.size() == 0) {
            error(new Exceptions.NoMailsSentException().getMessage());
        }else {
            System.out.print("Select one item from the list:\n");
            showMailsDetailsMenu();
            List<String> senders = mails.stream().map((mail -> mail.getSender())).collect(Collectors.toSet()).stream().toList();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= senders.size()){
                    System.out.print(Color.BLACK_B);
                    mails.stream().filter(mail -> mail.getSender().equals(senders.get(option-1))).forEach(System.out::println);
                    System.out.print(Color.DEFAULT);
                }
                else
                    throw new WrongInputException();
            }, this::showMailsFromSenderMenu);
        }
    }

    public void removeMailMenu() {
        prevSection = this::MainMenu;
        if (mails.size() == 0) {
            error(new Exceptions.NoMailsSentException().getMessage());
        }
        else {
            System.out.print("Select one item from the list:\n");
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

    @Override
    public String toString(){
        return super.toString() + " | Max mails count: " + max_mails_count + " | Price for kg: " + price_for_kg + "$";
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+)\\s+(\\d+\\.?\\d*)";
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max mails count | Price for kg");
    }

    @Override
    public boolean isElectricCar() {
        return true;
    }

    public static class Exceptions{
        public static class OutOfMailCountException extends Exception{
            public OutOfMailCountException(){
                super("No place for new mail!");
            }
        }

        public static class NoMailsSentException extends Exception{
            public NoMailsSentException(){
                super("No mails was sent!");
            }
        }
    }

    public static MailCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_mails_count = getRandomBetween(50_000, 100_000);
        double price_for_kg = roundDouble(getRandomBetween(1.0, 20.0), 2);
        MailCar car = new MailCar(DefaultSender, weight, max_mails_count, price_for_kg, true);
        for(int i = 100; i > 0; i--){
            try {
                car.addMail(Mail.generate());
            }catch (Exception ignored){}
        }
        return car;
    }
}
