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

@Lazy
@Controller
public class CurrencyValuesViewController {

    private final DisplayAlerts displayAlerts;
    private final CurrencyServiceImpl currencyService;

    public CurrencyValuesViewController(
            DisplayAlerts displayAlerts, CurrencyServiceImpl currencyService
    ) {
        this.currencyService = currencyService;
        this.displayAlerts = displayAlerts;
    }

    @FXML
    private TextField tfCurrency, tfMLCtoCUP, tfUSDtoCUP, tfEURtoCUP;
    @FXML
    private MenuButton mbCurrency;


    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            initMbCurrency();
        });

    }

    @FXML
    public void assignPrices(ActionEvent actionEvent) {

    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) mbCurrency.getScene().getWindow();
        stage.close();
    }

    private void initMbCurrency() {
        mbCurrency.getItems().clear();
        List<Currency> currencyList = currencyService.getAllCurrencies();

        for (Currency c : currencyList) {
            MenuItem item = new MenuItem(c.getCurrencyName());
            item.setOnAction(e -> {
                tfCurrency.setText(item.getText());
            });
            mbCurrency.getItems().add(item);
        }
    }
}
