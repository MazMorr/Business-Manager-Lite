
package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

import static javafx.scene.paint.Color.*;

@Controller
public class CreateClientViewController {

    @FXML
    private PasswordField passFieldPasswordConfirmed;
    @FXML
    private TextField txtFieldPassword, txtFieldUserName, txtFieldCompany;
    @FXML
    private ProgressIndicator percentageBar;
    @FXML
    private Label txtDebugForm;

    @Autowired
    ClientRepository clientRepository;

    public boolean userNameIsSet, passwordIsSet, confirmedPasswordIsSet, companyIsSet;
    public double percentageName = 0, percentagePassword = 0, percentageConfirmedPassword =0, percentageCompany = 0;

    @FXML
    private void createAccount() throws IOException {

        if (clientRepository.existsByClientName(txtFieldUserName.getText())) {

            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("EStá intentando crear una cuenta que ya existe");

        } else if (percentageBar.getProgress() == 1.0) {

            Client client = new Client(
                    null,
                    txtFieldUserName.getText(),
                    txtFieldPassword.getText(),
                    txtFieldCompany.getText(),
                    false
            );

            clientRepository.save(client);


        }
    }

    @FXML
    public void txtFieldTypingUserName() {

        if (txtFieldUserName.getLength() > 16) {
            txtDebugForm.setText("El Nombre de Usuario no puede exceder los 16 carácteres");
            txtDebugForm.setTextFill(RED);
            if (userNameIsSet) {
                percentageName -= 0.5;
                userNameIsSet = false;
            }

        } else if (txtFieldUserName.getLength() < 4) {
            txtDebugForm.setText("El Nombre de Usuario debe tener al menos 4 carácteres");
            txtDebugForm.setTextFill(ORANGERED);
            if (userNameIsSet) {
                percentageName -= 0.5;
                userNameIsSet = false;
            }

        } else if (!userNameIsSet && txtFieldUserName.getLength() > 3 && txtFieldUserName.getLength() < 16) {
            txtDebugForm.setTextFill(GREEN);
            txtDebugForm.setText("El nombre de usuario es permitido");
            userNameIsSet = true;
            percentageName += 0.5;

        }
        percentageBar.setProgress(percentageName + percentagePassword);
    }

    @FXML
    private void goBack() throws IOException {
        Main.setRoot("clientView");
    }

    @FXML
    public void txtFieldTypingPassword() {
        if (txtFieldPassword.getLength() > 16 || passFieldPasswordConfirmed.getLength() > 16) {
            txtDebugForm.setTextFill(RED);
            if(confirmedPasswordIsSet){
                txtDebugForm.setText("La contraseña confirmada debe exceder los 16 carácteres");
                confirmedPasswordIsSet=false;
                percentageConfirmedPassword-=0.25;
            } else if(passwordIsSet){
                txtDebugForm.setText("La contraseña debe exceder los 16 carácteres");
                passwordIsSet=false;
                percentagePassword-=0.25;
            }

        } else if (txtFieldPassword.getLength() < 4 || passFieldPasswordConfirmed.getLength() < 4) {
            txtDebugForm.setTextFill(ORANGERED);
            txtDebugForm.setText("La contraseña debe tener al menos 4 caracteres");
            if(confirmedPasswordIsSet){
                txtDebugForm.setText("La contraseña confirmada debe tener al menos 4 carácteres");
                confirmedPasswordIsSet=false;
                percentageConfirmedPassword-=0.25;
            } else if(passwordIsSet){
                txtDebugForm.setText("La contraseña debe tener al menos 4 caracteres");
                passwordIsSet=false;
                percentagePassword-=0.25;
            }

        } else if (txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText()) && !passwordIsSet) {
            if ((txtFieldPassword.getLength() > 3 && txtFieldPassword.getLength() < 16) && (passFieldPasswordConfirmed.getLength() > 3 && passFieldPasswordConfirmed.getLength() < 16)) {
                passwordIsSet = true;
                confirmedPasswordIsSet = true;
                percentagePassword += 0.25;
                percentageConfirmedPassword += 0.25;

                txtDebugForm.setText("Las contraseñas coinciden");
                txtDebugForm.setTextFill(GREEN);
            }

        } else if (!txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText())) {
            if (passwordIsSet) {
                passwordIsSet = false;
                percentagePassword -= 0.5;
                txtDebugForm.setText("Las contraseñas no coinciden");
                txtDebugForm.setTextFill(RED);
            }
        }
        percentageBar.setProgress(percentageName + percentagePassword + percentageConfirmedPassword);

    }

    @FXML
    public void txtFieldTypingCompany() {

    }

    @FXML
    public void initialize() {
        // TODO
    }
}
