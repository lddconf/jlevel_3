package srv;

import java.sql.*;
import java.util.HashMap;

public class SQLAdapter {
    private static Connection conn;
    private static Statement stmt;

    private static PreparedStatement psSelectNickname;
    private static PreparedStatement psCreateUser;
    private static PreparedStatement psSearchNickByLogin;
    private static PreparedStatement psSetNickByLogin;

    private static PreparedStatement psAddMessage;
    private static PreparedStatement psGetMessagesForNickName;

    public static final String BROAD_CAST_USER_NICK = "BROADCAST";

    private static void prepareAllStatement() throws SQLException {
        psSelectNickname = conn.prepareStatement("SELECT nick FROM users WHERE login = ? AND pass = ?;");
        psCreateUser = conn.prepareStatement("INSERT INTO users (login, nick, pass) VALUES (?,?,?)");
        psSearchNickByLogin = conn.prepareStatement("SELECT nick FROM users WHERE login = ?;");
        psSetNickByLogin = conn.prepareStatement("UPDATE users SET nick = ? WHERE login = ?");

        psAddMessage = conn.prepareStatement("INSERT INTO messages(`from`, `to`, message) " +
                "SELECT * " +
                "FROM " +
                "(SELECT id FROM users WHERE users.nick = ?) t1" +
                " JOIN" +
                "(SELECT id FROM users WHERE users.nick = ?) t2" +
                " JOIN " +
                "(SELECT ?) t3;");

        psGetMessagesForNickName = conn.prepareStatement(
                "SELECT " +
                        "users.nick as `from`," +
                        "message " +
                    "FROM " +
                        "messages " +
                    "JOIN users ON messages.`from` = users.id " +
                    "WHERE messages.`to` IN ( " +
                        "select id from users where " +
                        "users.nick = ? or users.nick = \"" + BROAD_CAST_USER_NICK +"\" );"
        );
    }


    public static synchronized boolean connect()  {
        try {
                disconnect();
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
                stmt = conn.createStatement();
                int result = stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `users` " +
                            "( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "login TEXT NOT NULL UNIQUE, " +
                            "nick TEXT NOT NULL UNIQUE, " +
                            "pass TEXT NOT NULL );");


                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `messages` (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
                        "`from` INTEGER NOT NULL," +
                        "`to` INTEGER NOT NULL," +
                        "`message` TEXT NOT NULL," +
                        "`created_at` TEXT DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(`from`) REFERENCES `users`(`id`)," +
                        "FOREIGN KEY(`to`) REFERENCES `users`(`id`)" +
                        ");");
                prepareAllStatement();

                //Add broadcast messages
                registration(BROAD_CAST_USER_NICK, BROAD_CAST_USER_NICK, BROAD_CAST_USER_NICK);

                return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            disconnect();
        }
        return false;
    }

    public static synchronized void disconnect() {
        if ( conn != null ) {
            try {
                conn.close();
            } catch (SQLException throwables) {
            } finally {
                stmt = null;
                conn = null;
            }

        }
    }

    public synchronized static String getNickByLoginAndPassword(String login, String password) {
        if ( stmt != null ) {
            try {
                if ( login.equals(BROAD_CAST_USER_NICK) ) {
                    return null;
                }
                psSelectNickname.setString(1, login);
                psSelectNickname.setString(2, password);
                ResultSet rs = psSelectNickname.executeQuery();
                if ( rs.next() ) {
                    return rs.getString("nick");
                }
            } catch (SQLException throwables) {
                return null;
            }
        }
        return null;
    }

    public synchronized static boolean registration(String login, String password, String nickname) {
        if ( stmt != null ) {
            try {
                //Try to find already registered
                psSearchNickByLogin.setString(1, login);
                ResultSet rs = psSearchNickByLogin.executeQuery();
                if ( rs.next() ) return false;

                //Create a new one
                psCreateUser.setString(1, login);
                psCreateUser.setString(2, nickname);
                psCreateUser.setString(3, password);
                if ( psCreateUser.executeUpdate() > 0 ) {
                    return true;
                }
            } catch (SQLException throwables) {
            }
        }
        return false;
    }

    public synchronized static boolean changeNickForLogin(String login, String newNickName) {
        if ( stmt != null ) {
            try {
                if ( login.equals(BROAD_CAST_USER_NICK) ) {
                    return false;
                }
                psSetNickByLogin.setString(1, newNickName);
                psSetNickByLogin.setString(2, login);
                int rs = psSetNickByLogin.executeUpdate();
                if ( rs > 0 ) {
                    return true;
                }
            } catch (SQLException throwables) {
            }
        }
        return false;
    }

    public synchronized static boolean addMessage( String from, String to, String message ) {
        if ( stmt != null ) {
            try {
                psAddMessage.setString(1, from);
                psAddMessage.setString(2, to);
                psAddMessage.setString(3, message);
                int rs = psAddMessage.executeUpdate();
                if ( rs > 0 ) {
                    return true;
                }
            } catch (SQLException throwables) {
            }
        }
        return false;
    }

    public synchronized static HashMap<String, String> getMessagesForNickName(String nickname) {
        HashMap<String, String> messages = new HashMap<>();
        if ( stmt != null ) {
            try {
                psGetMessagesForNickName.setString(1, nickname);
                ResultSet rs = psGetMessagesForNickName.executeQuery();

                while (rs.next()) {
                  messages.put(rs.getString(1), rs.getString(2));
                }
                return messages;
            } catch (SQLException throwables) {

            }
        }
        return messages;
    }
}
