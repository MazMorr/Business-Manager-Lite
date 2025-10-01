package com.marcosoft.storageSoftware.application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class ProductionViewController {
    @FXML
    private TextField tfAmount1, tfAmount4, tfAmount3, tfWarehouse4, tfWarehouse3, tfWarehouse2, tfAmount2,
            tfNewProductDestinyWarehouse, tfNewProductAmount, tfProduct2, tfProduct1, tfProduct4, tfProduct3,
            tfNewProductName, tfWarehouse;
    @FXML
    private MenuButton mbAmunt1, mbAmunt2, mbAmunt3, mbAmunt4, mbProduct2, mbProduct3, mbProduct4, mbProduct1,
            mbWarehouse1, mbWarehouse3, mbWarehouse2, mbWarehouse4, mbNewProductDestinyWarehouse;

    @FXML
    private void initialize() {

    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
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
