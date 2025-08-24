package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
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
public class ChangeProductNameViewController {
    private Client client;

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseViewController warehouseViewController;

    /**
     * Constructor for dependency injection.
     * @param warehouseViewController the warehouse view controller
     * @param generalRegistryService the general registry service
     * @param displayAlerts the display alerts
     * @param userLogged the user logged
     * @param productService the product service
     */
    @Lazy
    public ChangeProductNameViewController(
            WarehouseViewController warehouseViewController, GeneralRegistryServiceImpl generalRegistryService,
            DisplayAlerts displayAlerts, UserLogged userLogged, ProductServiceImpl productService
    ) {
        this.productService = productService;
        this.warehouseViewController = warehouseViewController;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
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
            String productName = tfActualName.getText();
            String newName = tfNewName.getText();
            Product product = productService.getByProductNameAndClient(productName, client);
            product.setProductName(newName);
            productService.save(product);

            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client,"Almacenes", "Cambio Nombre Producto", LocalDateTime.now()
            );
            generalRegistryService.save(generalRegistry);

            displayAlerts.showAlert("El nuevo nombre ha sido correctamente asignado");
            clearFields();
            warehouseViewController.initTableValues();
            warehouseViewController.initTreeTable();
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
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

    // ============================
    // UTILITIES
    // ============================

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

        if (productService.existsByProductNameAndClient(newProductName, client)) {
            displayAlerts.showAlert("El nuevo nombre ya está asignado a otro producto");
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
