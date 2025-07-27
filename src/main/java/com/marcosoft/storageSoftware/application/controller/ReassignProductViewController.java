package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
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
public class ReassignProductViewController {

    private Client client;

    private final InventoryServiceImpl inventoryService;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final DisplayAlerts displayAlerts;
    private final ProductServiceImpl productService;
    private final ParseDataTypes parseDataTypes;

    @Lazy
    public ReassignProductViewController(ParseDataTypes parseDataTypes, ProductServiceImpl productService, DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, ClientServiceImpl clientService, UserLogged userLogged, InventoryServiceImpl inventoryService) {
        this.inventoryService = inventoryService;
        this.parseDataTypes = parseDataTypes;
        this.productService = productService;
        this.displayAlerts = displayAlerts;
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
        initClient();
        Platform.runLater(() -> {
            initMbWarehouse();
            initMbWarehouseReceipt();
        });
    }

    private void initClient() {
        client = clientService.getClientByName(userLogged.getName());
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void reassignProduct(ActionEvent actionEvent) {
        if (!validateAllFields()) {
            return;
        }

        try {
            String warehouseGivesName = tfWarehouseGives.getText();
            String warehouseReceivesName = tfWarehouseReceipt.getText();
            String productName = tfProduct.getText();
            Integer amount = parseDataTypes.parseInt(tfAmount.getText());

            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouseGives = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseGivesName, client);
            Warehouse warehouseReceives = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseReceivesName, client);

            Inventory inventoryGives = inventoryService.getByProductAndWarehouseAndClient(product, warehouseGives, client);
            inventoryGives.setAmount(inventoryGives.getAmount() - amount);
            if (inventoryGives.getAmount() == 0) {
                inventoryService.deleteInventoryById(inventoryGives.getId());
            } else {
                inventoryService.save(inventoryGives);
            }

            if (inventoryService.existsByProductAndWarehouseAndClient(product, warehouseReceives, client)) {
                Inventory inventoryReceives = inventoryService.getByProductAndWarehouseAndClient(product, warehouseReceives, client);
                inventoryReceives.setAmount(inventoryReceives.getAmount() + amount);
                inventoryService.save(inventoryReceives);
            } else {
                Inventory newInv = new Inventory(null, product, client, warehouseReceives, amount);
                inventoryService.save(newInv);
            }
            displayAlerts.showAlert("Se ha reasignado el producto satisfactoriamente");
            cleanFields();
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrid un error" + e.getMessage());
        }
    }

    @FXML
    public void assignAllProductAmount(ActionEvent actionEvent) {
        String productName = tfProduct.getText();
        String warehouseGivesName = tfWarehouseGives.getText();
        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseGivesName, client);

        if (productName.isEmpty() && warehouseGivesName.isEmpty()) {
            displayAlerts.showAlert("Debe asignar un almacén y un producto primero");
        } else if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("No se encontró el respectivo producto dentro del almacén que proporcionó");
        } else if (inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client).getAmount() == 0) {
            displayAlerts.showAlert("Esta inversión ha sido completamente asignada, debe seleccionar otra o reasignar los productos de esta");
        } else {
            tfAmount.setText(String.valueOf(inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client).getAmount()));
        }
    }

    // ============================
    // UTILITIES
    // ============================

    private void cleanFields() {
        tfProduct.clear();
        tfWarehouseReceipt.clear();
        tfWarehouseGives.clear();
        tfAmount.clear();
    }

    private boolean validateAllFields() {
        return validateTfWarehouseGives() && validateTfProduct() && validateTfAmount() && validateTfWarehouseReceipt();
    }

    private boolean validateTfWarehouseGives() {
        String warehouseGivesName = tfWarehouseGives.getText();
        if (warehouseGivesName == null || warehouseGivesName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un almacén de origen");
            return false;
        }

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseGivesName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("El almacén de origen seleccionado no existe");
            return false;
        }

        return true;
    }

    private boolean validateTfWarehouseReceipt() {
        String warehouseReceiptName = tfWarehouseReceipt.getText();
        if (warehouseReceiptName == null || warehouseReceiptName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un almacén de destino");
            return false;
        }

        // Validar que no sea el mismo almacén
        if (warehouseReceiptName.equals(tfWarehouseGives.getText())) {
            displayAlerts.showAlert("El almacén de destino no puede ser el mismo que el de origen");
            return false;
        }

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseReceiptName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("El almacén de destino seleccionado no existe");
            return false;
        }

        return true;
    }

    private boolean validateTfProduct() {
        String productName = tfProduct.getText();
        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un producto");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto seleccionado no existe");
            return false;
        }

        // Validar que el producto exista en el almacén de origen
        Warehouse warehouseGives = warehouseService.getWarehouseByWarehouseNameAndClient(tfWarehouseGives.getText(), client);
        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouseGives, client)) {
            displayAlerts.showAlert("El producto no existe en el almacén de origen");
            return false;
        }

        return true;
    }

    private boolean validateTfAmount() {
        String amountText = tfAmount.getText();
        if (amountText == null || amountText.isEmpty()) {
            displayAlerts.showAlert("Debe ingresar una cantidad");
            return false;
        }

        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                displayAlerts.showAlert("La cantidad debe ser mayor a cero");
                return false;
            }

            // Validar que haya suficiente stock en el almacén de origen
            if (validateTfProduct() && validateTfWarehouseGives()) {
                Product product = productService.getByProductNameAndClient(tfProduct.getText(), client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfWarehouseGives.getText(), client);
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                if (inventory.getAmount() < amount) {
                    displayAlerts.showAlert("No hay suficiente stock en el almacén de origen");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }
    }

    private void initMbProduct(Warehouse warehouse) {
        if (warehouse == null || warehouse.getWarehouseName() == null) return;

        mbProduct.getItems().clear();
        List<Inventory> inventory = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);

        if (inventory.isEmpty()) {
            MenuItem item = new MenuItem("No hay productos disponibles");
            item.setDisable(true);
            mbProduct.getItems().add(item);
            return;
        }

        for (Inventory i : inventory) {
            if (i.getProduct() != null && i.getProduct().getProductName() != null) {
                MenuItem item = new MenuItem(i.getProduct().getProductName());
                item.setOnAction(e -> tfProduct.setText(item.getText()));
                mbProduct.getItems().add(item);
            }
        }
    }

    private void initMbWarehouseReceipt() {
        mbWarehouseReceipt.getItems().clear();
        mbWarehouse.getItems().clear();
        Client client = clientService.getClientByName(userLogged.getName());
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);
        for (Warehouse w : warehouses) {
            if (!w.getWarehouseName().equals(tfWarehouseGives.getText())) {
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
            if (!w.getWarehouseName().equals(tfWarehouseReceipt.getText())) {
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
