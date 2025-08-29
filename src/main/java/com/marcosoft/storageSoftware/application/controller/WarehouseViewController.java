package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseWarehouseDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseDataTable;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final ExpenseServiceImpl expenseService;
    private final ExpenseViewController expenseViewController;

    public WarehouseViewController(
            ExpenseServiceImpl expenseService, DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, InventoryServiceImpl inventoryService,
            GeneralRegistryServiceImpl generalRegistryService, ExpenseViewController expenseViewController
    ) {
        this.expenseService = expenseService;
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
        this.generalRegistryService = generalRegistryService;
        this.expenseViewController = expenseViewController;
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

    public void initTableValues() {
        // Clear previous data
        tvExpenses.getItems().clear();

        // Get all expenses for the client with amount > 0 (not fully assigned)
        List<Expense> expenses = expenseService.getAllProductExpensesGreaterThanZeroByClient(client).stream().toList();

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
            int total = inventories.stream().mapToInt(inv -> inv.getAmount() != null ? inv.getAmount() : 0).sum();

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

    @FXML
    public void reassignProduct() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Sistema de cuentas", "/images/lc_logo.png", "/views/reassignProductView.fxml");
    }

    @FXML
    public void addWarehouse() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Añadir Almacén", "/images/lc_logo.png", "/views/addWarehouseView.fxml");
    }


    @FXML
    public void deleteWarehouse() {
        try {
            WarehouseDataTable warehouseDataTable = ttvWarehouse.getSelectionModel().getSelectedItem().getValue();
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseDataTable.getWarehouseName(), client);
            if (displayAlerts.showConfirmationAlert("Está seguro de querer eliminar el almacén seleccionado: " +
                    warehouseDataTable.getWarehouseName() + " junto a todos los productos almacenados en él?")) {
                List<Inventory> inventories = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);
                for (Inventory inv : inventories) {
                    inventoryService.deleteInventoryById(inv.getId());
                }
                warehouseService.deleteWarehouseById(warehouse.getId());
                ttvWarehouse.getSelectionModel().clearSelection();
                initTreeTable();

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacén",
                        "Eliminación de almacén: " + warehouse.getWarehouseName()
                        , LocalDateTime.now()
                );
                generalRegistryService.save(generalRegistry);
            } else {
                ttvWarehouse.getSelectionModel().clearSelection();
            }
        } catch (NullPointerException e) {
            displayAlerts.showAlert("Debe seleccionar un almacén de la tabla para poder eliminarlo");
        }

    }

    @FXML
    public void assignExpense() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Asignar Inversión", "/images/lc_logo.png", "/views/assignProductView.fxml");
    }

    @FXML
    public void checkExpense(ActionEvent actionEvent) {
        try {
            ExpenseWarehouseDataTable expenseWarehouseDataTable = tvExpenses.getSelectionModel().getSelectedItem();
            switchToExpense(actionEvent);
            expenseViewController.getTfFilterId().setText(expenseWarehouseDataTable.getExpenseId() + "");
        } catch (NullPointerException e) {
            displayAlerts.showAlert("Debe seleccionar un gasto para revisarlo");
        }


    }

    @FXML
    public void updateWarehouse() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Actualizar Almacén", "/images/lc_logo.png", "/views/renameWarehouseView.fxml");
    }

    @FXML
    public void changeProductName() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Asignar Inversión a un Almacén", "/images/lc_logo.png", "/views/renameProductView.fxml");
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }
}
