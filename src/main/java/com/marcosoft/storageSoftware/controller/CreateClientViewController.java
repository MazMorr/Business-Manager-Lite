package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.Client;
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

    private boolean userNameIsSet = false, passwordIsSet = false, confirmedPasswordIsSet = false, companyIsSet = false;

    @FXML
    private void createAccount() throws IOException {
        if (!userNameIsSet || !passwordIsSet || !confirmedPasswordIsSet) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Por favor, complete correctamente todos los campos obligatorios.");
            return;
        }

        if (clientServiceImpl.existsByClientName(txtFieldUserName.getText())) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Está intentando crear una cuenta que ya existe.");
            return;
        }

        Client client = new Client(
                null,
                txtFieldUserName.getText(),
                txtFieldPassword.getText(),
                txtFieldCompany.getText(),
                false
        );
        clientServiceImpl.save(client);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cuenta creada");
        alert.setHeaderText("Información de creación de la cuenta");
        if (txtFieldCompany.getText() != null && !txtFieldCompany.getText().isEmpty()) {
            alert.setContentText("Usted ha creado la cuenta con nombre " + txtFieldUserName.getText() +
                    " perteneciente a la compañía " + txtFieldCompany.getText() +
                    ".\n¡Por favor no olvide su contraseña!");
        } else {
            alert.setContentText("Usted ha creado la cuenta con nombre " + txtFieldUserName.getText()
                    + ".\n¡Por favor no olvide su contraseña!");
        }
        alert.showAndWait();
        goBack();
    }

    @FXML
    public void txtFieldTypingUserName() {
        String userName = txtFieldUserName.getText();
        if (userName.length() > 16) {
            setUserNameState(false, "El nombre de usuario no puede superar los 16 caracteres.", RED);
        } else if (userName.length() < 4) {
            setUserNameState(false, "El nombre de usuario debe tener al menos 4 caracteres.", ORANGERED);
        } else if (clientServiceImpl.existsByClientName(userName)) {
            setUserNameState(false, "El nombre de usuario ya está en uso. Por favor, elija otro.", RED);
        } else {
            setUserNameState(true, "El nombre de usuario está disponible.", GREEN);
        }
        updateProgress();
    }

    private void setUserNameState(boolean isValid, String message, javafx.scene.paint.Color color) {
        userNameIsSet = isValid;
        txtDebugForm.setTextFill(color);
        txtDebugForm.setText(message);
    }

    @FXML
    public void txtFieldTypingPassword() {
        String password = txtFieldPassword.getText();
        String confirm = passFieldPasswordConfirmed.getText();

        if (password.length() > 16) {
            setPasswordState(false, false, "La contraseña no puede superar los 16 caracteres.", RED);
        } else if (password.length() < 4) {
            setPasswordState(false, false, "La contraseña debe tener al menos 4 caracteres.", ORANGERED);
        } else if (confirm.length() > 0 && !password.equals(confirm)) {
            setPasswordState(true, false, "Las contraseñas no coinciden.", RED);
        } else if (password.length() >= 4 && password.length() <= 16 && password.equals(confirm) && confirm.length() >= 4) {
            setPasswordState(true, true, "Las contraseñas coinciden. Puede continuar.", GREEN);
        } else if (password.length() >= 4 && confirm.isEmpty()) {
            setPasswordState(true, false, "Por favor, confirme su contraseña.", GREEN);
        } else {
            setPasswordState(false, false, "", BLACK);
        }
        updateProgress();
    }

    private void setPasswordState(boolean passValid, boolean confirmValid, String message, javafx.scene.paint.Color color) {
        passwordIsSet = passValid;
        confirmedPasswordIsSet = confirmValid;
        txtDebugForm.setTextFill(color);
        txtDebugForm.setText(message);
    }

    @FXML
    public void txtFieldTypingCompany() {
        // Puedes agregar validación de empresa aquí si lo deseas
        companyIsSet = txtFieldCompany.getText() != null && !txtFieldCompany.getText().isEmpty();
        updateProgress();
    }

    private void updateProgress() {
        double progress = 0;
        if (userNameIsSet) progress += 0.33;
        if (passwordIsSet) progress += 0.33;
        if (confirmedPasswordIsSet) progress += 0.34;
        percentageBar.setProgress(progress);
    }

    @FXML
    private void goBack() throws IOException {
        Main.setRoot("clientView");
    }

    @FXML
    public void initialize() {
        percentageBar.setProgress(0);
        txtDebugForm.setText("");
    }
}
