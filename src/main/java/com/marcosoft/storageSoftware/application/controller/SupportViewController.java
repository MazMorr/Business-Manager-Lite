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

@Lazy
@Controller
public class SupportViewController {
    private ClientViewController accountController;

    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final ClientServiceImpl clientService;

    @Lazy
    public SupportViewController(UserLogged userLogged, SceneSwitcher sceneSwitcher, ClientServiceImpl clientService) {
        this.userLogged = userLogged;
        this.sceneSwitcher = sceneSwitcher;
        this.clientService = clientService;
    }

    @FXML
    private Label txtWelcome, versionLabel, txtClientName, txtWelcomeTitle;

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

    public void setAccountController(ClientViewController clientViewController) {
        this.accountController = clientViewController;
        System.out.println("Controlador de cuenta configurado: " + clientViewController);
    }


    private void initWelcomeLabels() {
        versionLabel.setText("0.9.3");
        txtWelcomeTitle.setText("Bienvenido, " + userLogged.getName());
        txtWelcome.setText("Este software fue desarrollado con un único objetivo en mente, " +
                "´Obtener un control eficiente de los recursos dentro de su negocio´. " +
                "Este software presenta una licencia de uso y su comercialización fuera de fuentes oficiales" +
                " puede y será penalizado de forma legal, multa o cancelación de la licencia de forma permanente. " +
                "Cualquier duda, sugerencia o reporte de errores será atentida a través de las fuentes proporcionadas" +
                " en la sección ´Soporte´ que se encuentra a la derecha de este mensaje.");
    }

    @FXML
    private void switchToRegistry(ActionEvent event) {
        sceneSwitcher.switchView(event, "/registryView.fxml");
    }

    @FXML
    private void switchToInvestment(ActionEvent event) {
        sceneSwitcher.switchView(event, "/investmentView.fxml");
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) {
        sceneSwitcher.switchView(event, "/configurationView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent event) {
        sceneSwitcher.switchView(event, "/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent event) {
        sceneSwitcher.switchView(event, "/balanceView.fxml");
    }

    @FXML
    public void switchToInventory(ActionEvent event) {
        sceneSwitcher.switchView(event, "/sellView.fxml");
    }

    @FXML
    private void licenseInformation(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Asistente de Ayuda");
        alert.setHeaderText("Información de la licencia");
        alert.setContentText(
                "Pasado el tiempo disponible para renovar su licencia, el programa se bloqueará " +
                        "instantáneamente y no podrá ser usado. Es posible que pierda los datos de la base de datos. " +
                        "Por favor, llame al +53 5550 5961 antes de que eso ocurra para revocar su licencia y continuar " +
                        "usando el software sin interrupciones."
        );
        alert.showAndWait();
    }
}
