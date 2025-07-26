package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class AddWarehouseViewController {

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final WarehouseServiceImpl warehouseService;

    @Lazy
    public AddWarehouseViewController(WarehouseServiceImpl warehouseService, UserLogged userLogged,ClientServiceImpl clientService){
        this.warehouseService= warehouseService;
        this.clientService = clientService;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField tfWarehouseName;
    @FXML
    public void addWarehouse(ActionEvent actionEvent) {
        if (tfWarehouseName.getText().isEmpty()) {
            showAlert("Debe asignar un nombre para el nuevo almacén");
        } else if (tfWarehouseName.getText().length() > 18) {
            showAlert("El nuevo nombre no puede exceder los 18 carácteres incluyendo espacios");
        } else {
            try {
                Warehouse warehouse = new Warehouse(
                        null,
                        tfWarehouseName.getText(),
                        clientService.getClientByName(userLogged.getName())
                );
                warehouseService.save(warehouse);

                showAlert("El nuevo almacén ha sido añadido correctamente");
            } catch (Exception e) {
                showAlert("Ha ocurrido un error: " + e.getMessage());
            }
        }
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfWarehouseName.getScene().getWindow();
        stage.close();
    }

    // ============================
    // UTILITIES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
