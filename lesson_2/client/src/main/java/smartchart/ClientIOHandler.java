package smartchart;

import java.net.ConnectException;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientIOHandler {
    private Loggable view;
    private Socket socket;
    private boolean authenticated = false;
    private String nick;
    final String IP_ADDRESS = "localhost";
    final int PORT = 10000;

    private String socketInfo;
    private Controller controller;
    private DataInputStream  istream;
    private DataOutputStream ostream;
    private Thread t;

    public String getNick() {
        return nick;
    }

    public ClientIOHandler(Loggable view, Controller controller) {
        this.view = view;
        this.controller = controller;
        try {
            socket = new Socket(IP_ADDRESS, PORT);;
            istream = new DataInputStream(socket.getInputStream());
            ostream = new DataOutputStream(socket.getOutputStream());
            socketInfo = socket.getInetAddress() + ":" + socket.getPort();
            t = new Thread(() ->{
                try {
                    //Auth loop
                    while (!Thread.interrupted()) {
                        String str = istream.readUTF();
                        if ( str.startsWith("/regOk")) {
                            view.printMessage("I'm", "Registration succeed");
                            continue;
                        }

                        if ( str.startsWith("/regErr ")) {
                            String[] tokens = str.split("\\s", 2);
                            if ( tokens.length != 2 ) {
                                view.printMessage("I'm", "Invalid registration data");
                            } else {
                                view.printMessage("I'm", tokens[1]);
                            }
                            continue;
                        }

                        if ( str.startsWith("/authOk ")) {
                            String[] tokens = str.split("\\s", 2);
                            if ( tokens.length != 2 ) {
                                view.printMessage("I'm", "Authentication error");
                                continue;
                            }
                            nick = tokens[1];
                            view.printMessage("I'm", "Authentication accepted");
                            controller.setAuthenticated(true);
                            authenticated = true;
                            break;
                        }
                        view.printMessage("I'm", "Authentication error");
                    }

                    //Main loop
                    while (!Thread.interrupted()) {
                        String str = istream.readUTF();
                        if ( str.equals("/end") ) {
                            break;
                        }

                        if (str.startsWith("/from ")) {
                            String[] tokens = str.split("\\s", 3);
                            if ( tokens.length == 3 ) {
                                view.printMessage(tokens[1], tokens[2]);
                            }
                        }

                        if (str.startsWith("/wErr ")) {
                            String[] tokens = str.split("\\s", 4);
                            if ( tokens.length == 4 ) {
                                view.printMessage(tokens[2], tokens[3]);
                            }
                        }

                        if (str.startsWith("/clientlistonline ")) {
                            String[] tokens = str.split("\\s" );
                            if ( tokens.length > 1 ) {
                                controller.addOnlineUsers(Arrays.copyOfRange(tokens, 1, tokens.length));
                            }
                        }

                        if (str.startsWith("/clientlistoffline ")) {
                            String[] tokens = str.split("\\s" );
                            if ( tokens.length > 1 ) {
                                controller.removeOfflineUsers(Arrays.copyOfRange(tokens, 1, tokens.length));
                            }
                        }

                        if (str.startsWith("/nickChanged ")) {
                            String[] tokens = str.split("\\s", 3 );
                            if ( tokens.length == 3 ) {
                                controller.removeOfflineUsers(Arrays.copyOfRange(tokens, 1, 2));
                                controller.addOnlineUsers(Arrays.copyOfRange(tokens, 2, 3));
                            }
                        }

                        if (str.startsWith("/nickChangedOk ")) {
                            String[] tokens = str.split("\\s", 2 );
                            if ( tokens.length == 2 ) {
                                view.printMessage(nick, "changed to " + tokens[1]);
                                nick = tokens[1];
                                controller.setNickName(nick);
                            }
                        }

                        if (str.startsWith("/nickChangeErr ")) {
                            view.printMessage(nick, "nick change error");
                        }
                    }
                } catch (EOFException e ) {
                } catch (ConnectException e) {
                   view.printMessage("I'm","Can't connect to: " + IP_ADDRESS + ":" + PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }  finally {
                    try {
                        view.printMessage("I'm","Connection is now closed: " + socketInfo );
                        if ( socket != null ) {
                            socket.close();
                        }
                    } catch (IOException e) {

                    } finally {
                        socket = null;
                        controller.setAuthenticated(false);
                        authenticated = false;
                    }
                }
            });
            t.start();
        } catch (IOException e) {
            view.printMessage("I'm","Can't connect to: " + IP_ADDRESS + ":" + PORT);
            socket = null;
        }
    }

    public boolean isConnected() {
        return socket != null;
    }

    public void tryAuthenticate(String login, String password) {
        if (( !authenticated ) && (socket != null ) && (socket.isConnected())) {
            sendMessage("/auth " + login + " " + password);
        }
    }

    public void sendMessageToUsers( String msg ) {
        sendMessage(msg);

        if (msg.startsWith("/w ")) {
            String[] tokens = msg.split("\\s", 3);
            if ( tokens.length == 3 ) {
                view.printMessage("I'm->"+tokens[1], tokens[2]);
                return;
            }
        }
        view.printMessage("I'm", msg);
    }

    private void sendMessage( String msg ) {
        try {
            ostream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryRegistration(String login, String password, String nickname) {
        if (( !authenticated ) && (socket != null ) && (socket.isConnected())) {
            sendMessage("/reg " + login + " " + password + " " + nickname);
        }
    }

    public void tryChangeNickName(String newNickname) {
        if (( authenticated ) && (socket != null ) && (socket.isConnected())) {
            sendMessage("/newNick " + newNickname);
        }
    }

    public void disconnect()  {
        if ( isConnected() ) {
            sendMessage("/end");
            if ( socket != null ) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                socket = null;
            }
            t.interrupt();
            controller.setAuthenticated(false);
            authenticated = false;
        }
    }
}
