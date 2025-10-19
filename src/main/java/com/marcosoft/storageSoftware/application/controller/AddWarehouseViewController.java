package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class AddWarehouseViewController {
    private Client client;

    private final WarehouseViewController warehouseViewController;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final WarehouseServiceImpl warehouseService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final InventoryServiceImpl inventoryService;
    private final SceneSwitcher sceneSwitcher;

    @FXML
    private TextField tfWarehouseName;

    @FXML
    private void initialize() {
        client = userLogged.getClient();
    }

    @FXML
    public void addWarehouse() {
        LocalDateTime registryMoment = LocalDateTime.now();
        if (tfWarehouseName.getText().isEmpty()) {
            displayAlerts.showAlert("Debe asignar un nombre para el nuevo almacén");
        } else if (tfWarehouseName.getText().length() > 18) {
            displayAlerts.showAlert("El nuevo nombre no puede exceder los 18 carácteres incluyendo espacios");
        } else if (warehouseService.existsByWarehouseNameAndClient(tfWarehouseName.getText(), client)) {
            displayAlerts.showAlert("Ya existe un almacén con ese nombre");
        } else {
            try {
                Warehouse warehouse = new Warehouse(
                        null,
                        tfWarehouseName.getText(),
                        client
                );
                warehouseService.save(warehouse);

                // ELIMINAR la creación automática de Inventory - ya no es necesaria
                // Un almacén vacío no necesita un registro de Inventory
                // El Inventory se creará cuando se asignen productos al almacén
                Inventory inventory = new Inventory(null, null, client, warehouse, null,
                        null, null, null, null, null
                );
                inventoryService.save(inventory);


                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacén",
                        "Adición Almacén: " + warehouse.getWarehouseName(), registryMoment
                );
                generalRegistryService.save(generalRegistry);

                WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                        null, client, "Adición", registryMoment,
                        warehouse.getWarehouseName(), null, null
                );
                warehouseRegistryService.save(warehouseRegistry);

                warehouseViewController.initializeTreeTable();

                // Cerrar la ventana después de crear exitosamente
                Stage stage = (Stage) tfWarehouseName.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
            }
        }
    }

    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfWarehouseName);
    }
}