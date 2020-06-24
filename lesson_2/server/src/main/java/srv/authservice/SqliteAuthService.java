package srv.authservice;

import srv.AuthService;

import java.sql.*;

public class SqliteAuthService implements AuthService {
    private Connection conn;
    private Statement stmt;

    private PreparedStatement selectNickname;
    private PreparedStatement createUser;
    private PreparedStatement searchNickByLogin;
    private PreparedStatement setNickByLogin;

    public SqliteAuthService() {
        try {
            open();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `users` " +
                                   "( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                     "login TEXT NOT NULL, " +
                                     "nick TEXT NOT NULL, " +
                                     "pass TEXT NOT NULL );");

            selectNickname = conn.prepareStatement("SELECT nick FROM users WHERE login = ? AND pass = ?;");
            createUser = conn.prepareStatement("INSERT INTO users (login, nick, pass) VALUES (?,?,?)");
            searchNickByLogin = conn.prepareStatement("SELECT nick FROM users WHERE login = ?;");
            setNickByLogin = conn.prepareStatement("UPDATE users SET nick = ? WHERE login = ?");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            close();
        }
    }

    public synchronized void open() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
        stmt = conn.createStatement();
    }

    public synchronized void close() {
        if ( conn != null ) {
            try {
                conn.close();
            } catch (SQLException throwables) {
            }
            stmt = null;
            conn = null;
        }
    }

    @Override
    public synchronized String getNickByLoginAndPassword(String login, String password) {
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

    @Override
    public synchronized boolean registration(String login, String password, String nickname) {
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

    @Override
    public synchronized boolean changeNickForLogin(String login, String newNickName) {
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
