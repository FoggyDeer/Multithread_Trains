import BasicClasses.InteractiveMenu;
import BasicClasses.RailroadMap;

public class Main {
    /**
     * <p><b>MENU </b>(<i>type <b>"exit"</b> or <b>"quit"</b> to close the program, to go to previous section type <b>"/"</b> or press <b>"ENTER"</b></i>)</p>
     * <p>(1) - Locomotives
     * <p>---- (1.1) - Create locomotive
     * <p>---- (1.2) - Show Available locomotives
     * <p>---- (1.3) - Delete locomotive
     * <p>(2) - Cars
     * <p>---- (2.1) - Create car
     * <p>---- (2.2) - Show Available cars
     * <p>---- (2.3) - Edit car load
     * <p>---- (2.4) - Delete car
     * <p>---- (2.5) - Show broken cars
     * <p>(3) - Trains
     * <p>---- (3.1) - Create train
     * <p>---- (3.2) - Show Available trains
     * <p>---- (3.3) - Edit train cars load
     * <p>---- (3.4) - Delete locomotive
     * <p>(4) - Stations
     * <p>---- (4.1) - Create station
     * <p>---- (4.2) - Edit station connections
     * <p>-------- (4.2.1) - Connect stations
     * <p>-------- (4.2.2) - Remove connection
     * <p>---- (4.3) - Show Available stations
     * <p>---- (4.4) - Delete station
     * <p>(5) - Railroad
     * <p>---- (5.1) - Generate stations
     * <p>---- (5.2) - Generate train
     * <p>---- (5.3) - Start the train
     * <p>---- (5.4) - Show trains info
     **/
    public static void main(String[] args) {
        InteractiveMenu.generateTrains(25);
        RailroadMap.generate();
        RailroadMap.connectAll();
        InteractiveMenu.start();
    }
}
