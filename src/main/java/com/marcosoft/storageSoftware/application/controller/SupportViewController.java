package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/**
 * Controller for the support view.
 * Handles welcome messages, navigation, and license information display.
 */
@Lazy
@Controller
public class SupportViewController {
    // Reference to the account controller for session management
    private ClientViewController accountController;

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final ClientServiceImpl clientService;

    /**
     * Constructor for dependency injection.
     */
    @Lazy
    public SupportViewController(UserLogged userLogged, SceneSwitcher sceneSwitcher, ClientServiceImpl clientService) {
        this.userLogged = userLogged;
        this.sceneSwitcher = sceneSwitcher;
        this.clientService = clientService;
    }

    // FXML UI components
    @FXML
    private Label txtWelcome, versionLabel, txtClientName, txtWelcomeTitle;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets welcome labels and displays the active client name.
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            initWelcomeLabels();

            try {
                String clientName = clientService.getByIsClientActive(true).getClientName();
                txtClientName.setText(clientName != null ? clientName : "Usuario");
            } catch (Exception e) {
                txtClientName.setText("Usuario");
            }
        });
    }

    /**
     * Sets the account controller reference for session management.
     */
    public void setAccountController(ClientViewController clientViewController) {
        this.accountController = clientViewController;
        System.out.println("Controlador de cuenta configurado: " + clientViewController);
    }

    /**
     * Initializes welcome labels with version and welcome message.
     * The welcome message is shown in Spanish.
     */
    private void initWelcomeLabels() {
        versionLabel.setText("0.9.4");
        txtWelcomeTitle.setText("Bienvenido, " + userLogged.getName());
        txtWelcome.setText(
            "Este sistema ha sido diseñado para brindarle un control eficiente y seguro sobre los recursos de su negocio. " +
            "Aquí podrá gestionar inventarios, inversiones, ventas y mucho más de manera sencilla y centralizada.\n\n" +
            "Recuerde que el uso de este software está protegido por una licencia. La distribución o comercialización fuera de los canales oficiales " +
            "puede resultar en sanciones legales, multas o la cancelación permanente de la licencia.\n\n" +
            "Para cualquier duda, sugerencia o reporte de errores, utilice la sección 'Soporte' ubicada a la derecha de este mensaje. " +
            "Nuestro equipo estará siempre disponible para ayudarle y mejorar su experiencia.\n\n" +
            "¡Gracias por confiar en nosotros!"
        );
    }

    /**
     * Navigates to the registry view.
     */
    @FXML
    private void switchToRegistry(ActionEvent event) {
        sceneSwitcher.switchView(event, "/registryView.fxml");
    }

    /**
     * Navigates to the investment view.
     */
    @FXML
    private void switchToInvestment(ActionEvent event) {
        sceneSwitcher.switchView(event, "/investmentView.fxml");
    }

    /**
     * Navigates to the configuration view.
     */
    @FXML
    private void switchToConfiguration(ActionEvent event) {
        sceneSwitcher.switchView(event, "/configurationView.fxml");
    }

    /**
     * Navigates to the warehouse view.
     */
    @FXML
    public void switchToWarehouse(ActionEvent event) {
        sceneSwitcher.switchView(event, "/warehouseView.fxml");
    }

    /**
     * Navigates to the balance view.
     */
    @FXML
    public void switchToBalance(ActionEvent event) {
        sceneSwitcher.switchView(event, "/balanceView.fxml");
    }

    /**
     * Navigates to the inventory (sell) view.
     */
    @FXML
    public void switchToInventory(ActionEvent event) {
        sceneSwitcher.switchView(event, "/sellView.fxml");
    }

    /**
     * Displays license information in an alert dialog.
     * The message is shown in Spanish.
     */
    @FXML
    private void licenseInformation(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Asistente de Ayuda");
        alert.setHeaderText("Información de la licencia");
        alert.setContentText(
                "Pasado el tiempo disponible para renovar su licencia, el programa se bloqueará " +
                        "instantáneamente y no podrá ser usado. Es posible que pierda los datos de la base de datos. " +
                        "Por favor, llame al +53 5550 5961 antes de que eso ocurra para renovar su licencia y continuar " +
                        "usando el software sin interrupciones."
        );
        alert.showAndWait();
    }
}
