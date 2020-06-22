package server;

import java.util.ArrayList;
import java.util.Map;

public class SimpleAuthService implements AuthService {

    private class UserData {
        private String login;
        private String password;
        private String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getNickname() {
            return nickname;
        }
    }

    private ArrayList<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("user1", "pass1", "usr1") );
        users.add(new UserData("user2", "pass2", "usr2") );
        users.add(new UserData("user3", "pass3", "usr3") );
        users.add(new UserData("user4", "pass4", "usr4") );
        users.add(new UserData("user5", "pass5", "usr5") );
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (int i = 0; i < users.size(); i++) {
            UserData usr = users.get(i);
            if ( usr.getLogin().equals(login) && usr.getPassword().equals(password) ) {
                return usr.getNickname();
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (int i = 0; i < users.size(); i++) {
            if ( users.get(i).login.equals(login) ) {
                return false;
            }
        }

        users.add(new UserData(login, password, nickname));
        return true;
    }
}
