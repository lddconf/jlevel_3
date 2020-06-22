package server;

public class Logger implements Loggable {
    synchronized public void printMessage(String name, String msg) {
        System.out.printf( "[%s]: %s\n", name, msg );
    }
}
