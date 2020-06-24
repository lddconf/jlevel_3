package smartchart;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    Controller controller;

    @FXML
    public TextField loginField;

    @FXML
    public TextField nickField;

    @FXML
    public PasswordField passwordField;

    public void onClick(ActionEvent actionEvent) {
        controller.tryRegistration(loginField.getText().trim(), passwordField.getText().trim(), nickField.getText().trim());
    }
}
