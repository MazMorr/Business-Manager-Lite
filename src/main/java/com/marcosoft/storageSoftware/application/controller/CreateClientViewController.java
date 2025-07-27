package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.IOException;

import static javafx.scene.paint.Color.*;

@Lazy
@Controller
public class CreateClientViewController {

    private final ClientServiceImpl clientService;

    @Lazy
    public CreateClientViewController(ClientServiceImpl clientService){
        this.clientService = clientService;
    }

    @FXML
    private PasswordField passFieldPasswordConfirmed;
    @FXML
    private TextField txtFieldPassword, txtFieldUserName, txtFieldCompany;
    @FXML
    private ProgressIndicator percentageBar;
    @FXML
    private Label txtDebugForm;

    private boolean userNameIsSet = false, passwordIsSet = false, confirmedPasswordIsSet = false, companyIsSet = false;


    @FXML
    public void initialize() {
        percentageBar.setProgress(0);
        txtDebugForm.setText("");
    }

    @FXML
    private void createAccount() throws IOException {
        if (!userNameIsSet || !passwordIsSet || !confirmedPasswordIsSet) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Por favor, complete correctamente todos los campos obligatorios.");
            return;
        }

        if (clientService.existsByClientName(txtFieldUserName.getText())) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Está intentando crear una cuenta que ya existe.");
            return;
        }

        Client client = new Client(
                txtFieldUserName.getText(),
                txtFieldPassword.getText(),
                txtFieldCompany.getText(),
                false
        );
        clientService.save(client);

        Alert alert = getAlert(txtFieldCompany, txtFieldUserName);
        alert.showAndWait();
        goBack();
    }

    private static Alert getAlert(TextField txtFieldCompany, TextField txtFieldUserName) {
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
        return alert;
    }

    @FXML
    public void txtFieldTypingUserName() {
        String userName = txtFieldUserName.getText();
        if (userName.length() > 16) {
            setUserNameState(false, "El nombre de usuario no puede superar los 16 caracteres.", RED);
        } else if (userName.length() < 4) {
            setUserNameState(false, "El nombre de usuario debe tener al menos 4 caracteres.", ORANGERED);
        } else if (clientService.existsByClientName(userName)) {
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
        } else if (!confirm.isEmpty() && !password.equals(confirm)) {
            setPasswordState(true, false, "Las contraseñas no coinciden.", RED);
        } else if (password.equals(confirm)) {
            setPasswordState(true, true, "Las contraseñas coinciden. Puede continuar.", GREEN);
        } else {
            setPasswordState(true, false, "Por favor, confirme su contraseña.", GREEN);
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

}
