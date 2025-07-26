package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;

@Lazy
@Controller
public class UpdateWarehouseViewController {

    private final WarehouseServiceImpl warehouseService;
    private final ClientServiceImpl clientService;
    private final UserLogged userLogged;

    @Lazy
    public UpdateWarehouseViewController(UserLogged userLogged, ClientServiceImpl clientService, WarehouseServiceImpl warehouseService) {
        this.warehouseService = warehouseService;
        this.clientService = clientService;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField tfActualName, tfNewName;
    @FXML
    private MenuButton mbActualName;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initMbActualName();
        });

    }

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

    @FXML
    public void updateWarehouseName(ActionEvent actionEvent) {
        if(validateTfActualName() && validateTfNewName()){
            String newName= tfNewName.getText();
            String actualName = tfActualName.getText();
            Client client = clientService.getClientByName(userLogged.getName());
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(actualName, client);
            warehouse.setWarehouseName(newName);
            showAlert("Nombre actualizado satisfactoriamente");
            cleanFields();
        }
    }

    private void cleanFields(){
        tfActualName.clear();
        tfNewName.clear();
    }

    private boolean validateTfNewName() {
        String newName= tfNewName.getText();
        Client client = clientService.getClientByName(userLogged.getName());
        if(newName.isEmpty()){
            showAlert("No ha establecido el nuevo nombre que desea para su almacén");
            return false;
        } else if (tfActualName.getText().equals(newName)) {
            showAlert("Está introduciendo 2 veces el mismo nombre");
            return false;
        } else if (warehouseService.existsByWarehouseNameAndClient(newName, client)) {
            showAlert("Ya existe este nombre en otro almacén");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateTfActualName() {
        String actualName = tfActualName.getText();
        Client client = clientService.getClientByName(userLogged.getName());
        if (actualName.isEmpty()) {
            showAlert("No ha seleccionado ningún almacén al que actualizarle el nombre");
            return false;
        } else if (!warehouseService.existsByWarehouseNameAndClient(actualName, client)) {
            showAlert("El almacén al que desea cambiarle el nombre no existe");
            return false;
        } else {
            return true;
        }
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) mbActualName.getScene().getWindow();
        stage.close();
    }

    // ============================
    // UTILIDADES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
