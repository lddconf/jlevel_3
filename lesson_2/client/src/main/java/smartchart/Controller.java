package smartchart;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

public class Controller implements Initializable, Loggable {
    @FXML
    public PasswordField passwordField;

    @FXML
    public TextField loginTextField;

    @FXML
    public Button loginButton;

    @FXML
    public Label logintxt;

    @FXML
    public Label passwordtxt;

    @FXML
    public MenuItem menuRegistration;

    @FXML
    public MenuItem connect;

    @FXML
    public ComboBox<String> usersList;

    @FXML
    public MenuItem menuChangeNickName;

    @FXML
    private Button button;

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;
    private ClientIOHandler handler;
    private RegistrationController registrationController;
    private Stage regStage;

    private SwitchNickNameController newNickNameController;
    private Stage nicknameStage;

    private RingHistoryLogger logger;

    static final String SEND_TO_ALL = " ALL";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authenticatedChangedNotify(false);
        regStage = createRegWindow();
        nicknameStage = createNewNicknameWindow();
        setTitle("Not connected");


        Platform.runLater(() -> {
            Stage stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("bue");
                    if ( handler != null && handler.isConnected() ) {
                        handler.disconnect();
                    }
                }
            });
        });
    }

    public void nickNameChangedNotify(String nickName) {
        setTitle(nickName);
    }

    public void authenticatedChangedNotify(boolean status) {
        textField.setVisible(status);
        textField.setManaged(status);
        button.setVisible(status);
        button.setManaged(status);
        usersList.setManaged(status);
        usersList.setVisible(status);

        loginTextField.setVisible(!status);
        passwordField.setVisible(!status);
        loginTextField.setManaged(!status);
        passwordField.setManaged(!status);
        logintxt.setVisible(!status);
        logintxt.setManaged(!status);
        passwordtxt.setVisible(!status);
        passwordtxt.setManaged(!status);
        loginButton.setVisible(!status);
        loginButton.setManaged(!status);


        if (status) {
            setTitle(handler.getNick());
            restoreMessages(handler.getCurrentLogin());
        } else {
            if ( logger != null ) {
                logger.flushToFile();
                logger = null;
            }
        }
        menuRegistration.setDisable(status);
        menuChangeNickName.setDisable(!status);
    }

    private void restoreMessages(String login) {
        if ( logger == null ) {
            logger = new RingHistoryLogger(login);
            logger.restoreFromFileLastRecords(100);

            Platform.runLater(()->{
                textArea.clear();
                String[] messages = logger.getMessages();
                for (int i = 0; i < messages.length; ++i) {
                    textArea.appendText(messages[i] + "\n");
                }
            });
        }
    }

    static void setupErrorView(boolean disable, TextField txt) {
        ObservableList<String> styleClass = txt.getStyleClass();
        if ( !disable ) {
            if (!styleClass.contains("error")) {
                styleClass.add("error");
            }
        } else {
            if (styleClass.contains("error")) {
                styleClass.removeAll(Collections.singleton("error"));
            }
        }
    }

    private boolean inValidate() {
        boolean status = true;
        if ( textField.getText().length() == 0 ) {
            status = false;
        }
        setupErrorView(status, textField);
        return status;
    }

    @Override
    public void printMessage(String name, String msg) {
        synchronized (textArea) {
            String line = "[" + name + "]:" + msg;
            textArea.appendText(line + "\n");
            if ( logger != null ) {
                logger.addMessage(line);
            }
        }
    }

    @Override
    public void printSystemInfo( String name, String msg ) {
        synchronized (textArea) {
            String line = "[" + name + "]:" + msg;
            textArea.appendText(line + "\n");
        }
    }

    private void sendMessage() {
        if ( inValidate() ) {
            synchronized (textArea) {
                if ( usersList.getValue().equals(SEND_TO_ALL) ) {
                    handler.sendMessageToUsers(textField.getText());
                } else {
                    handler.sendMessageToUsers("/w " + usersList.getValue().toString()
                                                         + " " + textField.getText());
                }
            }
            textField.clear();
        }
    }

    public void btnClicked(MouseEvent mouseEvent) {
        sendMessage();
    }

    public void onExitClicked(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
            sendMessage();
        } else {
            setupErrorView(true, textField);
        }
    }

    public void btnAuth(MouseEvent mouseEvent) {
        if ( handler == null || !handler.isConnected()) {
            onConnectClicked(null);
            //handler = new ClientIOHandler(this, this);
        }
        handler.tryAuthenticate(loginTextField.getText(),passwordField.getText());
    }

    private void setTitle(String nick) {
        Platform.runLater(() -> {
            ((Stage) textField.getScene().getWindow()).setTitle("SmartChat " + nick);
        });
    }


    private Stage createRegWindow() {
        Stage stage = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root = fxmlLoader.load();

            stage = new Stage();
            stage.setTitle("Registration");
            stage.setScene(new Scene(root, 300,200));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);

            registrationController = fxmlLoader.getController();
            registrationController.controller = this;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }

    private Stage createNewNicknameWindow() {
        Stage stage = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/newnickname.fxml"));
            Parent root = fxmlLoader.load();

            stage = new Stage();
            stage.setTitle("Switch Nickname");
            stage.setScene(new Scene(root, 300,50));
            stage.setResizable(true);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            newNickNameController = fxmlLoader.getController();
            newNickNameController.controller = this;

            stage.setOnShowing(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    newNickNameController.onShowStuff();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }

    public void onRegClicked(ActionEvent actionEvent) {
        regStage.show();
    }

    public void tryRegistration(String login, String password, String nickname) {
        if ( handler == null || !handler.isConnected()) {
            onConnectClicked(null);
            //handler = new ClientIOHandler(this, this);
        }
        handler.tryRegistration(login, password, nickname);
    }

    public void tryChangeNickName(String newNickname) {
        if ( handler.isConnected()) {
            handler.tryChangeNickName(newNickname);
        }
    }

    public void onConnectClicked(ActionEvent actionEvent) {
        if ( handler == null || !handler.isConnected()) {
            usersList.getItems().clear();
            usersList.getItems().add(SEND_TO_ALL);
            usersList.setValue(SEND_TO_ALL);


            handler = new ClientIOHandler(this, this);
            connect.setText("Disconnect");
            setTitle("");
        } else {
            handler.disconnect();
            connect.setText("Connect");
            setTitle("[Not connected]");
        }
    }

    public void addOnlineUsers( String[] users ) {
        Platform.runLater(() -> {
            usersList.getItems().addAll(users);
        });
    }


    public void removeOfflineUsers( String[] users ) {
        Platform.runLater(() -> {
            usersList.getItems().removeAll(users);
        });

        for (int i = 0; i < users.length; i++) {
            if ( usersList.getValue().equals(users[i])) {
                Platform.runLater(() -> {
                    usersList.setValue(SEND_TO_ALL);
                });
                break;
            }
        }
    }

    public void nickNameChangedTo( String oldNickName, String newNickname ) {
        Platform.runLater(() -> {
            boolean switchToNew = false;
            if ( usersList.getValue().equals(oldNickName )) {
                switchToNew = true;
            }
            usersList.getItems().removeAll(oldNickName);
            usersList.getItems().addAll(newNickname);
            if ( switchToNew ) {
                usersList.setValue(newNickname);
            }
        });
    }
    public void onNewNNClicked(ActionEvent actionEvent) {
        nicknameStage.show();
    }
}
