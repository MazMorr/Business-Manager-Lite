package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class ProductionViewController {

    private final SceneSwitcher sceneSwitcher;
    private final DisplayAlerts displayAlerts;

    @FXML
    private TextField tfAmount1, tfAmount4, tfAmount3, tfWarehouse4, tfWarehouse3, tfWarehouse2, tfAmount2,
            tfNewProductDestinyWarehouse, tfNewProductAmount, tfProduct2, tfProduct1, tfProduct4, tfProduct3,
            tfNewProductName, tfWarehouse;
    @FXML
    private MenuButton mbAmount1, mbAmount2, mbAmount3, mbAmount4, mbProduct2, mbProduct3, mbProduct4, mbProduct1,
            mbWarehouse1, mbWarehouse3, mbWarehouse2, mbWarehouse4, mbNewProductDestinyWarehouse;

    public ProductionViewController(SceneSwitcher sceneSwitcher, DisplayAlerts displayAlerts) {
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
    }

    @FXML
    private void initialize() {

    }

    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfAmount1);
    }

    @FXML
    public void produce() {
    }

    @FXML
    public void cleanProduct1(ActionEvent actionEvent) {
    }

    @FXML
    public void cleanProduct3(ActionEvent actionEvent) {
    }

    @FXML
    public void cleanProduct2(ActionEvent actionEvent) {
    }

    @FXML
    public void cleanProduct4(ActionEvent actionEvent) {
    }

    @FXML
    public void cleanNewProduct(ActionEvent actionEvent) {
    }
}
