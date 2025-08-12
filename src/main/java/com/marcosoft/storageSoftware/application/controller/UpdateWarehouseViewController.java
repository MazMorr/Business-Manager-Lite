package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
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
 * Controller for the update warehouse view.
 * Handles logic for updating the name of a warehouse for the current client.
 */
@Lazy
@Controller
public class UpdateWarehouseViewController {
    private Client client;

    // Service and utility dependencies
    private final WarehouseServiceImpl warehouseService;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseViewController warehouseViewController;

    /**
     * Constructor for dependency injection.
     * @param userLogged the user logged
     * @param displayAlerts the display alerts
     * @param clientService the client service
     * @param warehouseService the warehouse service
     * @param generalRegistryService the general registry service
     * @param warehouseViewController the warehouse view controller
     */
    @Lazy
    public UpdateWarehouseViewController(
            UserLogged userLogged, DisplayAlerts displayAlerts, ClientServiceImpl clientService,
            WarehouseServiceImpl warehouseService, GeneralRegistryServiceImpl generalRegistryService,
            WarehouseViewController warehouseViewController
    ) {
        this.warehouseService = warehouseService;
        this.warehouseViewController = warehouseViewController;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.clientService = clientService;
        this.userLogged = userLogged;
    }

    // FXML UI components
    @FXML
    private TextField tfActualName, tfNewName;
    @FXML
    private MenuButton mbActualName;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads warehouse menu for the current client.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            initMbActualName();
        });
    }

    /**
     * Initializes the warehouse menu with all warehouses for the current client.
     */
    private void initMbActualName() {
        mbActualName.getItems().clear();
        Client client = clientService.getClientByName(userLogged.getName());
        List<Warehouse> warehouses = warehouseService.getAllWarehousesByClient(client);
        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e -> {
                tfActualName.setText(item.getText());
            });
            mbActualName.getItems().add(item);
        }
    }

    /**
     * Handles the update of a warehouse name when the user clicks the update button.
     * Validates input and shows alerts in Spanish if validation fails.
     * @param actionEvent the action event
     */
    @FXML
    public void updateWarehouseName(ActionEvent actionEvent) {
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
                warehouseViewController.initTreeTable();
            }
        } catch (Exception e) {
            displayAlerts.showError("Error al actualizar el nombre: " + e.getMessage());
        }
    }

    /**
     * Clears all input fields in the form.
     */
    private void cleanFields() {
        tfActualName.clear();
        tfNewName.clear();
    }

    /**
     * Validates the new warehouse name field.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfNewName() {
        String newName = tfNewName.getText();
        Client client = clientService.getClientByName(userLogged.getName());
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
        Client client = clientService.getClientByName(userLogged.getName());
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

    /**
     * Closes the update warehouse window.
     * @param actionEvent the action event
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) mbActualName.getScene().getWindow();
        stage.close();
    }
}
