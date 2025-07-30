package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Controller for the add warehouse view.
 * Handles logic for creating a new warehouse for the current client.
 */
@Lazy
@Controller
public class AddWarehouseViewController {
    private Client client;

    // Dependencies injected via constructor
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final ClientServiceImpl clientService;
    private final WarehouseServiceImpl warehouseService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;

    /**
     * Constructor for dependency injection.
     */
    @Lazy
    public AddWarehouseViewController(WarehouseRegistryServiceImpl warehouseRegistryService, GeneralRegistryServiceImpl generalRegistryService, DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, UserLogged userLogged, ClientServiceImpl clientService) {
        this.warehouseService = warehouseService;
        this.generalRegistryService = generalRegistryService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.clientService = clientService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    // FXML UI component for warehouse name input
    @FXML
    private TextField tfWarehouseName;

    @FXML
    private void initialize() {
        client = clientService.getClientByName(userLogged.getName());
    }


    /**
     * Handles the creation of a new warehouse when the user clicks the add button.
     * Validates input and shows alerts in Spanish if validation fails.
     */
    @FXML
    public void addWarehouse(ActionEvent actionEvent) {
        LocalDateTime registryMoment = LocalDateTime.now();
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

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacenes", "Adición Almacén", registryMoment
                );
                generalRegistryService.save(generalRegistry);

                WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                        null, "Adición", registryMoment, warehouse, null, client, null
                );
                warehouseRegistryService.save(warehouseRegistry);

                displayAlerts.showAlert("El nuevo almacén ha sido añadido correctamente");
            } catch (Exception e) {
                displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the add warehouse window.
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfWarehouseName.getScene().getWindow();
        stage.close();
    }

}
