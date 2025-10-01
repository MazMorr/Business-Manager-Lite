package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
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
 * Controller for the change product name view.
 * Handles logic for updating the name of a product for the current client.
 */
@Lazy
@Controller
public class RenameProductViewController {
    private Client client;

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseViewController warehouseViewController;
    private final InventoryServiceImpl inventoryService;
    private final BuyServiceImpl buyService;
    private final CurrencyServiceImpl currencyService; // Servicio de monedas agregado

    public RenameProductViewController(
            WarehouseViewController warehouseViewController, GeneralRegistryServiceImpl generalRegistryService,
            DisplayAlerts displayAlerts, UserLogged userLogged, ProductServiceImpl productService,
            InventoryServiceImpl inventoryService, BuyServiceImpl buyService, CurrencyServiceImpl currencyService
    ) {
        this.productService = productService;
        this.warehouseViewController = warehouseViewController;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.inventoryService = inventoryService;
        this.buyService = buyService;
        this.currencyService = currencyService;
    }

    // FXML UI components
    @FXML
    private TextField tfActualName, tfNewName;
    @FXML
    private MenuButton mbActualName;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads client and initializes product menu.
     */
    @FXML
    private void initialize() {
        client = userLogged.getClient();
        Platform.runLater(this::initMbActualName);
    }

    /**
     * Handles the update of a product name when the user clicks the update button.
     * Validates input and shows alerts in Spanish if validation fails.
     */
    @FXML
    public void updateProductName() {
        if (!validateAllFields()) {
            return;
        }

        try {
            String oldProductName = tfActualName.getText();
            String newProductName = tfNewName.getText();

            // Verificar que el nuevo nombre sea diferente
            if (oldProductName.equals(newProductName)) {
                displayAlerts.showAlert("El nuevo nombre debe ser diferente al nombre actual");
                return;
            }

            Product oldProduct = productService.getByProductNameAndClient(oldProductName, client);
            if (oldProduct == null) {
                displayAlerts.showAlert("El producto de origen no existe");
                return;
            }

            // Verificar si ya existe un producto con el nuevo nombre
            Product existingProduct = productService.getByProductNameAndClient(newProductName, client);

            if (existingProduct != null) {
                // FUSIÓN: Producto destino ya existe - fusionar inventarios y compras
                mergeProducts(oldProduct, existingProduct);
                displayAlerts.showAlert("Producto fusionado correctamente. Las cantidades se han sumado.");
            } else {
                // RENOMBRADO SIMPLE: Solo cambiar el nombre
                renameProduct(oldProduct, newProductName);
                displayAlerts.showAlert("El nuevo nombre ha sido correctamente asignado");
            }

            // Actualizar registros y UI
            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Almacenes", "Cambio Nombre Producto: " + oldProductName + " → " + newProductName,
                    LocalDateTime.now()
            );
            generalRegistryService.save(generalRegistry);

            clearFields();
            warehouseViewController.initTableValues();
            warehouseViewController.initTreeTable();

        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fusiona dos productos: transfiere inventarios y compras del producto antiguo al existente
     */
    private void mergeProducts(Product sourceProduct, Product targetProduct) {
        // 1. Fusionar inventarios
        mergeInventories(sourceProduct, targetProduct);

        // 2. Fusionar compras (Buys) - actualizar referencias
        mergeBuys(sourceProduct, targetProduct);

        // 3. Eliminar el producto fuente después de la fusión
        productService.deleteById(sourceProduct.getId());
    }

    /**
     * Fusiona los inventarios del producto fuente al producto destino
     */
    private void mergeInventories(Product sourceProduct, Product targetProduct) {
        List<Inventory> sourceInventories = inventoryService.getAllInventoriesByProductAndClient(sourceProduct, client);

        for (Inventory sourceInv : sourceInventories) {
            // Buscar si ya existe un inventario para el mismo producto y almacén
            Inventory existingInv = inventoryService.getByProductAndWarehouseAndClient(
                    targetProduct, sourceInv.getWarehouse(), client);

            if (existingInv != null) {
                // SUMAR cantidades si ya existe
                int newAmount = existingInv.getAmount() + sourceInv.getAmount();
                existingInv.setAmount(newAmount);

                // Actualizar precios si es necesario (usar el promedio ponderado)
                updateMergedInventoryPrices(existingInv, sourceInv);

                inventoryService.save(existingInv);
                inventoryService.deleteInventoryById(sourceInv.getId());
            } else {
                // Si no existe, transferir el inventario al nuevo producto
                sourceInv.setProduct(targetProduct);
                inventoryService.save(sourceInv);
            }
        }
    }

    /**
     * Actualiza los precios del inventario fusionado usando promedio ponderado
     */
    private void updateMergedInventoryPrices(Inventory targetInv, Inventory sourceInv) {
        if (targetInv.getUnitPrice() != null && sourceInv.getUnitPrice() != null) {
            int totalAmount = targetInv.getAmount() + sourceInv.getAmount();

            // Si las monedas son diferentes, convertir a CUP y promediar
            if (!targetInv.getCurrency().equals(sourceInv.getCurrency())) {
                // Convertir ambos a CUP para el promedio
                double targetPriceCUP = convertToCUP(targetInv.getUnitPrice(), targetInv.getCurrency());
                double sourcePriceCUP = convertToCUP(sourceInv.getUnitPrice(), sourceInv.getCurrency());

                double weightedPriceCUP = (targetPriceCUP * targetInv.getAmount() +
                        sourcePriceCUP * sourceInv.getAmount()) / totalAmount;

                targetInv.setUnitPrice(weightedPriceCUP);
                targetInv.setCurrency("CUP");
            } else {
                // Misma moneda - promedio ponderado directo
                double weightedPrice = (targetInv.getUnitPrice() * targetInv.getAmount() +
                        sourceInv.getUnitPrice() * sourceInv.getAmount()) / totalAmount;
                targetInv.setUnitPrice(weightedPrice);
            }
        } else if (sourceInv.getUnitPrice() != null) {
            // Si el target no tiene precio pero el source sí, usar el del source
            targetInv.setUnitPrice(sourceInv.getUnitPrice());
            targetInv.setCurrency(sourceInv.getCurrency());
        }
    }

    /**
     * Actualiza las compras para que apunten al producto fusionado
     */
    private void mergeBuys(Product sourceProduct, Product targetProduct) {
        List<Buy> sourceBuys = buyService.getBuysByBuyNameAndClient(sourceProduct.getProductName(), client);

        for (Buy buy : sourceBuys) {
            buy.setBuyName(targetProduct.getProductName());
            buyService.save(buy);
        }
    }

    /**
     * Renombra un producto simplemente cambiando su nombre
     */
    private void renameProduct(Product product, String newName) {
        String oldName = product.getProductName();

        // Actualizar el nombre del producto
        product.setProductName(newName);
        productService.save(product);

        // Actualizar las compras asociadas con el nuevo nombre
        List<Buy> buys = buyService.getBuysByBuyNameAndClient(oldName, client);
        for (Buy buy : buys) {
            buy.setBuyName(newName);
            buyService.save(buy);
        }
    }

    /**
     * Convierte un precio a CUP usando el servicio Currency
     */
    private double convertToCUP(Double amount, String currencyName) {
        if (amount == null) return 0.0;

        // Si ya es CUP, no hay conversión
        if ("CUP".equalsIgnoreCase(currencyName) || currencyName == null || currencyName.trim().isEmpty()) {
            return amount;
        }

        try {
            // Obtener la moneda desde el servicio
            Currency currency = currencyService.getCurrencyByName(currencyName);
            if (currency != null && currency.getCurrencyPriceInCUP() != null) {
                return amount * currency.getCurrencyPriceInCUP();
            }

            // Si no se encuentra la moneda, usar 1 como fallback
            return amount;

        } catch (Exception e) {
            // En caso de error, retornar el amount sin convertir
            return amount;
        }
    }

    private void clearFields() {
        tfNewName.clear();
        tfActualName.clear();
    }

    /**
     * Closes the change product name window.
     * @param actionEvent the action event
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfActualName.getScene().getWindow();
        stage.close();
    }

    /**
     * Validates all required fields before updating the product name.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateAllFields() {
        return validateTfActualName() && validateTfNewName();
    }

    /**
     * Validates the new product name field.
     */
    private boolean validateTfNewName() {
        String newProductName = tfNewName.getText();
        if (newProductName == null || newProductName.isEmpty()) {
            displayAlerts.showAlert("Debe escribir el nuevo nombre");
            return false;
        }

        return true;
    }

    /**
     * Validates the actual product name field.
     */
    private boolean validateTfActualName() {
        String actualProductName = tfActualName.getText();
        if (actualProductName == null || actualProductName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un producto de origen");
            return false;
        }

        Product product = productService.getByProductNameAndClient(actualProductName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto de origen no existe");
            return false;
        }

        return true;
    }

    /**
     * Initializes the product menu with all products for the current client.
     */
    private void initMbActualName() {
        mbActualName.getItems().clear();
        List<Product> products = productService.getAllProductsByClient(client);
        for (Product p : products) {
            MenuItem item = new MenuItem(p.getProductName());
            item.setOnAction(e -> tfActualName.setText(item.getText()));
            mbActualName.getItems().add(item);
        }
    }
}