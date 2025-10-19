package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the update warehouse view.
 * Handles logic for updating the name of a warehouse for the current client.
 */
@RequiredArgsConstructor
@Controller
public class RenameWarehouseViewController {
    private Client client;

    // Service and utility dependencies
    private final WarehouseServiceImpl warehouseService;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseViewController warehouseViewController;
    private final SceneSwitcher sceneSwitcher;

    // FXML UI components
    @FXML
    private TextField tfActualName, tfNewName;
    @FXML
    private MenuButton mbActualName;

    @FXML
    public void initialize() {
        client = userLogged.getClient();
        Platform.runLater(this::initMbActualName);
    }

    private void initMbActualName() {
        mbActualName.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);
        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e -> {
                tfActualName.setText(item.getText());
            });
            mbActualName.getItems().add(item);
        }
    }

    @FXML
    public void updateWarehouseName() {
        try {
            if (validateTfActualName() && validateTfNewName()) {
                String newName = tfNewName.getText().trim(); // Elimina espacios en blanco
                String actualName = tfActualName.getText().trim();

                if (client == null) {
                    displayAlerts.showError("Cliente no encontrado");
                    return;
                }

                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(actualName, client);
                if (warehouse == null) {
                    displayAlerts.showAlert("Almacén no encontrado");
                    return;
                }

                warehouse.setWarehouseName(newName);
                warehouseService.save(warehouse);

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacén", "Cambio Nombre Almacén", LocalDateTime.now()
                );
                generalRegistryService.save(generalRegistry);

                displayAlerts.showAlert("Nombre actualizado satisfactoriamente");
                cleanFields();
                warehouseViewController.initializeTreeTable();
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al actualizar el nombre: " + e.getMessage());
        }
    }

    private void cleanFields() {
        tfActualName.clear();
        tfNewName.clear();
    }

    private boolean validateTfNewName() {
        String newName = tfNewName.getText();
        if (newName.isEmpty()) {
            displayAlerts.showAlert("No ha establecido el nuevo nombre que desea para su almacén");
            return false;
        } else if (tfActualName.getText().equals(newName)) {
            displayAlerts.showAlert("Está introduciendo 2 veces el mismo nombre");
            return false;
        } else if (warehouseService.existsByWarehouseNameAndClient(newName, client)) {
            displayAlerts.showAlert("Ya existe este nombre en otro almacén");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Validates the actual warehouse name field.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfActualName() {
        String actualName = tfActualName.getText();
        if (actualName.isEmpty()) {
            displayAlerts.showAlert("No ha seleccionado ningún almacén al que actualizarle el nombre");
            return false;
        } else if (!warehouseService.existsByWarehouseNameAndClient(actualName, client)) {
            displayAlerts.showAlert("El almacén al que desea cambiarle el nombre no existe");
            return false;
        } else {
            return true;
        }
    }

    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfActualName);
    }
}
