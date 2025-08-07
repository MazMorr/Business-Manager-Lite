package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.fxml.FXML;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class PricesViewController {

    private final DisplayAlerts displayAlerts;

    public PricesViewController(DisplayAlerts displayAlerts){
        this.displayAlerts = displayAlerts;
    }

    @FXML
    private void initialize(){

    }
}
