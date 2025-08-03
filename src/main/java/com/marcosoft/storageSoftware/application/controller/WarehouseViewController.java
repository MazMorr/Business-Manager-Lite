package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.application.dto.InvestmentWarehouseDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseDataTable;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller for the warehouse view.
 * Handles logic for displaying, managing, and navigating warehouses and their inventories.
 */
@Lazy
@Controller
public class WarehouseViewController {
    private Client client;

    // Service and utility dependencies
    private final WarehouseServiceImpl warehouseService;
    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final ParseDataTypes parseDataTypes;
    private final DisplayAlerts displayAlerts;
    private final InventoryServiceImpl inventoryService;
    private final ClientServiceImpl clientService;
    private final InvestmentServiceImpl investmentService;

    /**
     * Constructor for dependency injection.
     */
    public WarehouseViewController(
            InvestmentServiceImpl investmentService, DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, ParseDataTypes parseDataTypes,
            InventoryServiceImpl inventoryService, ClientServiceImpl clientService
    ) {
        this.clientService = clientService;
        this.investmentService = investmentService;
        this.inventoryService = inventoryService;
        this.parseDataTypes = parseDataTypes;
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
    }

    // FXML UI components
    @FXML
    private TableView<InvestmentWarehouseDataTable> tvInvestments;
    @FXML
    private TreeTableView<WarehouseDataTable> ttvWarehouse;
    @FXML
    private TreeTableColumn<WarehouseDataTable, String> ttcWarehouseName, ttcProductName;
    @FXML
    private TreeTableColumn<WarehouseDataTable, Integer> ttcProductAmount;


    @FXML
    private TableColumn<InvestmentWarehouseDataTable, Long> tcIdInvestment;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, String> tcProductName;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, Integer> tcProductAmount;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, LocalDate> tcProductDate;

    @FXML
    private Label txtClientName;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table labels and loads warehouse data.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            initTreeTable();
            initTableValues();
            initTableLabels();
        });
    }

    /**
     * Loads investment data into the investments table.
     * Populates the tvInvestments TableView with unassigned investments for the current client.
     */
    public void initTableValues() {
        // Clear previous data
        tvInvestments.getItems().clear();

        // Get all investments for the client with amount > 0 (not fully assigned)
        List<Investment> investments =
            investmentService.getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(client, "Producto").stream()
                .toList();

        // Map investments to InvestmentWarehouseDataTable
        List<InvestmentWarehouseDataTable> investmentData = investments.stream()
            .map(inv -> new InvestmentWarehouseDataTable(
                    inv.getInvestmentId(),
                    inv.getInvestmentName(),
                    inv.getAmount(),
                    inv.getReceivedDate()
            ))
            .toList();

        // Set up columns if not already set (optional, for safety)
        tcIdInvestment.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("investmentId"));
        tcProductName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("investmentName"));
        tcProductAmount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productAmount"));
        tcProductDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("investmentDate"));

        // Add data to the table
        tvInvestments.getItems().addAll(investmentData);
    }

    /**
     * Sets up placeholder labels for the warehouse and investment tables.
     */
    private void initTableLabels() {
        // Placeholder for TreeTableView (ttvWarehouse)
        ttvWarehouse.setPlaceholder(new Label("""
                📦 ¡Vaya! No hay almacenes registrados
                Añade tu primer almacén con el botón\s
                '(+) Agregar Almacén'"""));

        // Placeholder for TableView (tvInvestments)
        tvInvestments.setPlaceholder(new Label("""
                💼 Aquí aparecerán tus inversiones
                Cuando registres inversiones de tipo producto
                y estas no hayan sido asignadas, se mostrarán aquí"""));

        // Common style for both placeholders
        String commonStyle = "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 14px; " +
                "-fx-text-alignment: center; " +
                "-fx-text-fill: #64748b; " +
                "-fx-alignment: center; " +
                "-fx-padding: 10px; ";

        ttvWarehouse.getPlaceholder().setStyle(commonStyle);
        tvInvestments.getPlaceholder().setStyle(commonStyle);
    }

    /**
     * Loads warehouse and inventory data into the tree table.
     * Groups inventories by warehouse and displays products as children.
     */
    public void initTreeTable() {
        // Column configuration
        ttcWarehouseName.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));

        List<Inventory> inventory = inventoryService.getAllInventories();

        // Group inventories by warehouse
        Map<Warehouse, List<Inventory>> inventoriesByWarehouse = inventory.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouse));

        TreeItem<WarehouseDataTable> root = new TreeItem<>();

        inventoriesByWarehouse.forEach((warehouse, inventories) -> {
            int total = inventories.stream().mapToInt(Inventory::getAmount).sum();
            WarehouseDataTable warehouseNode = new WarehouseDataTable(
                    warehouse.getWarehouseName() + " (Total: " + total + ")",
                    "",
                    total
            );

            TreeItem<WarehouseDataTable> warehouseItem = new TreeItem<>(warehouseNode);

            // Children (products)
            inventories.forEach(inv -> {
                WarehouseDataTable productNode = new WarehouseDataTable(
                        "",
                        inv.getProduct().getProductName(),
                        inv.getAmount()
                );
                warehouseItem.getChildren().add(new TreeItem<>(productNode));
            });

            root.getChildren().add(warehouseItem);
        });

        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false); // Enables placeholder display
    }

    /**
     * Opens the reassign product view in a new window.
     */
    @FXML
    public void reassignProduct(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/reassignProductView.fxml"),
                "Sistema de cuentas",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.show();
    }

    /**
     * Opens the add warehouse view in a new window.
     */
    @FXML
    public void addWarehouse(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/addWarehouseView.fxml"),
                "Añadir Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    /**
     * Utility method to create and configure a new stage.
     */
    private Stage createStage(Parent root, String title, String iconPath) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString()));
        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }

    /**
     * Deletes the selected warehouse and all its products after confirmation.
     * Shows confirmation alert in Spanish.
     */
    @FXML
    public void deleteWarehouse(ActionEvent actionEvent) {
        WarehouseDataTable w = ttvWarehouse.getSelectionModel().getSelectedItem().getValue();
        Client c = clientService.getClientByName(userLogged.getName());
        if (displayAlerts.showConfirmationAlert("Está seguro de querer eliminar el almacén seleccionado:\n" +
                w.getWarehouseName() + " junto a todos los productos almacenados en él?")) {
            warehouseService.deleteWarehouseById(warehouseService.getWarehouseByWarehouseNameAndClient(w.getWarehouseName(), c).getId());
            ttvWarehouse.getSelectionModel().clearSelection();
        } else {
            ttvWarehouse.getSelectionModel().clearSelection();
        }
    }

    /**
     * Opens the assign investment view in a new window.
     */
    @FXML
    public void assignInvestment(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/assignInvestmentView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    /**
     * Shows a placeholder alert for checking investments (feature coming soon).
     */
    @FXML
    public void checkInvestment(ActionEvent actionEvent) {
        displayAlerts.showAlert("Próximamente");
    }

    /**
     * Opens the update warehouse view in a new window.
     */
    @FXML
    public void updateWarehouse(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/updateWarehouseView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    /**
     * Opens the change product name view in a new window.
     */
    @FXML
    public void changeProductName(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/changeProductNameView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================
    /**
     * Navigates to the configuration view.
     */
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/configurationView.fxml");
    }

    /**
     * Navigates to the support view.
     */
    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/supportView.fxml");
    }

    /**
     * Navigates to the registry view.
     */
    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/registryView.fxml");
    }

    /**
     * Navigates to the balance view.
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/balanceView.fxml");
    }

    /**
     * Navigates to the investment view.
     */
    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/investmentView.fxml");
    }

    /**
     * Navigates to the sell view.
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/sellView.fxml");
    }

}
