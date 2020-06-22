package server;

public interface AuthService {
    String getNickByLoginAndPassword(String login, String password);

    boolean registration(String login, String password, String nickname);
}
