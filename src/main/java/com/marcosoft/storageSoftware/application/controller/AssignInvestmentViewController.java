package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the assign investment view.
 * Handles logic for assigning investments to warehouses and products.
 */
@Lazy
@Controller
public class AssignInvestmentViewController {
    private Client client;

    // Service and utility dependencies
    private final InventoryServiceImpl inventoryService;
    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final WarehouseServiceImpl warehouseService;
    private final InvestmentServiceImpl investmentService;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final WarehouseViewController warehouseViewController;

    /**
     * Constructor for dependency injection.
     * @param generalRegistryService the general registry service
     * @param parseDataTypes the parse data types
     * @param displayAlerts the display alerts
     * @param inventoryService the inventory service
     * @param userLogged the user logged
     * @param warehouseService the warehouse service
     * @param clientService the client service
     * @param investmentService the investment service
     * @param productService the product service
     * @param warehouseRegistryService the warehouse registry service
     * @param warehouseViewController the warehouse view controller
     */
    public AssignInvestmentViewController(
            GeneralRegistryServiceImpl generalRegistryService, ParseDataTypes parseDataTypes, DisplayAlerts displayAlerts,
            InventoryServiceImpl inventoryService, UserLogged userLogged, WarehouseServiceImpl warehouseService,
            ClientServiceImpl clientService, InvestmentServiceImpl investmentService, ProductServiceImpl productService,
            WarehouseRegistryServiceImpl warehouseRegistryService, WarehouseViewController warehouseViewController
    ) {
        this.productService = productService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.inventoryService = inventoryService;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
        this.investmentService = investmentService;
        this.clientService = clientService;
        this.parseDataTypes = parseDataTypes;
        this.warehouseViewController = warehouseViewController;
    }

    // FXML UI components
    @FXML
    private MenuButton mbWarehouse, mbInvestment;
    @FXML
    private TextField tfWarehouse, tfProduct, tfInvestment, tfAmount;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads warehouse and investment menus.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            initMbWarehouse();
            initMbInvestment();
        });
    }

    /**
     * Assigns all available product amount from the selected investment.
     * Shows alerts in Spanish if validation fails.
     * @param actionEvent the action event
     */
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
                    Long.parseLong(tfInvestment.getText())).getLeftAmount())
            );
        }
    }

    /**
     * Closes the assign investment window.
     * @param actionEvent the action event
     */
    @FXML
    public void goOut(ActionEvent actionEvent) {
        Stage stage = (Stage) tfAmount.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the assignment of a product from an investment to a warehouse.
     * Validates fields, updates inventory and investment, and shows alerts in Spanish.
     * @param actionEvent the action event
     */
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

                Product product = productService.getByProductNameAndClient(
                        tfProduct.getText(),
                        client
                );
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                        tfWarehouse.getText(),
                        client
                );

                Inventory inventory;
                if(inventoryService.existsByProductAndWarehouseAndClient(product,warehouse,client)){
                    inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
                    inventory.setAmount(inventory.getAmount() + amountToAssign);
                }else{
                    inventory = new Inventory(
                            null,
                            product,
                            client,
                            warehouse,
                            amountToAssign,
                            null,
                            null
                    );
                }

                inventoryService.save(inventory);

                int actualInvestmentAmount = investment.getLeftAmount() - amountToAssign;
                investment.setLeftAmount(actualInvestmentAmount);
                investmentService.save(investment);


                LocalDateTime registryMoment = LocalDateTime.now();
                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacenes", "Asignación de Productos", registryMoment
                );
                generalRegistryService.save(generalRegistry);

                WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                        null, client, "Asignación", registryMoment, warehouse.getWarehouseName(), product.getProductName(), amountToAssign
                );
                warehouseRegistryService.save(warehouseRegistry);

                warehouseViewController.initTreeTable();
                warehouseViewController.initTableValues();
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

    /**
     * Checks if the warehouse exists for the current client.
     */
    private boolean warehouseExistsForClient() {
        Client client = clientService.getClientByName(userLogged.getName());
        return warehouseService.existsByWarehouseNameAndClient(
                tfWarehouse.getText(),
                client
        );
    }

    /**
     * Initializes the investment menu with investments that have remaining amount.
     */
    private void initMbInvestment() {
        mbInvestment.getItems().clear();
        List<Investment> investments = investmentService.getAllProductInvestmentsGreaterThanZeroByClient(client);

        for (Investment i : investments) {
            MenuItem item = new MenuItem(String.valueOf(i.getInvestmentId()));
            item.setOnAction(e -> {
                tfInvestment.setText(item.getText());
                tfProduct.setText(i.getInvestmentName());
            });
            mbInvestment.getItems().add(item);
        }
    }

    /**
     * Initializes the warehouse menu with all warehouses for the current client.
     */
    private void initMbWarehouse() {
        mbWarehouse.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getWarehousesByClient(client);

        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e_ -> {
                tfWarehouse.setText(item.getText());
            });
            mbWarehouse.getItems().add(item);
        }
    }
}
