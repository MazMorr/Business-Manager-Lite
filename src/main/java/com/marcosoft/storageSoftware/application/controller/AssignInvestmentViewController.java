package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
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
public class AssignInvestmentViewController {

    private final InventoryServiceImpl inventoryService;
    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final WarehouseServiceImpl warehouseService;
    private final InvestmentServiceImpl investmentService;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final ParseDataTypes parseDataTypes;

    @Lazy
    public AssignInvestmentViewController(ParseDataTypes parseDataTypes, DisplayAlerts displayAlerts, InventoryServiceImpl inventoryService, UserLogged userLogged, WarehouseServiceImpl warehouseService, ClientServiceImpl clientService, InvestmentServiceImpl investmentService, ProductServiceImpl productService) {
        this.productService = productService;
        this.displayAlerts = displayAlerts;
        this.inventoryService = inventoryService;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
        this.investmentService = investmentService;
        this.clientService = clientService;
        this.parseDataTypes = parseDataTypes;
    }

    @FXML
    private MenuButton mbWarehouse, mbInvestment;
    @FXML
    private TextField tfWarehouse, tfProduct, tfInvestment, tfAmount;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initMbWarehouse();
            initMbInvestment();
        });
    }

    @FXML
    public void assignAllProductAmount(ActionEvent actionEvent) {
        if (tfInvestment.getText().isEmpty()) {
            displayAlerts.showAlert("Debe asignar una inversión primero");
        } else if (!investmentService.existsByInvestmentId(Long.parseLong(tfInvestment.getText()))) {
            displayAlerts.showAlert("No se encontró el identificador de la inversión en la base de datos");
        } else if (investmentService.getInvestmentById(parseDataTypes.parseLong(tfInvestment.getText())).getAmount() == 0) {
            displayAlerts.showAlert("Esta inversión ha sido completamente asignada, debe seleccionar otra o reasignar los productos de esta");
        } else {
            tfAmount.setText(String.valueOf(investmentService.getInvestmentById(
                    Long.parseLong(tfInvestment.getText())).getAmount())
            );
        }
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void assignProduct(ActionEvent actionEvent) {
        try {
            if (tfInvestment.getText().isEmpty() || tfWarehouse.getText().isEmpty() || tfAmount.getText().isEmpty()) {
                displayAlerts.showAlert("Todos los campos son obligatorios");
                return;
            }

            long investmentId = Long.parseLong(tfInvestment.getText());
            int amountToAssign = Integer.parseInt(tfAmount.getText());

            Investment investment = investmentService.getInvestmentById(investmentId);

            if (investment == null) {
                displayAlerts.showAlert("No se encontró la inversión en la base de datos");
            } else if (investment.getAmount() == 0) {
                displayAlerts.showAlert("Esta inversión ha sido completamente asignada");
            } else if (amountToAssign > investment.getAmount()) {
                displayAlerts.showAlert("La cantidad excede el monto de la inversión");
            } else if (!warehouseExistsForClient()) {
                displayAlerts.showAlert("No se encuentra el Almacén especificado");
            } else {

                Client client = clientService.getClientByName(userLogged.getName());
                Product product = productService.getByProductNameAndClient(
                        tfProduct.getText(),
                        client
                );
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                        tfWarehouse.getText(),
                        client
                );

                Inventory inventory = new Inventory(
                        investmentId,
                        product,
                        client,
                        warehouse,
                        amountToAssign
                );

                inventoryService.save(inventory);
                int actualInvestmentAmount = investmentService.getInvestmentById(investmentId).getAmount() - amountToAssign;
                investmentService.getInvestmentById(investmentId).setAmount(actualInvestmentAmount);
                displayAlerts.showAlert("Producto asignado exitosamente");
            }
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("Los campos numéricos deben contener valores válidos");
        } catch (Exception e) {
            displayAlerts.showAlert("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    // ============================
    // UTILITIES
    // ============================
    private boolean warehouseExistsForClient() {
        Client client = clientService.getClientByName(userLogged.getName());
        return warehouseService.existsByWarehouseNameAndClient(
                tfWarehouse.getText(),
                client
        );
    }

    private void initMbInvestment() {
        mbInvestment.getItems().clear();
        List<Investment> investments = investmentService.getNonZeroInvestmentsByClient(clientService.getClientByName(userLogged.getName()));

        for (Investment i : investments) {
            MenuItem item = new MenuItem(String.valueOf(i.getInvestmentId()));
            item.setOnAction(e -> {
                tfInvestment.setText(item.getText());
                tfProduct.setText(i.getProductName());
            });
            mbInvestment.getItems().add(item);
        }
    }

    private void initMbWarehouse() {
        mbWarehouse.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getWarehousesByClient(clientService.getClientByName(userLogged.getName()));

        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e_ -> {
                tfWarehouse.setText(item.getText());
            });
            mbWarehouse.getItems().add(item);
        }
    }
}
