package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
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

import java.util.List;

@Lazy
@Controller
public class ChangeProductNameViewController {
    private Client client;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;

    @Lazy
    public ChangeProductNameViewController(DisplayAlerts displayAlerts, UserLogged userLogged, ClientServiceImpl clientService, ProductServiceImpl productService) {
        this.productService = productService;
        this.displayAlerts = displayAlerts;
        this.clientService = clientService;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField tfActualName, tfNewName;
    @FXML
    private MenuButton mbActualName;

    @FXML
    private void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(this::initMbActualName);
    }

    @FXML
    public void updateProductName(ActionEvent actionEvent) {
        if (!validateAllFields()) {
            return;
        }

        try {
            String productName = tfActualName.getText();
            String newName = tfNewName.getText();
            Product product = productService.getByProductNameAndClient(productName, client);
            product.setProductName(newName);
            productService.save(product);
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
        }

    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfActualName.getScene().getWindow();
        stage.close();
    }

    // ============================
    // UTILITIES
    // ============================
    private boolean validateAllFields() {
        return validateTfActualName() && validateTfNewName();
    }

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

    private void initMbActualName() {
        mbActualName.getItems().clear();
        List<Product> products = productService.getAllProductsByClient(client);
        for (Product p : products) {
            MenuItem item = new MenuItem(p.getProductName());
            item.setOnAction(e -> {
                tfActualName.getText();
            });
            mbActualName.getItems().add(item);
        }
    }
}
