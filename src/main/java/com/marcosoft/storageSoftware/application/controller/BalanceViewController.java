package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Lazy
@Controller
public class BalanceViewController {
    private Client client;
    @Getter
    @Setter
    private LocalDate startDate;
    @Getter
    @Setter
    private LocalDate endDate;
    private Currency currency;
    private double totalExpense;
    private double totalProfit;

    private final SceneSwitcher sceneSwitcher;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final SellRegistryServiceImpl sellRegistryService;
    private final ClientServiceImpl clientService;
    private final CurrencyServiceImpl currencyService;
    private final InvestmentServiceImpl investmentService;

    public BalanceViewController(
            DisplayAlerts displayAlerts, UserLogged userLogged, SceneSwitcher sceneSwitcher, InvestmentServiceImpl investmentService,
            SellRegistryServiceImpl sellRegistryService, ClientServiceImpl clientService, CurrencyServiceImpl currencyService

    ) {
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.clientService = clientService;
        this.investmentService = investmentService;
        this.sellRegistryService = sellRegistryService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    @FXML
    private Label lblTimeLapse, lblTotalExpense, lblProductExpense, lblRentExpense, lblTotalProfit, lblServiceExpense,
            lblProductProfit, lblPublicityExpense, lblSalaryExpense, lblNetProfit, lblClientName;

    @FXML
    private MenuButton mbDateRange;


    @FXML
    public void initialize() {
        initDefaultValues();
        Platform.runLater(() -> {
            initMbDateRange();
            refreshBalance();
        });
    }

    private void initMbDateRange() {
        mbDateRange.getItems().clear();
        List<MenuItem> items = List.of(
                new MenuItem("Hoy"),
                new MenuItem("Última Semana"),
                new MenuItem("Último Mes"),
                new MenuItem("Último Trimestre"),
                new MenuItem("Último Semestre"),
                new MenuItem("Último Año")
        );
        for (MenuItem item : items) {
            switch (item.getText()) {
                case "Hoy" -> item.setOnAction(e -> initMbDateRangeSwitchHelper(item, LocalDate.now()));
                case "Última Semana" -> item.setOnAction(e ->
                        initMbDateRangeSwitchHelper(item, LocalDate.now().minusWeeks(1)));
                case "Último Mes" -> item.setOnAction(e ->
                        initMbDateRangeSwitchHelper(item, LocalDate.now().minusMonths(1)));
                case "Último Trimestre" -> item.setOnAction(e ->
                        initMbDateRangeSwitchHelper(item, LocalDate.now().minusMonths(3)));
                case "Último Semestre" -> item.setOnAction(e ->
                        initMbDateRangeSwitchHelper(item, LocalDate.now().minusMonths(6)));
                case "Último Año" -> item.setOnAction(e ->
                        initMbDateRangeSwitchHelper(item, LocalDate.now().minusYears(1)));
            }
            mbDateRange.getItems().add(item);
        }
    }

    private void initMbDateRangeSwitchHelper(MenuItem item, LocalDate startDate) {
        lblTimeLapse.setText(item.getText());
        endDate = LocalDate.now();
        this.startDate = startDate;
        refreshBalance();
    }

    private void initDefaultValues() {
        startDate = LocalDate.now().minusMonths(1);
        endDate = LocalDate.now();
        currency = currencyService.getCurrencyByName("CUP");
        lblClientName.setText(userLogged.getName());
        client = clientService.getClientByName(userLogged.getName());
    }

    private void refreshBalance() {
        initProfitLabels();
        initExpenseLabels();
        initNetProfit();
    }

    private void initExpenseLabels() {
        try {
            double rent = investmentService.getTotalRentExpense(client, startDate, endDate, currency);
            double salary = investmentService.getTotalSalaryExpense(client, startDate, endDate, currency);
            double publicity = investmentService.getTotalPublicityExpense(client, startDate, endDate, currency);
            double product = investmentService.getTotalProductExpense(client, startDate, endDate, currency);
            double service = investmentService.getTotalServiceExpense(client, startDate, endDate, currency);

            totalExpense = rent + salary + publicity + product + service;

            lblRentExpense.setText(String.format("$ %.2f", rent));
            lblSalaryExpense.setText(String.format("$ %.2f", salary));
            lblPublicityExpense.setText(String.format("$ %.2f", publicity));
            lblProductExpense.setText(String.format("$ %.2f", product));
            lblServiceExpense.setText(String.format("$ %.2f", service));
            lblTotalExpense.setText(String.format("$ %.2f", totalExpense));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private void initProfitLabels() {
        try {
            double productProfit = sellRegistryService.getTotalProductProfit(client, startDate, endDate, currency);

            totalProfit = productProfit;

            lblProductProfit.setText(String.format("$ %.2f", productProfit));
            lblTotalProfit.setText(String.format("$ %.2f", totalProfit));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private void initNetProfit() {
        double netProfit = totalProfit - totalExpense;
        lblNetProfit.setText(String.format("$ %.2f", netProfit));
        if (netProfit < 0) {
            lblNetProfit.setStyle("-fx-text-fill: #e40000");
        } else if (netProfit > 0) {
            lblNetProfit.setStyle("-fx-text-fill: #00ae03");
        }
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
    public void switchToRegistry(ActionEvent actionEvent) {sceneSwitcher.switchView(actionEvent, "/registryView.fxml");}

    @FXML
    public void exportToExcel(ActionEvent actionEvent) {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void exportToPdf(ActionEvent actionEvent) {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void displayCurrencyValues(ActionEvent actionEvent) {
        sceneSwitcher.displayWindow("Valor de Monedas", "/images/RTS_logo", "/currencyValuesView.fxml");
    }
}
