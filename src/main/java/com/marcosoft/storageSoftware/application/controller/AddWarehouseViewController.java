package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class AddWarehouseViewController {

    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final ClientServiceImpl clientService;
    private final WarehouseServiceImpl warehouseService;

    @Lazy
    public AddWarehouseViewController(DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, UserLogged userLogged,ClientServiceImpl clientService){
        this.warehouseService= warehouseService;
        this.clientService = clientService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField tfWarehouseName;
    @FXML
    public void addWarehouse(ActionEvent actionEvent) {
        if (tfWarehouseName.getText().isEmpty()) {
            displayAlerts.showAlert("Debe asignar un nombre para el nuevo almacén");
        } else if (tfWarehouseName.getText().length() > 18) {
            displayAlerts.showAlert("El nuevo nombre no puede exceder los 18 carácteres incluyendo espacios");
        } else {
            try {
                Warehouse warehouse = new Warehouse(
                        null,
                        tfWarehouseName.getText(),
                        clientService.getClientByName(userLogged.getName())
                );
                warehouseService.save(warehouse);

                displayAlerts.showAlert("El nuevo almacén ha sido añadido correctamente");
            } catch (Exception e) {
                displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
            }
        }
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfWarehouseName.getScene().getWindow();
        stage.close();
    }

}
