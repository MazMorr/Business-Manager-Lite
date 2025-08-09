package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static javafx.scene.paint.Color.RED;

/**
 * Controller for the client login view.
 * Handles authentication, navigation, and session management for clients.
 */
@Controller
public class ClientViewController {

    // Dependencies injected via constructor
    private final SpringFXMLLoader springFXMLLoader;
    private final ClientServiceImpl clientServiceImpl;
    private final UserLogged userLogged;
    private final LicenseValidator licenseValidator;

    /**
     * Constructor for dependency injection.
     */
    public ClientViewController(UserLogged userLogged, LicenseValidator licenseValidator, ClientServiceImpl clientService, SpringFXMLLoader springFXMLLoader) {
        this.userLogged = userLogged;
        this.clientServiceImpl = clientService;
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

        // Authenticate user credentials
        if (clientServiceImpl.existsByClientNameAndClientPassword(username, password)) {
            try {
                if (licenseValidator.validateLicense(username)) {
                    // Mark client as active in the database
                    clientServiceImpl.updateIsClientActiveByClientName(true, username);
                    userLogged.setName(username);

                    // Load the support view using SpringFXMLLoader
                    Parent root = springFXMLLoader.load("/supportView.fxml");

                    // Get the controller for the support view and pass this controller as reference
                    SupportViewController primaryController = springFXMLLoader.getController(SupportViewController.class);
                    primaryController.setAccountController(this);

                    // Get the current window and prepare the new stage for support view
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Stage stage = new Stage();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/RTS_logo.png")).toString()));
                    stage.setTitle("Almacenamiento");
                    stage.centerOnScreen();
                    stage.setMinWidth(1100);
                    stage.setMinHeight(650);

                    // Handle window close event to update client active status
                    stage.setOnCloseRequest(e -> {
                        if (showExitAlert()) {
                            clientServiceImpl.updateIsClientActiveByClientName(false, username);
                            stage.close();
                        } else {
                            e.consume();
                        }
                    });

                    stage.show();
                    currentStage.close();
                }

            } catch (IOException e) {
                showError("Ha ocurrido un error al cargar la aplicación: " + e.getMessage());
            }
        } else {
            showError("Usuario o contraseña incorrecta.");
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
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("¿Seguro que quiere salir?");
        alert.setContentText("Asegúrese de tener todo en orden antes de cerrar la aplicación, por favor.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Navigates to the create client view.
     * Clears input fields before switching view.
     */
    @FXML
    private void switchToCreateClient() {
        clearFields();
        Main.setRoot("createClientView");
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
            if (clientServiceImpl.existsByIsClientActive(true)) {
                clientServiceImpl.updateIsClientActiveByClientName(false,
                        clientServiceImpl.getByIsClientActive(true).getClientName());
            }
            clearFields();
        });

    }
}
