package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class SupportViewController {

    @FXML
    private Label txtWelcome, versionLabel, txtClientName;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;
    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    private ClientServiceImpl clientServiceImpl;

    private final WindowShowing windowShowing = new WindowShowing();

    private ClientViewController accountController;

    public void setAccountController(ClientViewController clientViewController) {
        this.accountController = clientViewController;
        System.out.println("Controlador de cuenta configurado: " + clientViewController);
    }

    @FXML
    private void initialize() {
        versionLabel.setText("Versión: Alfa");
        txtWelcome.setText(
                "Este software le facilitará el control físico-financiero de su negocio, " +
                "poniendo a su disposición múltiples formas de manejar y almacenar productos. " +
                "Tendrá control total de existencias, costos y ganancias. " +
                "Para cualquier problema con el rendimiento, por favor contacte por las vías mostradas a la derecha. " +
                "¡Gracias por confiar en nosotros!"
        );
        try {
            String clientName = clientServiceImpl.getByIsClientActive(true).getClientName();
            txtClientName.setText(clientName != null ? clientName : "Usuario");
        } catch (Exception e) {
            txtClientName.setText("Usuario");
        }
    }

    @FXML
    private void switchToRegistry(ActionEvent event) {
        switchView(event, "/registryView.fxml");
    }

    @FXML
    private void switchToStock(ActionEvent event) {
        switchView(event, "/stockView.fxml");
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
