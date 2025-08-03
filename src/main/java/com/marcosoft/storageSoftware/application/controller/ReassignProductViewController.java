package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the product reassignment view.
 * Handles logic for transferring products between warehouses.
 */
@Lazy
@Controller
public class ReassignProductViewController {

    private Client client;

    // Service and utility dependencies
    private final InventoryServiceImpl inventoryService;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final DisplayAlerts displayAlerts;
    private final ProductServiceImpl productService;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;

    /**
     * Constructor for dependency injection.
     */
    @Lazy
    public ReassignProductViewController(
            GeneralRegistryServiceImpl generalRegistryService, WarehouseRegistryServiceImpl warehouseRegistryService,
            ParseDataTypes parseDataTypes, ProductServiceImpl productService, DisplayAlerts displayAlerts,
            WarehouseServiceImpl warehouseService, ClientServiceImpl clientService, UserLogged userLogged,
            InventoryServiceImpl inventoryService
    ) {
        this.inventoryService = inventoryService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.generalRegistryService = generalRegistryService;
        this.parseDataTypes = parseDataTypes;
        this.productService = productService;
        this.displayAlerts = displayAlerts;
        this.clientService = clientService;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
    }

    // FXML UI components
    @FXML
    private MenuButton mbWarehouse, mbWarehouseReceipt, mbProduct;
    @FXML
    private TextField tfAmount, tfProduct, tfWarehouseReceipt, tfWarehouseGives;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads client and initializes warehouse menus.
     */
    @FXML
    public void initialize() {
        initClient();
        Platform.runLater(() -> {
            initMbWarehouse();
            initMbWarehouseReceipt();
        });
    }

    /**
     * Loads the client based on the logged user.
     */
    private void initClient() {
        client = clientService.getClientByName(userLogged.getName());
    }

    /**
     * Closes the current window.
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the product reassignment between warehouses.
     * Validates fields, updates inventories, and shows alerts in Spanish.
     */
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

            LocalDateTime registryMoment = LocalDateTime.now();
            String registryType = "Reasignación de Producto";
            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Almacenes", registryType, registryMoment
            );
            generalRegistryService.save(generalRegistry);

            WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                    null, client, registryType, registryMoment, warehouseGives.getWarehouseName(), product.getProductName() , amount
            );
            warehouseRegistryService.save(warehouseRegistry);

            displayAlerts.showAlert("Se ha reasignado el producto satisfactoriamente");
            cleanFields();
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrid un error" + e.getMessage());
        }
    }

    /**
     * Assigns all available product amount from the origin warehouse to the destination.
     * Shows alerts in Spanish if validation fails.
     */
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

    /**
     * Clears all input fields in the form.
     */
    private void cleanFields() {
        tfProduct.clear();
        tfWarehouseReceipt.clear();
        tfWarehouseGives.clear();
        tfAmount.clear();
    }

    /**
     * Validates all required fields before reassignment.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateAllFields() {
        return validateTfWarehouseGives() && validateTfProduct() && validateTfAmount() && validateTfWarehouseReceipt();
    }

    /**
     * Validates the origin warehouse field.
     */
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

    /**
     * Validates the destination warehouse field.
     */
    private boolean validateTfWarehouseReceipt() {
        String warehouseReceiptName = tfWarehouseReceipt.getText();
        if (warehouseReceiptName == null || warehouseReceiptName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un almacén de destino");
            return false;
        }

        // Validate that origin and destination warehouses are not the same
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

    /**
     * Validates the product field.
     */
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

        // Validate that the product exists in the origin warehouse
        Warehouse warehouseGives = warehouseService.getWarehouseByWarehouseNameAndClient(tfWarehouseGives.getText(), client);
        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouseGives, client)) {
            displayAlerts.showAlert("El producto no existe en el almacén de origen");
            return false;
        }

        return true;
    }

    /**
     * Validates the amount field.
     */
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

            // Validate that there is enough stock in the origin warehouse
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

    /**
     * Initializes the product menu for the selected warehouse.
     * Populates the menu with available products.
     */
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

    /**
     * Initializes the destination warehouse menu.
     * Populates the menu with available warehouses except the origin.
     */
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

    /**
     * Initializes the origin warehouse menu.
     * Populates the menu with available warehouses except the destination.
     */
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
