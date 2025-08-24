package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import static javafx.scene.paint.Color.*;

/**
 * Controller for the create client view.
 * Handles logic for creating a new client account and validating input fields.
 */
@Lazy
@Controller
public class CreateClientViewController {

    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;


    /**
     * Constructor for dependency injection.
     * @param clientService the client service
     */
    @Lazy
    public CreateClientViewController(SceneSwitcher sceneSwitcher, ClientServiceImpl clientService) {
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
    }

    // FXML UI components
    @FXML
    private CheckBox cbShowPassword;
    @FXML
    private PasswordField passFieldPasswordConfirmed, passFieldPassword;
    @FXML
    private TextField txtFieldUserName, txtFieldCompany, textFieldPassword;
    @FXML
    private ProgressIndicator percentageBar;
    @FXML
    private Label txtDebugForm;

    // State variables for validation
    private boolean userNameIsSet = false, passwordIsSet = false, confirmedPasswordIsSet = false, companyIsSet = false;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets initial progress and clears debug label.
     */
    @FXML
    public void initialize() {
        textFieldPassword.setVisible(false);
        textFieldPassword.setManaged(false);
        percentageBar.setProgress(0);
    }

    /**
     * Handles the creation of a new client account when the user clicks the create button.
     * Validates input and shows alerts in Spanish if validation fails.
     */
    @FXML
    private void createAccount(ActionEvent actionEvent) {
        if (clientService.existsByClientName(txtFieldUserName.getText())) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Está intentando crear una cuenta que ya existe.");
            return;
        }

        if (!userNameIsSet || !passwordIsSet || !confirmedPasswordIsSet) {
            txtDebugForm.setTextFill(RED);
            txtDebugForm.setText("Por favor, complete correctamente todos los campos obligatorios.");
            return;
        }

        // Obtener la contraseña del campo visible actual
        String password = cbShowPassword.isSelected() ? textFieldPassword.getText() : passFieldPassword.getText();

        Client client = new Client(
                txtFieldUserName.getText(),
                password,
                txtFieldCompany.getText(),
                false,
                null
        );
        clientService.save(client);

        Alert alert = getAlert(txtFieldCompany, txtFieldUserName);
        alert.showAndWait();
        try {
            goBack(actionEvent);
        } catch (SceneSwitcher.ViewLoadException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and returns an alert with account creation information.
     * The alert message is shown in Spanish.
     */
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

    /**
     * Validates the username field as the user types.
     * Updates the debug label and progress bar. Messages are shown in Spanish.
     */
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

    /**
     * Sets the state and message for username validation.
     */
    private void setUserNameState(boolean isValid, String message, javafx.scene.paint.Color color) {
        userNameIsSet = isValid;
        txtDebugForm.setTextFill(color);
        txtDebugForm.setText(message);
    }

    /**
     * Validates the password and confirmation fields as the user types.
     * Updates the debug label and progress bar. Messages are shown in Spanish.
     */
    @FXML
    public void txtFieldTypingPassword() {
        String password = cbShowPassword.isSelected() ? textFieldPassword.getText() : passFieldPassword.getText();
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

    /**
     * Sets the state and message for password validation.
     */
    private void setPasswordState(boolean passValid, boolean confirmValid, String message, javafx.scene.paint.Color color) {
        passwordIsSet = passValid;
        confirmedPasswordIsSet = confirmValid;
        txtDebugForm.setTextFill(color);
        txtDebugForm.setText(message);
    }

    /**
     * Validates the company field as the user types.
     * Updates the progress bar.
     */
    @FXML
    public void txtFieldTypingCompany() {
        // You can add company validation here if needed
        companyIsSet = txtFieldCompany.getText() != null && !txtFieldCompany.getText().isEmpty();
        updateProgress();
    }

    /**
     * Updates the progress bar based on the validation state of all fields.
     */
    private void updateProgress() {
        double progress = 0;
        if (userNameIsSet) progress += 0.33;
        if (passwordIsSet) progress += 0.33;
        if (confirmedPasswordIsSet) progress += 0.34;
        percentageBar.setProgress(progress);
    }

    /**
     * Navigates back to the client view.
     */
    @FXML
    private void goBack(ActionEvent actionEvent) throws SceneSwitcher.ViewLoadException {
        sceneSwitcher.setRootWithEvent(actionEvent, "/views/clientView.fxml");
    }

    /**
     * Alterna entre mostrar/ocultar la contraseña.
     */
    @FXML
    public void showPassword() {
        if (cbShowPassword.isSelected()) {
            // Mostrar contraseña en texto plano
            textFieldPassword.setText(passFieldPassword.getText());
            textFieldPassword.setVisible(true);
            textFieldPassword.setManaged(true);
            passFieldPassword.setVisible(false);
            passFieldPassword.setManaged(false);

            // Trasladar el foco al campo de texto
            Platform.runLater(() -> textFieldPassword.requestFocus());
        } else {
            // Ocultar contraseña
            passFieldPassword.setText(textFieldPassword.getText());
            passFieldPassword.setVisible(true);
            passFieldPassword.setManaged(true);
            textFieldPassword.setVisible(false);
            textFieldPassword.setManaged(false);

            // Trasladar el foco al campo de contraseña
            Platform.runLater(() -> passFieldPassword.requestFocus());
        }
    }
}
