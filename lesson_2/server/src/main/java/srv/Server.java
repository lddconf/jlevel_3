package srv;

import srv.authservice.SSQLiteAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Server implements Runnable {
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private LoggerView view;
    private static final Logger logger;
    private SSQLiteAuthService authService;

    //private Thread mainRing;

    private ExecutorService thread_pool;

    private ExecutorService mainRing; //Не обязательно переходить на thread pool, было бы лучше оставить Thread
    //IOHandler handler;
    HashSet<IOHandler> clients;

    static {
        logger = Logger.getLogger(Server.class.getName());
    }

    public Server( int port ) {
        this.port = port;
        view = new LoggerView();
        thread_pool = Executors.newCachedThreadPool();
        //authService = new SimpleAuthService();
        clients = new HashSet<>();
        //Старая версия кода
        //mainRing = new Thread(this);
        //mainRing.start();

        //Главный цикл через ExecurtorService
        thread_pool.execute(this);

    }

    @Override
    public void run() {
        try {
            if ( !SQLAdapter.connect() ) return;
            authService = new SSQLiteAuthService();
            serverSocket = new ServerSocket(port);

            view.printInfo("Server has been started on port " + port);

            while ( true ) {
                socket = serverSocket.accept();
                //Через executor service
                thread_pool.execute(new IOHandler(socket, this));
                view.printInfo("Client is now connected: " + socket.getInetAddress() + ":" + socket.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                view.printError("Critical", "Shutdown server");
                SQLAdapter.disconnect();
                serverSocket.close();
                thread_pool.shutdown();;
            } catch (IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public LoggerView getView() {
        return view;
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
