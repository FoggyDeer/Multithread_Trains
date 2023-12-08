package Cars;

import BasicClasses.StaticArrayList;
import CargoClasses.Mail;

import static BasicClasses.Menu.convertWeight;

public interface MailCarInterface {
    void addMailMenu();
    void showMailsMenu();
    void removeMailMenu();

    default String getMailInfo(Mail mail){
        return "("+(mail.getMailType() == Mail.Type.Package? convertWeight(mail.getWeight()) : mail.getText().length() + " symbols")+")";
    }

    default double getSenderMailsWeight(String sender, StaticArrayList<Mail> mailsList){
        return mailsList.stream().filter(mails -> mails.getSender().equals(sender)).mapToDouble(mail -> mail.getWeight()).sum();
    }

    default long getSenderMailsCount(String sender, StaticArrayList<Mail> mailsList){
        return mailsList.stream().filter(mails -> mails.getSender().equals(sender)).count();
    }
}
