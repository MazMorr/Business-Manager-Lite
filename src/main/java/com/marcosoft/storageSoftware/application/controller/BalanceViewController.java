package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class BalanceViewController {

    private final SceneSwitcher sceneSwitcher;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;

    public BalanceViewController(
            DisplayAlerts displayAlerts, UserLogged userLogged, SceneSwitcher sceneSwitcher
    ) {
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    @FXML
    private Label lblTimeLapse, lblTotalExpense, lblProductExpense, lblRentExpense, lblTotalProfit, lblServiceExpense,
            lblProductProfit, lblPublicityExpense, lblSalaryExpense, lblClientName;

    @FXML
    public void initialize() {
        lblClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            initProfitLabels();
            initExpenseLabels();
        });
    }

    private void initExpenseLabels() {
    }

    private void initProfitLabels() {
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/sellView.fxml");
    }

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void exportToExcel(ActionEvent actionEvent) {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void exportToPdf(ActionEvent actionEvent) {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void displayPrices(ActionEvent actionEvent) {
    }
}
