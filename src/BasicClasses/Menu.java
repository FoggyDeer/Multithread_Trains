package BasicClasses;

//import org.intellij.lang.annotations.Language;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Menu {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isRunning = true;
    protected static Function prevSection;
    private static ArrayList<Thread> threads = new ArrayList<>();
    protected enum InputMode{
        DEFAULT,
        UNIQUE
    }

    public enum Color{
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        WHITE("\u001B[37m"),
        BLACK_B("\u001B[40m"),
        PURPLE("\u001B[35m"),
        DEFAULT("\u001B[0m");

        public final String color;
        Color(String s){
            this.color = s;
        }

        @Override
        public String toString() {
            return color;
        }

        public static String getAllColorsRegex() {
            return "(\u001B\\[31m)|(\u001B\\[32m)|(\u001B\\[33m)|(\u001B\\[37m)|(\u001B\\[40m)|(\u001B\\[35m)|(\u001B\\[0m)";
        }
    }

    protected static <T> void findInputExpression(String regex, TemplateFunction<T> function, Function error) {
        try {
            System.out.print(InteractiveMenu.Color.DEFAULT);
            String input = scanner.nextLine().trim();
            if(input.equals("exit") || input.equals("quit")){
                closeMenuThreads();
                isRunning = false;
            }
            else if(!input.equals("/") && !input.equals("")){
                Matcher matcher = Pattern.compile(regex).matcher(input);
                if (matcher.matches())
                    function.call((T) matcher);
                else
                    throw new WrongInputException();
            } else if(regex.equals("/"))
                closeMenuThreads();
        }catch (Exception e){
            WrongInput.notify(error, e.getMessage());
        }
    }

    protected static <T> void findInputExpression(String regex, InputMode mode, TemplateFunction<T> function, Function error){
        try {
            System.out.print(InteractiveMenu.Color.DEFAULT);
            String input = scanner.nextLine().trim();
            if(input.equals("exit") || input.equals("quit")){
                closeMenuThreads();
                isRunning = false;
            }
            else if(!input.equals("/") && !input.equals("")){
                switch (mode) {
                    case UNIQUE -> {
                        String[] arr = input.split("\\s+");
                        Set<String> set = new HashSet<>();
                        Pattern pattern = Pattern.compile("(?!" + regex + ").");

                        if (arr.length > 0) {
                            for (String elem : arr) {
                                if (elem.length() == 0 || pattern.matcher(elem).find() || !set.add(elem))
                                    throw new WrongInputException();
                            }
                        } else
                            throw new WrongInputException();
                        function.call((T) Pattern.compile(regex).matcher(input));
                    }
                    case DEFAULT -> {
                        Matcher matcher = Pattern.compile(regex).matcher(input);
                        if (matcher.matches())
                            function.call((T) matcher);
                        else
                            throw new WrongInputException();
                    }
                }
            } else if(regex.equals("/"))
                closeMenuThreads();
        }catch (Exception e){
            WrongInput.notify(error, e.getMessage());
        }
    }

    public static void close(){
        scanner.close();
        closeMenuThreads();
        isRunning = false;
    }

    public static void closeMenuThreads(){
        for (Thread thread : Menu.threads){
            thread.interrupt();
        }
        threads.clear();
    }

    public static void addMenuThread(Thread thread){
        threads.add(thread);
    }

    protected static <T> void chooseOneOption(int max_option_value, TemplateFunction<T> function, Function error){
        findInputExpression("(\\d+)", (Matcher matcher) -> {
            Integer option = Integer.parseInt(matcher.group(1));
            if(option > 0 && option <= max_option_value){
                option--;
                function.call((T)option);
            }
            else
                throw new WrongInputException();
        }, error);
    }

    public static void success(String object, String action){
        System.out.println(Color.GREEN + "-------------------------------------------------------------------------");
        System.out.println(Color.YELLOW + object + Color.GREEN + " has been " + action + " successfully!");
        System.out.println("-------------------------------------------------------------------------");
        System.out.print(Color.DEFAULT);
    }

    protected static void run(){
        while (isRunning()) {
            prevSection.call();
            System.out.print(Color.DEFAULT);
            while (WrongInput.hasLastError())
                WrongInput.invoke();
        }
    }

    public static class Text {
        private static final Map<Color, Pattern> patterns = Map.of(
                Color.BLACK_B, Pattern.compile("/Bb/(.+?):/"),
                Color.GREEN, Pattern.compile("/G/(.+?):/"),
                Color.PURPLE, Pattern.compile("/P/(.+?):/"),
                Color.RED, Pattern.compile("/R/(.+?):/"),
                Color.WHITE, Pattern.compile("/W/(.+?):/"),
                Color.YELLOW, Pattern.compile("/Y/(.+?):/")
        );

        public static String highlightedPattern(String str) {
            Matcher matcher;
            for (Map.Entry<Color, Pattern> entry : patterns.entrySet()) {
                matcher = entry.getValue().matcher(str);
                str = matcher.replaceAll(match -> entry.getKey() + match.group(1) + Color.DEFAULT);
            }
            return str;
        }

        public static String highlighted(String str) {
            return Color.YELLOW + str + Color.DEFAULT;
        }

        public static String highlighted(String str, Color color) {
            return color + str + Color.DEFAULT;
        }

        public static String disabled(String str, boolean condition) {
            if (condition)
                return Color.WHITE + str + Color.DEFAULT;
            else
                return str;
        }
    }

    public static void error(String str) {
        StringBuilder line = new StringBuilder();
        for (int i = 0, max = str.length() + 12; i < max; i++) {
            line.append('-');
        }
        System.out.print(Color.RED);
        System.out.println(line);
        System.out.println(str);
        System.out.println(line);
        System.out.print(Color.DEFAULT);
    }

    public static String convertWeight(double weight) {
        if (weight < 0)
            return (weight * 1000) + "g";
        else if (weight > 1000)
            return roundDouble(weight / 1000, 3) + "t";
        else
            return weight + "kg";
    }

    public static String convertVolume(double volume){
        if(volume < 0)
            return (volume*1000)+"ml";
        else
            return volume+"l";
    }

    public int sumBoolean(boolean x, boolean y) {
        return (x && y) ? 3 : (y) ? 2 : (x) ? 1 : 0;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static double roundDouble(double val, int pow) {
        return ((long) (val * Math.pow(10.0, pow))) / Math.pow(10.0, pow);
    }

    public static double getRandomBetween(double val1, double val2){
        double min = Math.min(val1, val2);
        double max = Math.max(val1, val2);
        return Math.random() * (max - min + 1) + min;
    }

    public static int getRandomBetween(int val1, int val2){
        int min = Math.min(val1, val2);
        int max = Math.max(val1, val2);
        return (int)(Math.random() * (max - min + 1) + min);
    }

    public static int getRandomBetween(int val1, int val2, boolean between_options){
        if(between_options)
            return (Math.random() > 0.5) ? val2 : val1;
        else
            return getRandomBetween(val1, val2);
    }

    public static Object getRandomBetween(Object val1, Object val2){
        return (Math.random() > 0.5) ? val2 : val1;
    }

    public static String getRandomBetween(String[] strings){
        return strings[getRandomBetween(0, strings.length-1)];
    }

    public static char getRandomBetween(char ch1, char ch2){
        return (Math.random() > 0.5) ? ch2 : ch1;
    }

    public static class WrongInputException extends Exception {

        public WrongInputException() {
            super();
        }

        public WrongInputException(String str) {
            super(str);
        }
    }
}
