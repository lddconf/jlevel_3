package srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private Logger view;
    private SqliteAuthService authService;

    private Thread mainRing;

    //IOHandler handler;
    HashSet<IOHandler> clients;

    public Server( int port ) {
        this.port = port;
        view = new Logger();

        //authService = new SimpleAuthService();
        authService = new SqliteAuthService();
        clients = new HashSet<>();
        mainRing = new Thread(()->{
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server has been started on port " + port);

                while ( true ) {
                    socket = serverSocket.accept();
                    new IOHandler(view, socket, this);
                    System.out.println("Client is now connected: " + socket.getInetAddress() + ":" + socket.getPort() );
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                    authService.close();
                } catch (IOException e ) {
                    e.printStackTrace();
                }
            }
        });
        mainRing.start();
    }

    public void sendMessageToAllClients(String fromNick, String message) {
            clients.forEach((handler)-> {
                synchronized (clients) {
                    if ( !handler.getNick().equals(fromNick)) {
                        handler.sendMessage(fromNick, message);
                    }
                }
            });
    }

    public boolean sendMessageTo(String fromNick, String toNick, String message) {
        if ( toNick == "Server") return true;
        boolean status = false;

        synchronized (clients) {
            Iterator<IOHandler> iterator = clients.iterator();
            IOHandler handler;
            while ( iterator.hasNext() ) {
                handler = iterator.next();
                if ( handler.getNick().equals(toNick)) {
                    handler.sendMessage(fromNick, message);
                    status = true;
                }
            }
        }
        return status;
    }

    public void subscribe( IOHandler handler ) {
        StringBuilder nicknamesList = new StringBuilder();
        synchronized (clients) {
            //Send all users info about new user
            Iterator<IOHandler> iterator = clients.iterator();
            while ( iterator.hasNext() ) {
                nicknamesList.append( iterator.next().getNick() );
                nicknamesList.append(" ");
            }

            //Inform online users
            iterator = clients.iterator();
            while ( iterator.hasNext() ) {
                iterator.next().sendOnlineUserList(1, handler.getNick());
            }
            clients.add(handler);
        };
        //Send new user info about existed users
        handler.sendOnlineUserList(1, nicknamesList.toString().trim());
    }

    public void unsubscribe(IOHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
            //Send all users info about new user
            Iterator<IOHandler> iterator = clients.iterator();
            while ( iterator.hasNext() ) {
                iterator.next().sendOnlineUserList(0, handler.getNick());
            }
        };
    }


    public String getNickNameFor( String login, String password ) {
        synchronized (authService) {
            return authService.getNickByLoginAndPassword( login, password );
        }
    }

    public boolean registerNewUser( String login, String password, String nickname ) {
        synchronized (authService) {
            return authService.registration(login, password, nickname);
        }
    }

    public boolean newNickName( String login, String newNickName, String oldNickName, IOHandler handler ) {
        boolean result;
        synchronized (authService) {
            result = authService.changeNickForLogin(login, newNickName);
        }
        if ( !result ) return result;

        synchronized (clients) {
            //Inform online users
            Iterator<IOHandler> iterator = clients.iterator();
            while ( iterator.hasNext() ) {
                IOHandler nextHandler = iterator.next();
                if ( nextHandler != handler ) {
                    nextHandler.sendUserNickNameChanged(oldNickName, newNickName);
                }
            }
        }
        return true;
    }
}
