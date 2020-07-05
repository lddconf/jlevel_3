package srv;

public interface Loggable {
    void printMessage(String name, String msg);

    void printError(String name, String msg);

    void printInfo(String name, String msg);
    void printInfo(String msg);
}
