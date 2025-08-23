package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Lazy
@Controller
public class BalanceViewController {
    private Client client;
    private LocalDate startDate;
    private LocalDate endDate;
    @Setter
    @Getter
    private Currency currency;
    private double totalExpense;
    private double totalProfit;

    private final SceneSwitcher sceneSwitcher;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final SellRegistryServiceImpl sellRegistryService;
    private final CurrencyServiceImpl currencyService;
    private final ExpenseServiceImpl expenseService;

    public BalanceViewController(
            DisplayAlerts displayAlerts, UserLogged userLogged, SceneSwitcher sceneSwitcher, ExpenseServiceImpl expenseService,
            SellRegistryServiceImpl sellRegistryService, CurrencyServiceImpl currencyService
    ) {
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.expenseService = expenseService;
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

        List<DateRangeOption> dateOptions = List.of(
                new DateRangeOption("Hoy", Period.ZERO),
                new DateRangeOption("Última Semana", Period.ofWeeks(1)),
                new DateRangeOption("Último Mes", Period.ofMonths(1)),
                new DateRangeOption("Último Trimestre", Period.ofMonths(3)),
                new DateRangeOption("Último Semestre", Period.ofMonths(6)),
                new DateRangeOption("Último Año", Period.ofYears(1))
        );

        dateOptions.forEach(option -> {
            MenuItem item = new MenuItem(option.getLabel());
            item.setOnAction(e -> setDateRange(option));
            mbDateRange.getItems().add(item);
        });
    }

    private void setDateRange(DateRangeOption option) {
        lblTimeLapse.setText(option.getLabel());
        endDate = LocalDate.now();
        this.startDate = endDate.minus(option.getPeriod());
        refreshBalance();
    }

    // Clase de apoyo para encapsular la lógica de rangos de fecha
    @Getter
    @AllArgsConstructor
    private static class DateRangeOption {
        private final String label;
        private final Period period;

    }

    private void initDefaultValues() {
        startDate = LocalDate.now().minusMonths(1);
        endDate = LocalDate.now();
        currency = currencyService.getCurrencyByName("CUP");
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
    }

    public void refreshBalance() {
        initProfitLabels();
        initExpenseLabels();
        initNetProfit();
    }

    private void initExpenseLabels() {
        try {
            double rent = expenseService.getTotalRentExpense(client, startDate, endDate, currency);
            double salary = expenseService.getTotalSalaryExpense(client, startDate, endDate, currency);
            double publicity = expenseService.getTotalPublicityExpense(client, startDate, endDate, currency);
            double product = expenseService.getTotalProductExpense(client, startDate, endDate, currency);
            double service = expenseService.getTotalServiceExpense(client, startDate, endDate, currency);

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
        String currencyName = currency.getCurrencyName();
        lblNetProfit.setText(netProfit + " " + currencyName );
        if (netProfit < 0) {
            lblNetProfit.setStyle("-fx-text-fill: #e40000");
        } else if (netProfit > 0) {
            lblNetProfit.setStyle("-fx-text-fill: #00ae03");
        }
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    @FXML
    public void exportToExcel() {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void exportToPdf() {
        displayAlerts.showAlert("Próximamente");
    }

    @FXML
    public void displayCurrencyValues() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Valor de Monedas", "/images/lc_logo.png", "/views/currencyValuesView.fxml");
    }
}
