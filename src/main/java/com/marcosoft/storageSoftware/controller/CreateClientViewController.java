package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    ClientServiceImpl clientServiceImpl;

    public boolean userNameIsSet, passwordIsSet, confirmedPasswordIsSet, companyIsSet;
    public double percentageName = 0, percentagePassword = 0, percentageConfirmedPassword = 0, percentageCompany = 0;

    @FXML
    private void createAccount() throws IOException {

        if (clientServiceImpl.existsByClientName(txtFieldUserName.getText())) {

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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Alerta");
            alert.setHeaderText("Información de creación de la cuenta");
            if (txtFieldCompany.getText() != null) {
                alert.setContentText("Usted ha creado la cuenta con nombre " + txtFieldUserName.getText() +
                        " perteneciente la compañía " + txtFieldCompany.getText() +
                        " \n¡Por favor no olvidar su contraseña!");
            } else {
                alert.setContentText("Usted ha creado la cuenta con nombre " + txtFieldUserName.getText()
                        + " \n¡Por favor no olvidar su contraseña!");
            }
            alert.showAndWait();

            clientServiceImpl.save(client);

            goBack();

        }
    }

    @FXML
    public void txtFieldTypingUserName() {
        if (txtFieldUserName.getLength() > 16) {
            txtDebugForm.setText("El nombre de usuario no puede superar los 16 caracteres. Por favor, ingrese un nombre más corto.");
            txtDebugForm.setTextFill(RED);
            if (userNameIsSet) {
                percentageName -= 0.5;
                userNameIsSet = false;
            }

        } else if (txtFieldUserName.getLength() < 4) {
            txtDebugForm.setText("El nombre de usuario debe tener al menos 4 caracteres. Por favor, ingrese un nombre más largo.");
            txtDebugForm.setTextFill(ORANGERED);
            if (userNameIsSet) {
                percentageName -= 0.5;
                userNameIsSet = false;
            }

        } else if (clientServiceImpl.existsByClientName(txtFieldUserName.getText())) {
            txtDebugForm.setText("El nombre de usuario ya está en uso. Por favor, elija un nombre diferente.");
            txtDebugForm.setTextFill(RED);
            if (userNameIsSet) {
                percentageName -= 0.5;
                userNameIsSet = false;
            }

        } else if (!userNameIsSet && txtFieldUserName.getLength() > 3 && txtFieldUserName.getLength() < 16) {
            txtDebugForm.setTextFill(GREEN);
            txtDebugForm.setText("El nombre de usuario está disponible.");
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
            if (confirmedPasswordIsSet) {
                txtDebugForm.setText("La confirmación de la contraseña no puede superar los 16 caracteres. Por favor, revise su entrada.");
                confirmedPasswordIsSet = false;
                percentageConfirmedPassword -= 0.25;
            } else if (passwordIsSet) {
                txtDebugForm.setText("La contraseña no puede superar los 16 caracteres. Por favor, revise su entrada.");
                passwordIsSet = false;
                percentagePassword -= 0.25;
            }

        } else if (txtFieldPassword.getLength() < 4 || passFieldPasswordConfirmed.getLength() < 4) {
            txtDebugForm.setTextFill(ORANGERED);
            txtDebugForm.setText("La contraseña debe tener al menos 4 caracteres. Por favor, ingrese una contraseña más larga.");
            if (confirmedPasswordIsSet) {
                txtDebugForm.setText("La confirmación de la contraseña debe tener al menos 4 caracteres. Por favor, revise su entrada.");
                confirmedPasswordIsSet = false;
                percentageConfirmedPassword -= 0.25;
            } else if (passwordIsSet) {
                txtDebugForm.setText("La contraseña debe tener al menos 4 caracteres. Por favor, revise su entrada.");
                passwordIsSet = false;
                percentagePassword -= 0.25;
            }

        } else if (txtFieldPassword.getLength() >= 4 && passFieldPasswordConfirmed.getLength() == 0) {
            txtDebugForm.setTextFill(ORANGERED);
            txtDebugForm.setText("Por favor, confirme su contraseña ingresándola nuevamente en el campo de confirmación.");

        } else if (txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText()) && !passwordIsSet) {
            if ((txtFieldPassword.getLength() > 3 && txtFieldPassword.getLength() < 16) && (passFieldPasswordConfirmed.getLength() > 3 && passFieldPasswordConfirmed.getLength() < 16)) {
                passwordIsSet = true;
                confirmedPasswordIsSet = true;
                percentagePassword += 0.25;
                percentageConfirmedPassword += 0.25;

                txtDebugForm.setText("Las contraseñas coinciden. Puede continuar.");
                txtDebugForm.setTextFill(GREEN);
            }

        } else if (!txtFieldPassword.getText().equals(passFieldPasswordConfirmed.getText())) {
            if (passwordIsSet) {
                passwordIsSet = false;
                percentagePassword -= 0.5;
                txtDebugForm.setText("Las contraseñas no coinciden. Por favor, asegúrese de que ambas contraseñas sean iguales.");
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
