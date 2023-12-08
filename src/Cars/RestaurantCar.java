package Cars;

import BasicClasses.Locomotive;
import CargoClasses.Dish;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class RestaurantCar extends Car{
    private final String cuisineName;
    private final int tablesCount;
    private final int seatsCount;
    private final Map<String, ArrayList<Dish>> menu = new LinkedHashMap<>();

    public RestaurantCar(String sender, double tare_weight, String cuisineName, int tablesCount, int seatsCount) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        super("Wagon Restauracyjny", sender, tare_weight);
        this.cuisineName = cuisineName;
        this.tablesCount = tablesCount;
        this.seatsCount = seatsCount;
        increaseNetWeight(tablesCount*40+seatsCount/2+seatsCount%2*50);
    }

    public RestaurantCar(String sender, double tare_weight, String cuisineName, int tablesCount, int seatsCount, boolean ignoreEvent) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        super("Wagon Restauracyjny", sender, tare_weight, ignoreEvent);
        this.cuisineName = cuisineName;
        this.tablesCount = tablesCount;
        this.seatsCount = seatsCount;
        increaseNetWeight(tablesCount*40+seatsCount/2+seatsCount%2*50);
    }

    public RestaurantCar(Matcher matcher) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)), matcher.group(3), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(4)));
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n1.Add menu category | " +
                Text.disabled("2.Add dish | 3.Show menu | 4.Delete category | ", menu.size() == 0) +
                Text.disabled("5.Delete dish | ", getDishesCount()+getDrinksCount()==0) +
                Text.disabled("6.Count dishes | ", getDishesCount() == 0) +
                Text.disabled("7.Count drinks", getDrinksCount() == 0));

        findInputExpression("([1-7])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> addMenuCategoryMenu();
                case 2 -> addDishMenu();
                case 3 -> showMenu();
                case 4 -> removeCategoryMenu();
                case 5 -> removeDishMenu();
                case 6 -> showDishesCountMenu();
                case 7 -> showDrinksCountMenu();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    @Override
    public void showCargo() {
        System.out.println(getCargoInfo());
    }

    @Override
    public String getCargoInfo() {
        return "\nTables count: " + tablesCount + "("+convertWeight(tablesCount*40)+"),  Seats count: " + seatsCount + "("+convertWeight(seatsCount/2+seatsCount%2*50)+")";
    }

    public void addMenuCategoryMenu(){
        prevSection = this::MainMenu;
        System.out.println("Enter new category name: ");
        findInputExpression("(.+)", (Matcher matcher) -> {
            if(menu.containsKey(matcher.group(1)))
                error(new Exceptions.ContainsSameCategoryException().getMessage());
            else {
                addMenuCategory(matcher.group(1));
                success("Category "+matcher.group(1), "created");
            }
        }, this::addMenuCategoryMenu);
    }

    public void addMenuCategory(String name){
        menu.put(name, new ArrayList<>());
    }

    public void addDishMenu(){
        prevSection = this::MainMenu;
        if(menu.size() == 0){
            error(new Exceptions.NoMenuCategoriesCreatedException().getMessage());
        }
        else {
            System.out.println("Choose one category: ");
            showCategories();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= menu.size()) {
                    System.out.println("\nEnter data in the following order: " + Dish.showConstructorParameters());
                    findInputExpression(Dish.getParametersRegExp(), (Matcher _matcher) -> {
                        Dish dish = new Dish(_matcher);
                        menu.get(getCategoryByIndex(option-1)).add(dish);
                        success("Dish "+dish.getName() + " " + dish.getPriseString(), "created");
                    }, this::addDishMenu);
                }
                else
                    throw new WrongInputException();
            }, this::addDishMenu);

        }
    }

    public void addDish(String category_name, Dish dish){
        menu.get(category_name).add(dish);
    }

    public void showMenu(){
        prevSection = this::MainMenu;
        if(menu.size() == 0) {
            showCategories();
        }else {
            System.out.print(Color.BLACK_B);
            System.out.println("Menu: ");
            for (String str : menu.keySet()) {
                System.out.println(str + ": ");
                showCategoryDishes(str);
            }
        }
    }

    public void removeCategoryMenu(){
        prevSection = this::MainMenu;
        if(menu.size() == 0){
            showCategories();
        }else {
            System.out.print("Select one category from the list:\n");
            showCategories();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= menu.size()) {
                    String name = getCategoryByIndex(option-1);
                    menu.remove(name);
                    success("Category " + name, "removed");
                } else
                    throw new WrongInputException();
            }, this::removeCategoryMenu);
        }
    }

    public void removeDishMenu(){
        prevSection = this::MainMenu;
        if(getDishesCount() == 0){
            error(new Exceptions.NoDishesCreatedException().getMessage());
        } else {
            System.out.print("Select one category from the list:\n");
            showCategories();
            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if (option > 0 && option <= menu.size()) {
                    System.out.print("Select one dish from the list:\n");
                    String category_name = getCategoryByIndex(option-1);
                    showCategoryDishes(category_name);

                    findInputExpression("(\\d+)", (Matcher _matcher) -> {
                        int _option = Integer.parseInt(_matcher.group(1));
                        if (_option > 0 && _option <= menu.get(category_name).size()) {
                            Dish dish = menu.get(category_name).remove(_option-1);
                            success("Dish " + dish.getName() + " " + dish.getPriseString(), "removed");
                        } else
                            throw new WrongInputException();
                    }, this::removeCategoryMenu);
                } else
                    throw new WrongInputException();
            }, this::removeCategoryMenu);
        }
    }

    public void showDishesCountMenu(){
        prevSection = this::MainMenu;
        if(getDishesCount() == 0){
            error(new Exceptions.NoDishesCreatedException().getMessage());
        }
        else {
            int count = getDishesCount();
            System.out.println("Menu contains " + count + (count == 1 ? " dish" : " dishes"));
        }
    }

    public void showDrinksCountMenu(){
        prevSection = this::MainMenu;
        if(getDrinksCount() == 0){
            error(new Exceptions.NoDrinksCreatedException().getMessage());
        }
        else {
            int count = getDrinksCount();
            System.out.println("Menu contains " + count + (count == 1 ? " drink" : " drinks"));
        }
    }

    public int getDishesCount(){
        int count = 0;
        for (String str : menu.keySet()){
            for(Dish ignored : menu.get(str)){
                count++;
            }
        }
        return count;
    }

    public int getDrinksCount(){
        int count = 0;
        for (String str : menu.keySet()){
            for(Dish dish : menu.get(str)){
                if(dish.getType() == Dish.Type.Drink)
                    count++;
            }
        }
        return count;
    }

    public void showCategories(){
        if(menu.size() == 0){
            error(new Exceptions.NoMenuCategoriesCreatedException().getMessage());
        }else {
            System.out.print(Color.BLACK_B);
            int i = 1;
            for (String str : menu.keySet()) {
                System.out.println("\t" + (i++) + ". " + str);
            }
            System.out.print(Color.DEFAULT);
        }
    }

    public void showCategoryDishes(String str){
        System.out.print(Color.BLACK_B);
        int i = 1;
        for(Dish dish : menu.get(str)){
            System.out.println("\t"+(i++)+". " +dish);
        }
        if(i == 1) System.out.println("\tempty");
        System.out.print(Color.DEFAULT);
    }

    public String getCategoryByIndex(int index){
        List<String> set = menu.keySet().stream().toList();
        return set.get(index);
    }

    @Override
    public String toString(){
        return super.toString() + " | Cuisine name: " + cuisineName + " | Tables: " + tablesCount + " | Seats: " + seatsCount;
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\".+\")\\s+(\\d+)\\s+(\\d+)";
    }

    public static String showConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlightedPattern("/Y/ | Cuisine Name (://P/\"...\"://Y/) | Tables Count | Seats Count:/");
    }

    @Override
    public boolean isElectricCar() {
        return true;
    }

    public static class Exceptions {
        public static class NoMenuCategoriesCreatedException extends Exception {
            NoMenuCategoriesCreatedException() {
                super("You must create at least 1 menu category!");
            }
        }
        public static class NoDishesCreatedException extends Exception {
            NoDishesCreatedException() {
                super("No dishes created!");
            }
        }
        public static class NoDrinksCreatedException extends Exception {
            NoDrinksCreatedException() {
                super("No drinks created!");
            }
        }

        public static class ContainsSameCategoryException extends Exception {
            ContainsSameCategoryException() {
                super("Same category already created!");
            }
        }
    }

    public static RestaurantCar generate(){
        String[] cuisineNames = {"Polish", "Ukrainian", "Belarusian", "French", "Italian", "American", "Chinese", "Asian", "Spanish"};
        String cuisine_name = getRandomBetween(cuisineNames);
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int tablesCount = getRandomBetween(10, 20);
        int seatsCount = tablesCount * 4;
        try {
            RestaurantCar car = new RestaurantCar(DefaultSender, weight, cuisine_name, tablesCount, seatsCount, true);
            return car;
        }catch (Exception ignored){}

        return null;
    }
}
