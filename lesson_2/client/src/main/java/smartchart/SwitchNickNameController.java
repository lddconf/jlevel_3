package smartchart;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SwitchNickNameController {
    Controller controller;

    @FXML
    public TextField nickNameField;

    public void onClick(ActionEvent actionEvent) {
        if ( nickNameField.getText().trim().length() > 0 ) {
            controller.tryChangeNickName(nickNameField.getText().trim());
        }
    }

}
