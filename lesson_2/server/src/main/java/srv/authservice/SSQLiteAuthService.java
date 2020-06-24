package srv.authservice;

import srv.AuthService;
import srv.SQLAdapter;

public class SSQLiteAuthService implements AuthService {
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return SQLAdapter.getNickByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return SQLAdapter.registration(login, password, nickname);
    }

    @Override
    public boolean changeNickForLogin(String login, String newNickName) {
        return SQLAdapter.changeNickForLogin(login, newNickName);
    }
}
