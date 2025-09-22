package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The type Warning manager view controller.
 */
@Lazy
@Controller
public class WarningManagerViewController {
    private Client client;

    private final InventoryServiceImpl inventoryService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final SellViewController sellViewController;

    /**
     * Instantiates a new Warning manager view controller.
     *
     * @param inventoryService   the inventory service
     * @param userLogged         the user logged
     * @param warehouseService   the warehouse service
     * @param displayAlerts      the display alerts
     * @param productService     the product service
     * @param sellViewController the sell view controller
     */
    public WarningManagerViewController(
            InventoryServiceImpl inventoryService, UserLogged userLogged,
            WarehouseServiceImpl warehouseService, DisplayAlerts displayAlerts, ProductServiceImpl productService,
            SellViewController sellViewController
    ) {
        this.userLogged = userLogged;
        this.sellViewController = sellViewController;
        this.displayAlerts = displayAlerts;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
    }

    @FXML
    private MenuButton mbWarehouse, mbProduct;
    @FXML
    private TextField tfWarning, tfProduct, tfWarehouse, tfAlert;


    @FXML
    private void initialize() {
        client = userLogged.getClient();
        Platform.runLater(() -> {
            initTfProductListener();
            initValuesInTextFields();
            initMbProducts();
            loadWarningAndAlerts();
        });
    }

    private void initValuesInTextFields() {
        if (!sellViewController.ttvInventory.getSelectionModel().isEmpty()) {
            tfProduct.setText(sellViewController.tfSellProductName.getText());
            tfWarehouse.setText(sellViewController.tfSellWarehouse.getText());
        }
    }

    private void initTfProductListener() {
        tfProduct.textProperty().addListener((obs, oldValue, newValue) -> {
            // 1. Filter MenuButton items based on input
            filterProductItems(newValue);

            // 2. If product exists in database, load warehouses
            if (isProductCompleteAndValid(newValue)) {
                loadMbWarehouseIfProductExists(newValue);
            }
        });
    }

    private void filterProductItems(String input) {
        if (input == null || input.isEmpty()) {
            // Show all items when input is empty
            mbProduct.getItems().forEach(item -> item.setVisible(true));
            return;
        }

        String searchLower = input.toLowerCase();
        mbProduct.getItems().forEach(item -> {
            boolean matches = item.getText().toLowerCase().contains(searchLower);
            item.setVisible(matches);
        });

        // Show the dropdown when typing
        if (!mbProduct.isShowing()) {
            mbProduct.show();
        }
    }

    private boolean isProductCompleteAndValid(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }

        // Check if the product exists in the database
        try {
            Product product = productService.getByProductNameAndClient(productName.trim(), client);
            return product != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadMbWarehouseIfProductExists(String productName) {
        mbWarehouse.getItems().clear();
        tfWarehouse.clear();

        try {
            List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
            initMbWarehouse(productName.trim(), inventories);
        } catch (Exception e) {
            displayAlerts.showError("Error al cargar almacenes: " + e.getMessage());
        }
    }

    private void initMbProducts() {
        mbProduct.getItems().clear();
        List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
        Set<String> addedProducts = new HashSet<>();

        for (Inventory inventory : inventories) {
            // Skip null inventory or inventory with null product
            if (inventory == null || inventory.getProduct() == null) {
                continue;
            }

            String productName = inventory.getProduct().getProductName();
            // Skip if product name is null or empty
            if (productName == null || productName.trim().isEmpty()) {
                continue;
            }

            if (!addedProducts.contains(productName)) {
                MenuItem item = new MenuItem(productName);
                item.setOnAction(e -> {
                    tfProduct.setText(productName);
                    initMbWarehouse(productName, inventories);
                });
                mbProduct.getItems().add(item);
                addedProducts.add(productName);
            }
        }
    }

    private void initMbWarehouse(String productName, List<Inventory> inventories) {
        mbWarehouse.getItems().clear();
        Set<String> addedWarehouses = new HashSet<>();

        for (Inventory inventory : inventories) {
            // Add null checks for inventory, product, and warehouse
            if (inventory == null || inventory.getProduct() == null || inventory.getWarehouse() == null) {
                continue; // Skip invalid inventory records
            }

            // Compare product names safely
            String currentProductName = inventory.getProduct().getProductName();
            if (currentProductName == null || !currentProductName.equals(productName)) {
                continue;
            }

            String warehouseName = inventory.getWarehouse().getWarehouseName();
            if (warehouseName == null) {
                continue; // Skip if warehouse name is null
            }

            if (!addedWarehouses.contains(warehouseName)) {
                MenuItem item = new MenuItem(warehouseName);
                item.setOnAction(e -> {
                    tfWarehouse.setText(warehouseName);
                    loadWarningAndAlerts();
                });
                mbWarehouse.getItems().add(item);
                addedWarehouses.add(warehouseName);
            }
        }
    }

    private void loadWarningAndAlerts() {
        // Clear previous values
        tfAlert.clear();
        tfWarning.clear();

        // Get current selections
        String productName = tfProduct.getText();
        String warehouseName = tfWarehouse.getText();

        // Validate we have both product and warehouse selected
        if (productName == null || productName.isEmpty() ||
                warehouseName == null || warehouseName.isEmpty()) {
            return;
        }

        try {
            // Get the inventory record for this product+warehouse combination
            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
            Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

            // Load existing values if they exist
            if (inventory.getAmountAlert() != null) {
                tfAlert.setText(String.valueOf(inventory.getAmountAlert()));
            }
            if (inventory.getAmountWarning() != null) {
                tfWarning.setText(String.valueOf(inventory.getAmountWarning()));
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al cargar las alertas: " + e.getMessage());
        }
    }

    /**
     * Assign alerts.
     */
    @FXML
    public void assignAlerts() {
        if (!validateAllTextFields()) {
            return;
        }
        String productName = tfProduct.getText();
        String warehouseName = tfWarehouse.getText();
        String amountAlert = tfAlert.getText();
        String amountWarning = tfWarning.getText();

        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

        // Only set values if they're not empty
        if (!amountAlert.isEmpty()) {
            inventory.setAmountAlert(Integer.parseInt(amountAlert));
        }
        if (!amountWarning.isEmpty()) {
            inventory.setAmountWarning(Integer.parseInt(amountWarning));
        }

        inventoryService.save(inventory);
        sellViewController.loadProductTable();
        sellViewController.loadWarningAndAlertLabels();
        displayAlerts.showAlert("Operación exitosa");
    }

    private boolean validateAllTextFields() {
        return validateTfAlertAndWarning() && validateTfProduct() && validateTfWarehouse();
    }

    private boolean validateTfWarehouse() {
        String warehouseName = tfWarehouse.getText();
        if (warehouseName == null || warehouseName.trim().isEmpty()) {
            displayAlerts.showError("Debe seleccionar un almacén");
            return false;
        }

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (warehouse == null) {
            displayAlerts.showError("El almacén seleccionado no existe");
            return false;
        }

        return true;
    }

    private boolean validateTfProduct() {
        String productName = tfProduct.getText();
        if (productName == null || productName.trim().isEmpty()) {
            displayAlerts.showError("Debe seleccionar un producto");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showError("El producto seleccionado no existe");
            return false;
        }

        return true;
    }

    private boolean validateTfAlertAndWarning() {
        String alertText = tfAlert.getText();
        String warningText = tfWarning.getText();

        // Handle null cases by converting to empty strings
        alertText = alertText == null ? "" : alertText.trim();
        warningText = warningText == null ? "" : warningText.trim();

        // Check if both fields are empty
        if (alertText.isEmpty() && warningText.isEmpty()) {
            displayAlerts.showError("Debe establecer al menos un valor de Alerta o Warning");
            return false;
        }

        // Validate numeric format first
        try {
            Integer alertValue = null;
            Integer warningValue = null;

            if (!alertText.isEmpty()) {
                alertValue = Integer.parseInt(alertText);
            }
            if (!warningText.isEmpty()) {
                warningValue = Integer.parseInt(warningText);
            }

            // Only compare if both values exist
            if (alertValue != null && warningValue != null) {
                if (alertValue >= warningValue) {
                    displayAlerts.showError("La cantidad de productos para producir una ALERTA debe ser mayor que la ADVERTENCIA");
                    return false;
                }
            }

            // Additional validation (optional)
            if (alertValue != null && alertValue < 0) {
                displayAlerts.showError("El valor de Alerta no puede ser negativo");
                return false;
            }
            if (warningValue != null && warningValue < 0) {
                displayAlerts.showError("El valor de Warning no puede ser negativo");
                return false;
            }

        } catch (NumberFormatException e) {
            displayAlerts.showError("Los valores de Alerta y Warning deben ser números enteros válidos");
            return false;
        }

        return true;
    }

    /**
     * Go out.
     */
    @FXML
    public void goOut() {
        Stage stage = (Stage) mbWarehouse.getScene().getWindow();
        stage.close();
    }
}
