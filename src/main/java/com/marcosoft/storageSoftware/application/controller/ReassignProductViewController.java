package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ReassignProductViewController {
    private Client client;

    // Service and utility dependencies
    private final InventoryServiceImpl inventoryService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final DisplayAlerts displayAlerts;
    private final ProductServiceImpl productService;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final WarehouseViewController warehouseViewController;

    @FXML
    private MenuButton mbWarehouse, mbWarehouseReceipt, mbProduct;
    @FXML
    private TextField tfAmount, tfProduct, tfWarehouseReceipt, tfWarehouseGives;

    @FXML
    public void initialize() {
        initClient();
        Platform.runLater(this::initMbWarehouse);
    }

    private void initClient() {
        client = userLogged.getClient();
    }

    @FXML
    public void goOut() {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void reassignProduct() {
        if (!validateAllFields()) return;

        try {
            String warehouseGivesName = tfWarehouseGives.getText();
            String warehouseReceivesName = tfWarehouseReceipt.getText();
            String productName = tfProduct.getText();
            Integer amount = parseDataTypes.parseInt(tfAmount.getText());

            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouseGives = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseGivesName, client);
            Warehouse warehouseReceives = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseReceivesName, client);

            Inventory inventoryGives = inventoryService.getByProductAndWarehouseAndClient(product, warehouseGives, client);

            // Validar que hay suficiente stock
            if (inventoryGives.getAmount() < amount) {
                displayAlerts.showAlert("No hay suficiente stock en el almacén de origen");
                return;
            }

            // Reducir cantidad en el almacén de origen
            inventoryGives.setAmount(inventoryGives.getAmount() - amount);
            if (inventoryGives.getAmount() == 0) {
                inventoryService.deleteInventoryById(inventoryGives.getId());
            } else {
                inventoryService.save(inventoryGives);
            }

            // Manejar el almacén de destino
            if (inventoryService.existsByProductAndWarehouseAndClient(product, warehouseReceives, client)) {
                // Si ya existe el producto en el almacén destino, actualizar la cantidad
                Inventory inventoryReceives = inventoryService.getByProductAndWarehouseAndClient(product, warehouseReceives, client);
                inventoryReceives.setAmount(inventoryReceives.getAmount() + amount);
                inventoryService.save(inventoryReceives);
            } else {
                // Crear nuevo inventario en el almacén destino COPIANDO LA INFORMACIÓN DE COSTO
                Inventory newInv = new Inventory(
                        null,
                        product,
                        client,
                        warehouseReceives,
                        amount,
                        inventoryGives.getUnitPrice(),  // Copiar precio unitario
                        inventoryGives.getCurrency(),   // Copiar moneda
                        inventoryGives.getBuyId(),      // Copiar ID de compra
                        inventoryGives.getAmountAlert(), // Copiar alerta de cantidad
                        inventoryGives.getAmountWarning() // Copiar advertencia de cantidad
                );
                inventoryService.save(newInv);
            }

            LocalDateTime registryMoment = LocalDateTime.now();
            String registryType = "Reasignación de Producto";
            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Almacenes", registryType, registryMoment
            );
            generalRegistryService.save(generalRegistry);

            WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                    null, client, registryType, registryMoment,
                    warehouseGives.getWarehouseName(), product.getProductName(), amount
            );
            warehouseRegistryService.save(warehouseRegistry);

            displayAlerts.showAlert("Se ha reasignado el producto satisfactoriamente");
            cleanFields();

            warehouseViewController.initializeTreeTable();
        } catch (Exception e) {
            displayAlerts.showError("Ha ocurrido un error: " + e.getMessage());
        }
    }

    @FXML
    public void assignAllProductAmount() {
        String productName = tfProduct.getText();
        String warehouseGivesName = tfWarehouseGives.getText();

        if (productName.isEmpty() || warehouseGivesName.isEmpty()) {
            displayAlerts.showAlert("Debe asignar un almacén y un producto primero");
            return;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseGivesName, client);

        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("No se encontró el respectivo producto dentro del almacén que proporcionó");
        } else {
            Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
            if (inventory.getAmount() == 0) {
                displayAlerts.showAlert("No hay stock disponible de este producto en el almacén seleccionado");
            } else {
                tfAmount.setText(String.valueOf(inventory.getAmount()));
            }
        }
    }

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

            if (validateTfProduct() && validateTfWarehouseGives()) {
                Product product = productService.getByProductNameAndClient(tfProduct.getText(), client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfWarehouseGives.getText(), client);
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                if (inventory.getAmount() < amount) {
                    displayAlerts.showAlert("No hay suficiente stock en el almacén de origen. Stock disponible: " + inventory.getAmount());
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
        mbProduct.getItems().clear();
        if (warehouse == null) return;

        List<Inventory> inventory = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);

        if (inventory.isEmpty()) {
            MenuItem item = new MenuItem("No hay productos disponibles");
            item.setDisable(true);
            mbProduct.getItems().add(item);
            return;
        }

        inventory.stream()
                .filter(i -> i.getProduct() != null && i.getAmount() != null && i.getAmount() > 0)
                .forEach(i -> {
                    MenuItem item = new MenuItem(i.getProduct().getProductName());
                    item.setOnAction(e -> tfProduct.setText(i.getProduct().getProductName()));
                    mbProduct.getItems().add(item);
                });
    }

    private void initMbWarehouse() {
        mbWarehouse.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);

        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e -> {
                tfWarehouseGives.setText(item.getText());
                initMbProduct(w);
                updateReceivingWarehouses(w.getWarehouseName());
                cleanFieldsExceptWarehouseGives();
            });
            mbWarehouse.getItems().add(item);
        }
    }

    private void cleanFieldsExceptWarehouseGives() {
        tfProduct.clear();
        tfWarehouseReceipt.clear();
        tfAmount.clear();
    }

    private void updateReceivingWarehouses(String excludedWarehouseName) {
        mbWarehouseReceipt.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);

        for (Warehouse w : warehouses) {
            if (!w.getWarehouseName().equals(excludedWarehouseName)) {
                MenuItem item = new MenuItem(w.getWarehouseName());
                item.setOnAction(e -> tfWarehouseReceipt.setText(w.getWarehouseName()));
                mbWarehouseReceipt.getItems().add(item);
            }
        }
    }
}