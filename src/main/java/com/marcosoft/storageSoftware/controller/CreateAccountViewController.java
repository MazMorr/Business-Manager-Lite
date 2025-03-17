
package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.io.IOException;

import static javafx.scene.paint.Color.*;

public class CreateAccountViewController {

    @FXML
    private PasswordField passFieldPasswordConfirmed;
    @FXML
    private TextField txtFieldPassword, txtFieldUserName;
    @FXML
    private ProgressIndicator percentageBar;
    @FXML
    private Label txtDebugForm;

    public boolean userNameIsSetted, passwordIsSetted;
    public double percentageName = 0, percentagePassWord = 0;

    @FXML
    private void createAccount() throws IOException {
        Client client = new Client(null, txtFieldUserName.getText(), txtFieldPassword.getText());
        if (ClientRepository.existsByClientNameAndClientPassword(txtFieldUserName.getText(), txtFieldPassword.getText())) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("EStá intentando crear una cuenta que ya existe");
        } else if (percentageBar.getProgress() == 1.0) {

        }
    }

    @FXML
    public void txtFieldTypingUserName() {

        if (txtFieldUserName.getLength() > 16) {
            txtDebugForm.setText("El Nombre de Usuario no puede exceder los 16 carácteres");
            txtDebugForm.setTextFill(RED);
        } else if (txtFieldUserName.getLength() < 4) {
            txtDebugForm.setText("El Nombre de Usuario debe tener al menos 4 carácteres");
            txtDebugForm.setTextFill(YELLOW);
        } else if (!userNameIsSetted && txtFieldUserName.getLength() > 3 && txtFieldUserName.getLength() < 16) {
            userNameIsSetted = true;
            percentageName += 0.5;
            percentageBar.setProgress(percentageName + percentagePassWord);
        } else if (txtFieldUserName.getLength() < 4 && userNameIsSetted) {
            percentageName -= 0.5;
            userNameIsSetted = false;
            percentageBar.setProgress(percentageName + percentagePassWord);
        }
    }

    @FXML
    private void goBack() throws IOException {
        Main.setRoot("accountView");
    }

    @FXML
    public void txtFieldTypingPassword() {
        System.out.println(passFieldPasswordConfirmed.getText());
        if (txtFieldPassword.getLength() > 10 && passFieldPasswordConfirmed.getLength() > 10) {
            txtDebugForm.setText("La contraseña no puede exceder los 16 carácteres");
            txtDebugForm.setTextFill(RED);
        } else if (txtFieldPassword.getLength() < 4 && passFieldPasswordConfirmed.getLength() < 4) {
            txtDebugForm.setText("La contraseña debe tener al menos 4 carácteres");
            txtDebugForm.setTextFill(YELLOW);
        } else if (txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText()) && !passwordIsSetted) {
            if ((txtFieldPassword.getLength() > 3 && txtFieldPassword.getLength() < 11) && (passFieldPasswordConfirmed.getLength() > 3 && passFieldPasswordConfirmed.getLength() < 17)) {
                passwordIsSetted = true;
                percentagePassWord += 0.5;
                percentageBar.setProgress(percentageName + percentagePassWord);
                txtDebugForm.setText("Las contraseñas coinciden");
                txtDebugForm.setTextFill(GREEN);
            }
        } else if (!txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText())) {
            if (passwordIsSetted) {
                passwordIsSetted = false;
                percentagePassWord -= 0.5;
                percentageBar.setProgress(percentageName + percentagePassWord);
                txtDebugForm.setText("Las contraseñas no coinciden");
                txtDebugForm.setTextFill(RED);
            }
        }
    }

    @FXML
    public void initialize() {
        // TODO
    }
}
