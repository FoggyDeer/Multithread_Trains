package Cars;

import BasicClasses.Locomotive;
import BasicClasses.Menu;
import BasicClasses.StaticArrayList;
import CargoClasses.Container;

import java.util.regex.Matcher;

public class BasicFreightCar extends Car{
    protected final double maxCargoWeight = 80_000;
    protected final int max_containers;
    protected final StaticArrayList<Container> containers;

    public BasicFreightCar(String sender, double tare_weight, int max_containers) {
        super("Wagon Towarowy Podstawowy", sender, tare_weight);
        this.max_containers = max_containers;
        containers = new StaticArrayList<>(max_containers);
    }

    public BasicFreightCar(String sender, double tare_weight, int max_containers, boolean ignoreEvent) {
        super("Wagon Towarowy Podstawowy", sender, tare_weight, ignoreEvent);
        this.max_containers = max_containers;
        containers = new StaticArrayList<>(max_containers);
    }

    public BasicFreightCar(String type, String sender, double tare_weight, int max_containers) {
        super(type, sender, tare_weight);
        this.max_containers = max_containers;
        this.containers = new StaticArrayList<>(max_containers);
    }

    public BasicFreightCar(String type, String sender, double tare_weight, int max_containers, boolean ignoreEvent) {
        super(type, sender, tare_weight, ignoreEvent);
        this.max_containers = max_containers;
        this.containers = new StaticArrayList<>(max_containers);
    }

    public BasicFreightCar(Matcher matcher) {
        this(matcher.group(1), Double.parseDouble(matcher.group(2)),Integer.parseInt(matcher.group(3)));
    }

    @Override
    public void MainMenu() {
        prevSection = this::exit;
        System.out.println("\n"+
                Text.disabled("1.Add container | ", containers.size() >= max_containers) +
                Text.disabled("2.Show containers | 3.Delete container | 4.Sort cargo by product name | 5.Sort cargo by weight", containers.size() == 0));

        findInputExpression("([1-5])", (Matcher matcher) -> {
            switch (Integer.parseInt(matcher.group(1))){
                case 1 -> addContainerMenu();
                case 2 -> showContainersMenu();
                case 3 -> deleteContainerMenu();
                case 4 -> sortCargoByName();
                case 5 -> sortCargoByWeight();
                default -> throw new WrongInputException();
            }
        }, this::MainMenu);
    }

    @Override
    public void showCargo() {
        System.out.print(Menu.Color.BLACK_B);
        System.out.println(getCargoInfo());
        System.out.print(Menu.Color.DEFAULT);
    }

    @Override
    public String getCargoInfo(){
        StringBuilder sb = new StringBuilder("\n").append(containers.get(0).getName()).append("s: ");
        for (int i = 0; i < containers.size(); i++) {
            sb.append("\n\t").append(i + 1).append(". ").append(containers.get(i));
        }
        return sb.toString();
    }

    public void addContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() >= max_containers) {
            error(new Exceptions.OutOfMaxContainersCountException("container",max_containers).getMessage());
        } else {
            System.out.println("\nEnter data in the following order: " + Container.showConstructorParameters());

            findInputExpression(Container.getParametersRegExp(), (Matcher matcher) -> {
                Container container = new Container(matcher);
                if(cargoIsOutOfLimit(container)) {
                    error(new Exceptions.CargoWeightIsOutOfLimitException(maxCargoWeight).getMessage());
                }else {
                    addContainer(container);
                    success("Container with " + container.getCargoName() + " (" + convertWeight(container.getWeight()) + ")", "created");
                }
            }, this::addContainerMenu);
        }
    }

    public void addContainer(Container container) throws Car.Exceptions.CarIsOnTheWayException, Locomotive.Exceptions.OutOfMaxPullWeight {
        containers.add(container);
        increaseNetWeight(container.getWeight());
    }

    public void showContainersMenu() {
        prevSection = this::MainMenu;
        if(containers.size() == 0){
            error(new Exceptions.NoContainersAddedException().getMessage());
        } else {
            showCargo();
        }
    }

    public void deleteContainerMenu() {
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoContainersAddedException().getMessage());
        } else {
            System.out.print("Select one "+containers.get(0).getName()+" from the list:\n");
            showContainersMenu();

            findInputExpression("(\\d+)", (Matcher matcher) -> {
                int option = Integer.parseInt(matcher.group(1));
                if(option > 0 && option <= containers.size()){
                    Container container = containers.remove(option-1);
                    reduceNetWeight(container.getWeight());
                    success(container.getName()+" with "+container.getCargoName()+" ("+ convertWeight(container.getWeight())+")", "removed");
                }
                else
                    throw new WrongInputException();
            }, this::deleteContainerMenu);
        }
    }

    public void sortCargoByName() {
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoContainersAddedException().getMessage());
        } else {
            prevSection = this::MainMenu;
            containers.sort((cont1, cont2) -> cont1.getCargoName().compareTo(cont2.getCargoName()));
            success("Every "+containers.get(0).getName(), "sorted by name");
        }
    }

    public void sortCargoByWeight() {
        prevSection = this::MainMenu;
        if (containers.size() == 0) {
            error(new Exceptions.NoContainersAddedException().getMessage());
        } else {
            prevSection = this::MainMenu;
            containers.sort((cont1, cont2) -> Double.compare(cont2.getWeight(), cont1.getWeight()));
            success("Every container", "sorted by weight");
        }
    }

    public boolean cargoIsOutOfLimit(Container container) {
        return container.getWeight() + countCargoWeight() > maxCargoWeight;
    }

    public double countCargoWeight() {
        return containers.stream().mapToDouble((_container) -> _container.getWeight()).sum();
    }

    @Override
    public String toString(){
        return super.toString() + " | Max containers: " + max_containers;
    }

    public static String getConstructorParameters(){
        return Car.getConstructorParameters() + Text.highlighted(" | Max containers count (max 80 tons)");
    }

    public static String getParametersRegExp(){
        return Car.getParametersRegExp() + "\\s+(\\d+)";
    }

    public static String showConstructorParameters(){
        return getConstructorParameters();
    }

    @Override
    public boolean isElectricCar() {
        return false;
    }

    protected static class Exceptions extends Exception{
        public static class NoContainersAddedException extends Exception {
            NoContainersAddedException() {
                super("You need to add at least 1 load!");
            }
        }
        public static class CargoWeightIsOutOfLimitException extends Exception {
            CargoWeightIsOutOfLimitException(double max) {
                super("Weight exceeds maximum lifting weight of car: "+max+"!");
            }
        }
        public static class OutOfMaxContainersCountException extends Exception {
            OutOfMaxContainersCountException(String containerName, int max) {
                super("Maximum count of "+containerName+"s is already reached: "+max+"!");
            }
        }
    }

    public static BasicFreightCar generate() {
        double weight = roundDouble(getRandomBetween(20, 45), 2);
        int max_containers = getRandomBetween(20, 45);
        BasicFreightCar car = new BasicFreightCar(DefaultSender, weight, max_containers, true);
        for (int i = max_containers; i > 0; i--){
            try {
                car.addContainer(Container.generate());
            }catch (Exception ignored){}
        }
        return car;
    }
}
