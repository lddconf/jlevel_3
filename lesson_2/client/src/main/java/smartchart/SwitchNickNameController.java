package smartchart;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Collections;

public class SwitchNickNameController {
    Controller controller;

    @FXML
    public TextField nickNameField;

    private void changeNickname(String nickname) {
        if ( nickname.length() > 0 ) {
            controller.tryChangeNickName(nickname);
        }
    }

    public void onShowStuff() {
        Controller.setupErrorView(true, nickNameField);
    }

    public void onClick(ActionEvent actionEvent) {
        String nickname = nickNameField.getText().trim();
        if ( nickname.length() > 0  ) {
            changeNickname(nickname);
            Controller.setupErrorView(true, nickNameField);
        } else {
            Controller.setupErrorView(false, nickNameField);
        }
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
            String nickname = nickNameField.getText().trim();
            if ( nickname.length() > 0  ) {
                changeNickname(nickname);
            } else {
                Controller.setupErrorView(false, nickNameField);
            }
        } else {
            Controller.setupErrorView(true, nickNameField);
        }
    }


}
