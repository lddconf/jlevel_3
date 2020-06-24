package srv;

//import javafx.scene.Parent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class IOHandler {
    static final int AUTH_TIMEOUT_MS = 120 * 1000;
    static final int WORK_TIMEOUT_MS = 1800 * 1000;

    private Loggable view;
    private Socket socket;
    private Server server;
    private String nick;
    private String login;
    private String connectionInfo;
    private DataInputStream  istream;
    private DataOutputStream ostream;
    private boolean doStuff;
    private Thread t;

    public IOHandler(Loggable log, Socket socket, Server server) {
        this.view = log;
        this.socket = socket;
        this.server = server;
        this.nick = null;
        this.doStuff = true;

        connectionInfo = socket.getInetAddress() + ":" + socket.getPort();
        try {
            istream = new DataInputStream(socket.getInputStream());
            ostream = new DataOutputStream(socket.getOutputStream());


            t = new Thread(() ->{
                try {
                    //Auth Log
                    socket.setSoTimeout(AUTH_TIMEOUT_MS ); //Set timeout
                    while (!Thread.interrupted()) {
                        String str = istream.readUTF();

                        //Registration
                        if ( str.startsWith( "/reg" )) {
                            String[] tokens = str.split("\\s");
                            if ( tokens.length != 4 ) {
                                log.printMessage(connectionInfo, "Registration error");
                                sendMessage("/regErr Invalid Registration data");
                                continue;
                            }

                            if ( !server.registerNewUser(tokens[1], tokens[2], tokens[3]) ) {
                                log.printMessage(connectionInfo, "Registration error");
                                sendMessage("/regErr Login already in use");
                                continue;
                            }

                            log.printMessage(connectionInfo, "User \"" + tokens[3] + "\" " + "registered");
                            sendMessage("/regOk ");
                            continue;
                        }

                        //Authentication
                        if ( !str.startsWith("/auth ")) {
                            log.printMessage(connectionInfo, "Authentication error");
                            sendMessage("/authErr Invalid authentication data");
                            continue;
                        }
                        String[] tokens = str.split("\\s");
                        if ( tokens.length != 3 ) {
                            log.printMessage(connectionInfo, "Authentication error");
                            sendMessage("/authErr Invalid authentication data");
                            continue;
                        }

                        synchronized (this) {
                            nick = server.getNickNameFor(tokens[1], tokens[2]);
                            login = tokens[1];
                        }

                        if ( nick == null ) {
                            log.printMessage(connectionInfo, "Invalid login or password");
                            sendMessage("/authErr Invalid login or password");
                            continue;
                        }

                        log.printMessage(connectionInfo, "User \"" + nick + "\" " + "connected");
                        sendMessage("/authOk "+nick);
                        break;
                    }
                    //Disable socket timeout
                    socket.setSoTimeout(WORK_TIMEOUT_MS);
                    server.subscribe(this);
                    while (!Thread.interrupted()) {
                        String str = istream.readUTF();
                        if ( str.equals("/end") ) {
                            break;
                        }

                        if (str.startsWith("/newNick ")) {
                            String[] tokens = str.split("\\s", 2);
                            if ( tokens.length == 2 ) {
                                if ( server.newNickName(login, tokens[1], nick, this) ) {
                                    log.printMessage(nick,  nick + " switched to " + tokens[1] );
                                    sendMessage("/nickChangedOk " + tokens[1]);
                                    nick = tokens[1];
                                } else {
                                    log.printMessage(nick,  nick + " error switching to " + tokens[1] );
                                    sendMessage("/nickChangeErr Invalid or duplicate nickname specified");
                                }
                            } else {
                                log.printMessage(nick,  nick + " error switching to " + tokens[1] );
                                sendMessage("/nickChangeErr Invalid command");
                            }
                            continue;
                        }

                        if (str.startsWith("/w ")) {
                            String[] tokens = str.split("\\s", 3);
                            if ( tokens.length == 3 ) {
                                if ( server.sendMessageTo(nick, tokens[1], tokens[2]) ) {
                                    log.printMessage(nick + "->" + tokens[1] , str);
                                } else {
                                    sendMessage("/wErr " + "User " + tokens[1] + " not found/connected");
                                    log.printMessage(nick + "->" + tokens[1], "[Dest user not found/connected]");
                                }
                            }
                        } else {
                            log.printMessage(nick , str);
                            server.sendMessageToAllClients(nick, str);
                        }
                    }
                } catch (EOFException e ) {

                } catch (IOException e) {

                }  finally {
                    try {
                        System.out.println("Connection is now closed: " + connectionInfo );
                        if ( server != null ) {
                            server.unsubscribe(this);
                        }
                        socket.close();
                    } catch (IOException e) {

                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage( String from, String msg ) {
        this.sendMessage("/from " + from + " " + msg);
    }

    private void sendMessage( String msg ) {
        try {
            ostream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOnlineUserList( int status, String userList) {
        if ( status > 0 ) {
            this.sendMessage("/clientlistonline " +  userList );
        } else {
            this.sendMessage("/clientlistoffline " +  userList );
        }
    }

    public void sendUserNickNameChanged( String fromNickName, String toNickName ) {
        this.sendMessage("/nickChanged " + fromNickName + " " +  toNickName );
    }

    public void shutDown() {
        sendMessage( "/end");
        if ( server != null ) {
            server.unsubscribe(this);
        }
        try {
            doStuff = false;
            if (( t != null ) && (!t.isInterrupted() )) t.interrupt();
            socket.close();
        } catch (IOException e) {

        }

        System.out.println("Connection is now closed: " + connectionInfo );
    }

    public synchronized String getNick() {
        return nick;
    }
}
