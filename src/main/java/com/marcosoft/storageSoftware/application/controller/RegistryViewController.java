package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class RegistryViewController {
    private FilteredList<Investment> filteredInvestments;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final InvestmentServiceImpl investmentService;
    private final SceneSwitcher sceneSwitcher;

    @Lazy
    public RegistryViewController(UserLogged userLogged, ClientServiceImpl clientService, SceneSwitcher sceneSwitcher, InvestmentServiceImpl investmentService) {
        this.userLogged = userLogged;
        this.clientService = clientService;
        this.investmentService = investmentService;
        this.sceneSwitcher = sceneSwitcher;
    }

    @FXML
    private Label txtClientName;


    @FXML
    public void initialize() {
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            initTableValues();
        });
    }

    private void initTableValues() {

    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent){
        switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
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
