package server;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10000);


        Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();
            if ( message.length() != 0 ) {
                server.sendMessageToAllClients("Server", message);
            }
        }
    }
}
