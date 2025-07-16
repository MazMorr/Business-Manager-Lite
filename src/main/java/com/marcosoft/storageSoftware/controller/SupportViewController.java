package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.UserLogged;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SupportViewController {
    private final WindowShowing windowShowing = new WindowShowing();
    private ClientViewController accountController;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;
    @Autowired
    private UserLogged userLogged;
    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    private ClientServiceImpl clientServiceImpl;

    @FXML
    private Label txtWelcome, versionLabel, txtClientName, txtWelcomeTitle;

    public void setAccountController(ClientViewController clientViewController) {
        this.accountController = clientViewController;
        System.out.println("Controlador de cuenta configurado: " + clientViewController);
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            initWelcomeLabels();
            try {
                String clientName = clientServiceImpl.getByIsClientActive(true).getClientName();
                txtClientName.setText(clientName != null ? clientName : "Usuario");
            } catch (Exception e) {
                txtClientName.setText("Usuario");
            }
        });
    }

    private void initWelcomeLabels() {
        txtWelcomeTitle.setText("Bienvenido, "+ userLogged.getName());
        txtWelcome.setText("Este software fue desarrollado con un único objetivo en mente, " +
                "´Obtener un control eficiente de los recursos dentro de su negocio´. " +
                "Este software presenta una licencia de uso y su comercialización fuera de fuentes oficiales" +
                " puede y será penalizado de forma legal, multa o cancelación de la licencia de forma permanente. " +
                "Cualquier duda, sugerencia o reporte de errores será atentida a través de las fuentes proporcionadas" +
                " en la sección ´Soporte´ que se encuentra a la derecha de este mensaje.");
    }

    @FXML
    private void switchToRegistry(ActionEvent event) {
        switchView(event, "/registryView.fxml");
    }

    @FXML
    private void switchToStock(ActionEvent event) {
        switchView(event, "/investmentView.fxml");
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) {
        switchView(event, "/configurationView.fxml");
    }

    @FXML
    public void switchToWallet(ActionEvent event) {
        switchView(event, "/walletView.fxml");
    }

    private void switchView(ActionEvent event, String fxml) {
        sceneSwitcher.setRootWithEvent(event, fxml);
        windowShowing.closeAllWindows();
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
