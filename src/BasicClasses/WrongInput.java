package BasicClasses;

public class WrongInput extends Menu{

    private static boolean hasLastError = false;
    private static Function lastAction;
    private static String lastError = "";

    public static void notify(Function lastAction, String lastError){
        WrongInput.lastAction = lastAction;
        WrongInput.lastError = lastError == null ? "" : lastError;
        hasLastError = true;
    }

    public static void invoke() {
        if(hasLastError) {
            hasLastError = false;
            error("Wrong input! " + lastError);
            lastError = "";
            lastAction.call();
        }
    }

    public static boolean hasLastError() {
        return hasLastError;
    }
}

