package srv;

import java.sql.*;

public class SQLAdapter {
    private static Connection conn;
    private static Statement stmt;

    private static PreparedStatement selectNickname;
    private static PreparedStatement createUser;
    private static PreparedStatement searchNickByLogin;
    private static PreparedStatement setNickByLogin;

    private static void prepareAllStatement() throws SQLException {
        selectNickname = conn.prepareStatement("SELECT nick FROM users WHERE login = ? AND pass = ?;");
        createUser = conn.prepareStatement("INSERT INTO users (login, nick, pass) VALUES (?,?,?)");
        searchNickByLogin = conn.prepareStatement("SELECT nick FROM users WHERE login = ?;");
        setNickByLogin = conn.prepareStatement("UPDATE users SET nick = ? WHERE login = ?");
    }


    public static synchronized boolean connect()  {
        try {
                disconnect();
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
                stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `users` " +
                        "( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "login TEXT NOT NULL UNIQUE, " +
                        "nick TEXT NOT NULL UNIQUE, " +
                        "pass TEXT NOT NULL );");
                prepareAllStatement();
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
                selectNickname.setString(1, login);
                selectNickname.setString(2, password);
                ResultSet rs = selectNickname.executeQuery();
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
                searchNickByLogin.setString(1, login);
                ResultSet rs = searchNickByLogin.executeQuery();
                if ( rs.next() ) return false;

                //Create a new one
                createUser.setString(1, login);
                createUser.setString(2, nickname);
                createUser.setString(3, password);
                if ( createUser.executeUpdate() > 0 ) {
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
                setNickByLogin.setString(1, newNickName);
                setNickByLogin.setString(2, login);
                int rs = setNickByLogin.executeUpdate();
                if ( rs > 0 ) {
                    return true;
                }
            } catch (SQLException throwables) {
            }
        }
        return false;
    }


}
