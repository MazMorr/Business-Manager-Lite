package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Currency values view controller.
 */
@Lazy
@Controller
public class CurrencyValuesViewController {

    // Constantes para nombres de monedas
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final String MLC = "MLC";

    private final DisplayAlerts displayAlerts;
    private final CurrencyServiceImpl currencyService;
    private final BalanceViewController balanceViewController;

    /**
     * Instantiates a new Currency values view controller.
     *
     * @param displayAlerts the display alerts
     * @param currencyService the currency service
     * @param balanceViewController the balance view controller
     */
    public CurrencyValuesViewController(
            DisplayAlerts displayAlerts,
            CurrencyServiceImpl currencyService,
            BalanceViewController balanceViewController) {
        this.currencyService = currencyService;
        this.balanceViewController = balanceViewController;
        this.displayAlerts = displayAlerts;
    }

    @FXML private TextField tfCurrency, tfMLCtoCUP, tfUSDtoCUP, tfEURtoCUP;
    @FXML private MenuButton mbCurrency;

    @FXML
    private void initialize() {
        Platform.runLater(this::initMbCurrency);
    }

    /**
     * Update currency values.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void updateCurrencyValues(ActionEvent actionEvent) {
        if (!validateAllPrices()) {
            return;
        }

        updateAllCurrencyValues();
        displayAlerts.showAlert("Valores de moneda actualizados exitosamente");
    }

    private void updateAllCurrencyValues() {
        currencyService.getAllCurrencies().forEach(currency -> {
            switch (currency.getCurrencyName()) {
                case USD -> currency.setCurrencyPriceInCUP(parseDouble(tfUSDtoCUP));
                case EUR -> currency.setCurrencyPriceInCUP(parseDouble(tfEURtoCUP));
                case MLC -> currency.setCurrencyPriceInCUP(parseDouble(tfMLCtoCUP));
            }
            currencyService.save(currency);
        });
    }

    private double parseDouble(TextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    private boolean validateAllPrices() {
        return validateCurrencyField(tfUSDtoCUP, USD) &&
                validateCurrencyField(tfEURtoCUP, EUR) &&
                validateCurrencyField(tfMLCtoCUP, MLC);
    }

    private boolean validateCurrencyField(TextField field, String currencyName) {
        String input = field.getText().trim();

        if (input.isEmpty()) {
            showFieldError(field, String.format("El precio %s a CUP no puede estar vacío", currencyName));
            return false;
        }

        try {
            double value = Double.parseDouble(input);
            if (value <= 0) {
                showFieldError(field, String.format("El precio %s a CUP debe ser mayor que 0", currencyName));
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showFieldError(field, String.format("Por favor ingrese un número válido para %s a CUP", currencyName));
            return false;
        }
    }

    private void showFieldError(TextField field,  String message) {
        displayAlerts.showAlert( message);
        field.requestFocus();
    }

    /**
     * Go out.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) mbCurrency.getScene().getWindow();
        stage.close();
    }

    private void initMbCurrency() {
        mbCurrency.getItems().clear();
        List<Currency> currencyList = currencyService.getAllCurrencies();

        currencyList.forEach(currency -> {
            MenuItem item = new MenuItem(currency.getCurrencyName());
            item.setOnAction(e -> tfCurrency.setText(currency.getCurrencyName()));
            mbCurrency.getItems().add(item);
        });
    }

    /**
     * Update balance.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void updateBalance(ActionEvent actionEvent) {
        if (!validateCurrencySelection()) {
            return;
        }

        Currency currency = currencyService.getCurrencyByName(tfCurrency.getText().trim());
        updateBalanceView(currency);
        displayAlerts.showAlert("Balance actualizado exitosamente");
    }

    private boolean validateCurrencySelection() {
        String currencyName = tfCurrency.getText().trim();
        List<String> availableCurrencies = getAvailableCurrencyNames();

        if (currencyName.isEmpty()) {
            showCurrencyError("Debe seleccionar una moneda");
            return false;
        }

        if (!availableCurrencies.contains(currencyName)) {
            showCurrencyError(String.format(
                    "Moneda no válida. Opciones disponibles: %s",
                    String.join(", ", availableCurrencies)));
            return false;
        }

        return true;
    }

    private List<String> getAvailableCurrencyNames() {
        return currencyService.getAllCurrencies().stream()
                .map(Currency::getCurrencyName)
                .collect(Collectors.toList());
    }

    private void showCurrencyError(String message) {
        displayAlerts.showAlert(message);
        tfCurrency.requestFocus();
    }

    private void updateBalanceView(Currency currency) {
        balanceViewController.setCurrency(currency);
        balanceViewController.refreshBalance();
    }
}
