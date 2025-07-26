package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
@Lazy
@Controller
public class ConfigurationViewController {

    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;

    @Lazy
    public ConfigurationViewController(ClientServiceImpl clientService, SceneSwitcher sceneSwitcher) {
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
    }

    @FXML
    private MenuButton mbAppTheme;
    @FXML
    private RadioMenuItem rdmiDarkTheme, rdmiLightTheme;
    @FXML
    private Label txtClientName, txtClientCompany;
    @FXML
    private ToggleGroup rdmiTheme;

    @FXML
    void closeSession() {
        Client client = clientService.getByIsClientActive(true);
        clientService.updateIsClientActiveByClientName(false, client.getClientName());
        //Aqui básicamente cerraría todas las ventanas y volvería a iniciar la aplicación
        Main.launch();
    }

    @FXML
    public void initialize() {

        //Recoge los nombres y la compañia del usuario
        txtClientName.setText(clientService.getByIsClientActive(true).getClientName());
        txtClientCompany.setText(clientService.getByIsClientActive(true).getClientCompany());
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        switchView(actionEvent, "/sellView.fxml");
    }

    private void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            sceneSwitcher.setRootWithEvent(actionEvent, fxmlPath);
        } catch (Exception e) {
            showAlert("Error al cambiar de vista: " + e.getMessage());
        }
    }

    // ============================
    // UTILIDADES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
