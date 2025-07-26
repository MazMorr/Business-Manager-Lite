package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;

@Lazy
@Controller
public class ReassignProductViewController {

    private final InventoryServiceImpl inventoryService;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;

    @Lazy
    public ReassignProductViewController(WarehouseServiceImpl warehouseService, ClientServiceImpl clientService, UserLogged userLogged, InventoryServiceImpl inventoryService) {
        this.inventoryService = inventoryService;
        this.clientService = clientService;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
    }

    @FXML
    private MenuButton mbWarehouse, mbWarehouseReceipt, mbProduct;
    @FXML
    private TextField tfAmount, tfProduct, tfWarehouseReceipt, tfWarehouseGives;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initMbWarehouse();
            initMbWarehouseReceipt();
        });
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void reassignProduct(ActionEvent actionEvent) {

    }

    // ============================
    // UTILITIES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initMbProduct(Warehouse warehouse) {
        mbProduct.getItems().clear();
        Client client = clientService.getClientByName(userLogged.getName());
        List<Inventory> inventory = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);

        for (Inventory i : inventory) {
            MenuItem item = new MenuItem(i.getProduct().getProductName());
            item.setOnAction(e -> {
                tfProduct.setText(item.getText());
            });
            mbProduct.getItems().add(item);
        }
    }

    private void initMbWarehouseReceipt() {
        mbWarehouseReceipt.getItems().clear();
        mbWarehouse.getItems().clear();
        Client client = clientService.getClientByName(userLogged.getName());
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);
        for (Warehouse w : warehouses) {
            if(!w.getWarehouseName().equals(tfWarehouseGives.getText())){
                MenuItem item = new MenuItem(w.getWarehouseName());
                item.setOnAction(e -> {
                    tfWarehouseGives.setText(item.getText());
                    initMbWarehouse();
                });
                mbWarehouse.getItems().add(item);
            }
        }
    }

    private void initMbWarehouse() {
        mbWarehouse.getItems().clear();
        Client client = clientService.getClientByName(userLogged.getName());
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);
        for (Warehouse w : warehouses) {
            if(!w.getWarehouseName().equals(tfWarehouseReceipt.getText())){
                MenuItem item = new MenuItem(w.getWarehouseName());
                item.setOnAction(e -> {
                    tfWarehouseGives.setText(item.getText());
                    initMbProduct(w);
                    initMbWarehouseReceipt();
                });
                mbWarehouse.getItems().add(item);
            }
        }
    }
}
