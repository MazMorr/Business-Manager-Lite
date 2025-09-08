package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Objects;

import static javafx.scene.paint.Color.RED;

/**
 * Controller for the client login view.
 * Handles authentication, navigation, and session management for clients.
 */
@Controller
public class LoginViewController {

    // Dependencies injected via constructor
    private final SpringFXMLLoader springFXMLLoader;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;
    private final LicenseValidator licenseValidator;
    private final DisplayAlerts displayAlerts;
    private final SceneSwitcher sceneSwitcher;

    public LoginViewController(
            UserLogged userLogged, LicenseValidator licenseValidator, ClientServiceImpl clientService,
            SpringFXMLLoader springFXMLLoader, DisplayAlerts displayAlerts, SceneSwitcher sceneSwitcher
    ) {
        this.userLogged = userLogged;
        this.displayAlerts = displayAlerts;
        this.sceneSwitcher = sceneSwitcher;
        this.clientService = clientService;
        this.licenseValidator = licenseValidator;
        this.springFXMLLoader = springFXMLLoader;
    }

    // FXML UI components
    @FXML
    private TextField txtFieldName;
    @FXML
    private PasswordField txtFieldPassword;
    @FXML
    private Label txtDebugForm;

    /**
     * Handles the login process when the user clicks the login button.
     * Validates input, authenticates the user, and navigates to the support view if successful.
     */
    @FXML
    private void enterApplication(ActionEvent event) {
        String username = txtFieldName.getText();
        String password = txtFieldPassword.getText();

        // Validate input fields
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Por favor, ingrese usuario y contraseña.");
            return;
        }

        try {
            // Authenticate user credentials (ahora verifica directamente)
            Client client = clientService.authenticate(username, password);

            if (client == null) {
                showError("Usuario o contraseña incorrecta.");
                return;
            }

            if (!licenseValidator.validateLicense(username)) {
                showError("Licencia no válida o expirada.");
                return;
            }

            // Mark client as active in the database
            clientService.updateIsClientActiveByClientName(true, username);
            userLogged.setName(username);

            // Load the support view
            Parent root = springFXMLLoader.load("/views/supportView.fxml");
            SupportViewController primaryController = springFXMLLoader.getController(SupportViewController.class);
            primaryController.setAccountController(this);

            // Prepare new stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/lc_logo.png")).toString()));
            stage.setTitle("Business Manager");
            stage.centerOnScreen();
            stage.setMinWidth(1140);
            stage.setMinHeight(690);

            // Handle window close event
            stage.setOnCloseRequest(e -> {
                if (showExitAlert()) {
                    clientService.updateIsClientActiveByClientName(false, username);
                    stage.close();
                } else {
                    e.consume();
                }
            });

            stage.show();
            currentStage.close();

        } catch (IOException e) {
            showError("Error al cargar la interfaz: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Displays an error message in the debug label.
     */
    private void showError(String message) {
        txtDebugForm.setText(message);
        txtDebugForm.setTextFill(RED);
    }

    /**
     * Shows a confirmation dialog when the user attempts to exit the application.
     *
     * @return true if the user confirms exit, false otherwise.
     */
    private boolean showExitAlert() {
        return displayAlerts.showConfirmationAlert("Asegúrese de tener todo en orden antes de cerrar la aplicación.");
    }

    /**
     * Navigates to the create client view.
     * Clears input fields before switching view.
     */
    @FXML
    private void switchToCreateClient(ActionEvent actionEvent) throws SceneSwitcher.ViewLoadException {
        clearFields();
        sceneSwitcher.setRootWithEvent(actionEvent, "/views/createClientView.fxml");
    }

    /**
     * Clears all input fields and debug messages.
     */
    private void clearFields() {
        txtFieldName.clear();
        txtFieldPassword.clear();
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * Closes any previous active session and clears fields.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Close any previous active client session
            if (clientService.existsByIsClientActive(true)) {
                clientService.updateIsClientActiveByClientName(false,
                        clientService.getByIsClientActive(true).getClientName());
            }
            clearFields();
        });
    }
}
