package com.marcosoft.storageSoftware.application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class EstablishBalanceDateViewController {
    private final BalanceViewController balanceViewController;

    public EstablishBalanceDateViewController(BalanceViewController balanceViewController) {
        this.balanceViewController = balanceViewController;
    }

    @FXML
    private DatePicker dpStartDate, dpEndDate;

    @FXML
    private void initialize() {
        dpStartDate.setValue(BalanceViewController.getStartDate());
        dpEndDate.setValue(BalanceViewController.getEndDate());
    }

    @FXML
    public void establishDates() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        BalanceViewController.setStartDate(startDate);
        BalanceViewController.setEndDate(endDate);
        balanceViewController.getLabelTimeLapse().setText(startDate + " / " + endDate);
        balanceViewController.refreshBalance();
    }

    @FXML
    public void goOut() {
        Stage stage = (Stage) dpEndDate.getScene().getWindow();
        stage.close();
    }
}
