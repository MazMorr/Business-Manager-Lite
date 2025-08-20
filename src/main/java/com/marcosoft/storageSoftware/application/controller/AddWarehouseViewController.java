package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
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
    private final WarehouseViewController warehouseViewController;

    // Dependencies injected via constructor
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final WarehouseServiceImpl warehouseService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final InventoryServiceImpl inventoryService;

    public AddWarehouseViewController(
            InventoryServiceImpl inventoryService, WarehouseViewController warehouseViewController,
            WarehouseRegistryServiceImpl warehouseRegistryService, GeneralRegistryServiceImpl generalRegistryService,
            DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, UserLogged userLogged
    ) {
        this.inventoryService = inventoryService;
        this.warehouseService = warehouseService;
        this.warehouseViewController = warehouseViewController;
        this.generalRegistryService = generalRegistryService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
    }

    // FXML UI component for warehouse name input
    @FXML
    private TextField tfWarehouseName;

    @FXML
    private void initialize() {
        client = userLogged.getClient();
    }


    /**
     * Handles the creation of a new warehouse when the user clicks the add button.
     * Validates input and shows alerts in Spanish if validation fails.
     */
    @FXML
    public void addWarehouse() {
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
                        client
                );
                warehouseService.save(warehouse);

                Inventory inventory = new Inventory(
                        null,
                        null,
                        client,
                        warehouse,
                        null,
                        null,
                        null
                );
                inventoryService.save(inventory);

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null,
                        client,
                        "Almacenes",
                        "Adición Almacén",
                        registryMoment
                );
                generalRegistryService.save(generalRegistry);

                WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                        null,
                        client,
                        "Adición",
                        registryMoment,
                        warehouse.getWarehouseName(),
                        null,
                        null
                );
                warehouseRegistryService.save(warehouseRegistry);

                displayAlerts.showAlert("El nuevo almacén ha sido añadido correctamente");
                warehouseViewController.initTreeTable();
            } catch (Exception e) {
                displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the add warehouse window.
     * @param actionEvent the action event
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfWarehouseName.getScene().getWindow();
        stage.close();
    }

}
