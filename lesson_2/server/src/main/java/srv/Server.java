package srv;

import srv.authservice.SSQLiteAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private Logger view;
    private SSQLiteAuthService authService;

    private Thread mainRing;

    //IOHandler handler;
    HashSet<IOHandler> clients;

    public Server( int port ) {
        this.port = port;
        view = new Logger();

        //authService = new SimpleAuthService();
        clients = new HashSet<>();
        mainRing = new Thread(()->{
            try {
                if ( !SQLAdapter.connect() ) return;
                authService = new SSQLiteAuthService();
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
                    SQLAdapter.disconnect();
                    serverSocket.close();
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
            SQLAdapter.addMessage(fromNick, SQLAdapter.BROAD_CAST_USER_NICK, message);
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
                    handler.sendPrivateMessage(fromNick, message);
                    status = true;
                }
            }
        }
        if ( status ) {
            SQLAdapter.addMessage(fromNick, toNick, message);
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
        return authService.getNickByLoginAndPassword( login, password );
    }

    public boolean registerNewUser( String login, String password, String nickname ) {
        return authService.registration(login, password, nickname);
    }

    public boolean newNickName( String login, String newNickName, String oldNickName, IOHandler handler ) {
        if ( !authService.changeNickForLogin(login, newNickName) ) {
            return false;
        }
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
