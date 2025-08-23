package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseWarehouseDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseDataTable;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Expense;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final DisplayAlerts displayAlerts;
    private final InventoryServiceImpl inventoryService;
    private final ExpenseServiceImpl expenseService;

    /**
     * Constructor for dependency injection.
     * @param expenseService the investment service
     * @param displayAlerts the display alerts
     * @param warehouseService the warehouse service
     * @param userLogged the user logged
     * @param sceneSwitcher the scene switcher
     * @param inventoryService the inventory service
     */
    public WarehouseViewController(
            ExpenseServiceImpl expenseService, DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, InventoryServiceImpl inventoryService
    ) {
        this.expenseService = expenseService;
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
    }

    // FXML UI components
    @FXML
    private TableView<ExpenseWarehouseDataTable> tvExpenses;
    @FXML
    private TreeTableView<WarehouseDataTable> ttvWarehouse;
    @FXML
    private TreeTableColumn<WarehouseDataTable, String> ttcWarehouseName, ttcProductName;
    @FXML
    private TreeTableColumn<WarehouseDataTable, Integer> ttcProductAmount;


    @FXML
    private TableColumn<ExpenseWarehouseDataTable, Long> tcIdExpense;
    @FXML
    private TableColumn<ExpenseWarehouseDataTable, String> tcProductName;
    @FXML
    private TableColumn<ExpenseWarehouseDataTable, Integer> tcProductAmount;
    @FXML
    private TableColumn<ExpenseWarehouseDataTable, LocalDate> tcProductDate;

    @FXML
    private Label lblClientName;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table labels and loads warehouse data.
     */
    @FXML
    public void initialize() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        Platform.runLater(() -> {
            initTableValues();
            initTreeTable();
            initTableLabels();
        });
    }

    /**
     * Loads investment data into the investments table.
     * Populates the tvExpenses TableView with unassigned investments for the current client.
     */
    public void initTableValues() {
        // Clear previous data
        tvExpenses.getItems().clear();

        // Get all expenses for the client with amount > 0 (not fully assigned)
        List<Expense> expenses = expenseService.getAllProductExpensesGreaterThanZeroByClient(client)
                .stream().toList();

        // Map expenses to ExpenseWarehouseDataTable
        List<ExpenseWarehouseDataTable> investmentData = expenses.stream()
                .map(inv -> new ExpenseWarehouseDataTable(
                        inv.getExpenseId(),
                        inv.getExpenseName(),
                        inv.getLeftAmount(),
                        inv.getReceivedDate()
                ))
                .toList();

        // Set up columns if not already set (optional, for safety)
        tcIdExpense.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        tcProductName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcProductAmount.setCellValueFactory(new PropertyValueFactory<>("productAmount"));
        tcProductDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));

        // Add data to the table
        tvExpenses.getItems().addAll(investmentData);
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

        // Placeholder for TableView (tvExpenses)
        tvExpenses.setPlaceholder(new Label("""
                💼 Aquí aparecerán tus gastos
                Cuando registres GASTOS de tipo PRODUCTO
                y estos no hayan sido asignados, se mostrarán aquí"""));

        // Common style for both placeholders
        String commonStyle = "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 14px; " +
                "-fx-text-alignment: center; " +
                "-fx-text-fill: #64748b; " +
                "-fx-alignment: center; " +
                "-fx-padding: 10px; ";

        ttvWarehouse.getPlaceholder().setStyle(commonStyle);
        tvExpenses.getPlaceholder().setStyle(commonStyle);
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

        List<Inventory> inventory = inventoryService.getAllInventoriesByClient(client);

        // Group inventories by warehouse
        Map<Warehouse, List<Inventory>> inventoriesByWarehouse = inventory.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouse));

        TreeItem<WarehouseDataTable> root = new TreeItem<>();

        inventoriesByWarehouse.forEach((warehouse, inventories) -> {
            // Manejo de amount null en el cálculo del total
            int total = inventories.stream()
                    .mapToInt(inv -> inv.getAmount() != null ? inv.getAmount() : 0)
                    .sum();

            int invCount = inventories.size() - 1;

            WarehouseDataTable warehouseNode = new WarehouseDataTable(
                    warehouse.getWarehouseName(),
                    "Productos: " + invCount,
                    total
            );

            TreeItem<WarehouseDataTable> warehouseItem = new TreeItem<>(warehouseNode);

            // Children (products)

            inventories.forEach(inv -> {
                if (!(inv.getProduct() == null || inv.getAmount() == null)) {
                    WarehouseDataTable productNode = new WarehouseDataTable(
                            "",
                            inv.getProduct().getProductName(),
                            inv.getAmount()
                    );
                    warehouseItem.getChildren().add(new TreeItem<>(productNode));
                }
            });

            root.getChildren().add(warehouseItem);
        });

        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false); // Enables placeholder display
    }

    /**
     * Opens the reassign product view in a new window.
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void reassignProduct() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Sistema de cuentas", "/images/RTS_logo.png", "/views/reassignProductView.fxml");
    }

    /**
     * Opens the add warehouse view in a new window.
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void addWarehouse() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Añadir Almacén", "/images/RTS_logo.png", "/views/addWarehouseView.fxml");
    }


    /**
     * Deletes the selected warehouse and all its products after confirmation.
     * Shows confirmation alert in Spanish.
     */
    @FXML
    public void deleteWarehouse() {
        WarehouseDataTable w = ttvWarehouse.getSelectionModel().getSelectedItem().getValue();
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(w.getWarehouseName(), client);
        if (displayAlerts.showConfirmationAlert("Está seguro de querer eliminar el almacén seleccionado:\n" +
                w.getWarehouseName() + " junto a todos los productos almacenados en él?")) {
            List<Inventory> inv = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);
            for (Inventory i : inv) {
                inventoryService.deleteInventoryById(i.getId());
            }
            warehouseService.deleteWarehouseById(warehouse.getId());
            ttvWarehouse.getSelectionModel().clearSelection();
            initTreeTable();
        } else {
            ttvWarehouse.getSelectionModel().clearSelection();
        }
    }

    /**
     * Opens the assign investment view in a new window.
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void assignExpense() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Asignar Inversión", "/images/lc_logo.png", "/views/assignExpenseView.fxml");
    }

    /**
     * Shows a placeholder alert for checking investments (feature coming soon).
     */
    @FXML
    public void checkExpense() {
        displayAlerts.showAlert("Próximamente");
    }

    /**
     * Opens the update warehouse view in a new window.
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void updateWarehouse() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Actualizar Almacén", "/images/lc_logo.png", "/views/updateWarehouseView.fxml");
    }

    /**
     * Opens the change product name view in a new window.
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void changeProductName() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Asignar Inversión a un Almacén", "/images/lc_logo.png", "/views/changeProductNameView.fxml");
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================

    /**
     * Navigates to the configuration view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    /**
     * Navigates to the support view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    /**
     * Navigates to the registry view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    /**
     * Navigates to the balance view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    /**
     * Navigates to the investment view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    /**
     * Navigates to the sell view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }
}
