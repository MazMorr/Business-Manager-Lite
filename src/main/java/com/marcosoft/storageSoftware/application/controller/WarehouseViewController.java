package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseWarehouseDataTable;
import com.marcosoft.storageSoftware.application.dto.WarehouseDataTable;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de almacenes y asignación de productos/inversiones.
 * Maneja la visualización de almacenes, productos asignados y gastos disponibles.
 */
@Lazy
@Controller
public class WarehouseViewController {
    private static final String RAW_MATERIALS_TYPE = "Materias Primas y Materiales";
    private static final String CUP_CURRENCY = "CUP";

    private Client client;

    // Dependencias de servicios y utilidades
    private final WarehouseServiceImpl warehouseService;
    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final DisplayAlerts displayAlerts;
    private final InventoryServiceImpl inventoryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final ExpenseViewController expenseViewController;
    private final BuyServiceImpl buyService;
    private final CurrencyServiceImpl currencyService;

    /**
     * Constructor con inyección de dependencias.
     */
    public WarehouseViewController(
            DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, BuyServiceImpl buyService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, InventoryServiceImpl inventoryService,
            GeneralRegistryServiceImpl generalRegistryService, ExpenseViewController expenseViewController,
            CurrencyServiceImpl currencyService) {
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
        this.generalRegistryService = generalRegistryService;
        this.expenseViewController = expenseViewController;
        this.buyService = buyService;
        this.currencyService = currencyService;
    }

    // Componentes de UI FXML
    @FXML
    private TableView<ExpenseWarehouseDataTable> tvExpenses;
    @FXML
    private TreeTableView<WarehouseDataTable> ttvWarehouse;

    @FXML
    private TreeTableColumn<WarehouseDataTable, String> ttcValue, ttcWarehouseName, ttcProductName;
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
     * Inicializa el controlador después de que se hayan inyectado los componentes FXML.
     * Configura el cliente, etiquetas y inicializa las tablas.
     */
    @FXML
    public void initialize() {
        initializeClient();
        initializeTableValues();
        initializeTreeTable();
        initializeTablePlaceholders();
    }

    /**
     * Inicializa los datos del cliente y los muestra en la UI.
     */
    private void initializeClient() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
    }

    /**
     * Inicializa y popula la tabla de gastos con compras de materias primas no asignadas.
     */
    public void initializeTableValues() {
        tvExpenses.getItems().clear();

        List<Buy> rawMaterialBuys = buyService.getAllBuysGreaterThanZeroByClient(client).stream()
                .filter(buy -> RAW_MATERIALS_TYPE.equals(buy.getBuyType()))
                .toList();

        List<ExpenseWarehouseDataTable> buyData = rawMaterialBuys.stream()
                .map(buy -> new ExpenseWarehouseDataTable(
                        buy.getBuyId(),
                        buy.getBuyName(),
                        buy.getLeftAmount(),
                        buy.getReceivedDate()))
                .toList();

        configureExpenseTableColumns();
        tvExpenses.getItems().addAll(buyData);
    }

    /**
     * Configura las columnas de la tabla de gastos.
     */
    private void configureExpenseTableColumns() {
        tcIdExpense.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        tcProductName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcProductAmount.setCellValueFactory(new PropertyValueFactory<>("productAmount"));
        tcProductDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
    }

    /**
     * Inicializa los placeholders de las tablas con mensajes informativos.
     */
    private void initializeTablePlaceholders() {
        String placeholderStyle = "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 14px; " +
                "-fx-text-alignment: center; " +
                "-fx-text-fill: #64748b; " +
                "-fx-alignment: center; " +
                "-fx-padding: 10px; ";

        ttvWarehouse.setPlaceholder(createPlaceholderLabel("""
                📦 ¡Vaya! No hay almacenes registrados
                Añade tu primer almacén con el botón\s\s
                '(+) Agregar Almacén'""", placeholderStyle));

        tvExpenses.setPlaceholder(createPlaceholderLabel("""
                💼 Aquí aparecerán tus compras de Materias Primas
                Cuando hagas COMPRAS y no hayan sido asignadas,
                Se mostrarán aquí para su asignación""", placeholderStyle));
    }

    /**
     * Crea una etiqueta para el placeholder con el estilo especificado.
     */
    private Label createPlaceholderLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    /**
     * Inicializa y configura el TreeTableView de almacenes y productos.
     */
    public void initializeTreeTable() {
        configureTreeTableColumns();

        List<Inventory> inventory = inventoryService.getAllInventoriesByClient(client);
        List<Buy> materialBuys = getRawMaterialBuys();
        Map<String, Double> productAvgPricesInCUP = calculateWeightedAveragePrices(materialBuys);

        TreeItem<WarehouseDataTable> root = buildTreeTableRoot(inventory, productAvgPricesInCUP);
        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false);
    }

    /**
     * Configura las columnas del TreeTableView.
     */
    private void configureTreeTableColumns() {
        ttcWarehouseName.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));
        ttcValue.setCellValueFactory(new TreeItemPropertyValueFactory<>("valueInCUP"));
    }

    /**
     * Obtiene las compras de materias primas del cliente.
     */
    private List<Buy> getRawMaterialBuys() {
        return buyService.getAllBuysByClient(client).stream()
                .filter(buy -> RAW_MATERIALS_TYPE.equals(buy.getBuyType()))
                .toList();
    }

    /**
     * Construye la estructura jerárquica del TreeTableView.
     */
    private TreeItem<WarehouseDataTable> buildTreeTableRoot(List<Inventory> inventory,
                                                            Map<String, Double> productAvgPricesInCUP) {
        Map<Warehouse, List<Inventory>> inventoriesByWarehouse = inventory.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouse));

        TreeItem<WarehouseDataTable> root = new TreeItem<>();

        inventoriesByWarehouse.forEach((warehouse, inventories) -> {
            TreeItem<WarehouseDataTable> warehouseItem = createWarehouseTreeItem(
                    warehouse, inventories, productAvgPricesInCUP);
            root.getChildren().add(warehouseItem);
        });

        return root;
    }

    /**
     * Crea un ítem de árbol para un almacén con sus productos.
     */
    private TreeItem<WarehouseDataTable> createWarehouseTreeItem(Warehouse warehouse,
                                                                 List<Inventory> inventories,
                                                                 Map<String, Double> productAvgPricesInCUP) {
        WarehouseSummary summary = calculateWarehouseSummary(inventories, productAvgPricesInCUP);

        WarehouseDataTable warehouseNode = new WarehouseDataTable(
                warehouse.getWarehouseName(),
                "Productos: " + (inventories.size() - 1),
                summary.totalAmount,
                String.format("%.2f CUP", summary.totalValue));

        TreeItem<WarehouseDataTable> warehouseItem = new TreeItem<>(warehouseNode);
        addProductTreeItems(warehouseItem, inventories, productAvgPricesInCUP);

        return warehouseItem;
    }

    /**
     * Calcula el resumen de valores para un almacén.
     */
    private WarehouseSummary calculateWarehouseSummary(List<Inventory> inventories,
                                                       Map<String, Double> productAvgPricesInCUP) {
        double totalValue = 0.0;
        int totalAmount = 0;

        for (Inventory inv : inventories) {
            if (isValidInventory(inv)) {
                Double avgPriceInCUP = productAvgPricesInCUP.get(inv.getProduct().getProductName());
                if (avgPriceInCUP != null) {
                    totalValue += avgPriceInCUP * inv.getAmount();
                }
                totalAmount += inv.getAmount();
            }
        }

        return new WarehouseSummary(totalValue, totalAmount);
    }

    /**
     * Verifica si un inventario es válido para procesar.
     */
    private boolean isValidInventory(Inventory inventory) {
        return inventory.getProduct() != null && inventory.getAmount() != null;
    }

    /**
     * Añade los productos como hijos del ítem del almacén.
     */
    private void addProductTreeItems(TreeItem<WarehouseDataTable> warehouseItem,
                                     List<Inventory> inventories,
                                     Map<String, Double> productAvgPricesInCUP) {
        inventories.stream()
                .filter(this::isValidInventory)
                .forEach(inv -> {
                    TreeItem<WarehouseDataTable> productItem = createProductTreeItem(inv, productAvgPricesInCUP);
                    warehouseItem.getChildren().add(productItem);
                });
    }

    /**
     * Crea un ítem de árbol para un producto.
     */
    private TreeItem<WarehouseDataTable> createProductTreeItem(Inventory inventory,
                                                               Map<String, Double> productAvgPricesInCUP) {
        String productValue = calculateProductValue(inventory, productAvgPricesInCUP);

        WarehouseDataTable productNode = new WarehouseDataTable(
                "",
                inventory.getProduct().getProductName(),
                inventory.getAmount(),
                productValue);

        return new TreeItem<>(productNode);
    }

    /**
     * Calcula el valor de un producto en CUP.
     */
    private String calculateProductValue(Inventory inventory, Map<String, Double> productAvgPricesInCUP) {
        Double avgPriceInCUP = productAvgPricesInCUP.get(inventory.getProduct().getProductName());
        return (avgPriceInCUP != null) ?
                String.format("%.2f CUP", avgPriceInCUP * inventory.getAmount()) : "N/A";
    }

    /**
     * Calcula los precios promedio ponderados de los productos en CUP.
     */
    private Map<String, Double> calculateWeightedAveragePrices(List<Buy> buys) {
        Map<String, ProductSummary> productSummaries = new HashMap<>();

        for (Buy buy : buys) {
            String productName = buy.getBuyName();
            ProductSummary summary = productSummaries.getOrDefault(productName, new ProductSummary());

            double priceInCUP = convertToCUP(buy.getBuyUnitaryPrice(), buy.getCurrency().getCurrencyName());
            summary.addTransaction(priceInCUP, buy.getAmount());

            productSummaries.put(productName, summary);
        }

        return productSummaries.entrySet().stream()
                .filter(entry -> entry.getValue().totalAmount > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().totalValue / entry.getValue().totalAmount));
    }

    /**
     * Convierte un monto a CUP basado en la moneda.
     */
    private Double convertToCUP(Double amount, String currency) {
        if (amount == null) return 0.0;
        if (CUP_CURRENCY.equalsIgnoreCase(currency) || currency == null || currency.trim().isEmpty()) {
            return amount;
        }

        try {
            Currency currencyEntity = currencyService.getCurrencyByName(currency);
            if (currencyEntity != null && currencyEntity.getCurrencyPriceInCUP() != null) {
                return amount * currencyEntity.getCurrencyPriceInCUP();
            }
            return amount;
        } catch (Exception e) {
            return amount;
        }
    }

    // Métodos de navegación y acciones
    @FXML
    public void productionInProgress(ActionEvent actionEvent) {
        displayAlerts.showAlert("Aún en desarrollo");
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
            TreeItem<WarehouseDataTable> selectedItem = ttvWarehouse.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                displayAlerts.showAlert("Debe seleccionar un almacén de la tabla para poder eliminarlo");
                return;
            }

            WarehouseDataTable warehouseData = selectedItem.getValue();
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                    warehouseData.getWarehouseName(), client);

            if (displayAlerts.showConfirmationAlert(
                    "¿Está seguro de querer eliminar el almacén seleccionado: " +
                            warehouseData.getWarehouseName() + " junto a todos los productos almacenados en él?")) {

                deleteWarehouseAndContents(warehouse);
                ttvWarehouse.getSelectionModel().clearSelection();
                initializeTreeTable();
                logWarehouseDeletion(warehouse);
            } else {
                ttvWarehouse.getSelectionModel().clearSelection();
            }
        } catch (NullPointerException e) {
            displayAlerts.showAlert("Debe seleccionar un almacén de la tabla para poder eliminarlo");
        }
    }

    /**
     * Elimina un almacén y todo su contenido.
     */
    private void deleteWarehouseAndContents(Warehouse warehouse) {
        List<Inventory> inventories = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);
        for (Inventory inv : inventories) {
            inventoryService.deleteInventoryById(inv.getId());
        }
        warehouseService.deleteWarehouseById(warehouse.getId());
    }

    /**
     * Registra la eliminación del almacén en el registro general.
     */
    private void logWarehouseDeletion(Warehouse warehouse) {
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null, client, "Almacén",
                "Eliminación de almacén: " + warehouse.getWarehouseName(),
                LocalDateTime.now());
        generalRegistryService.save(generalRegistry);
    }

    @FXML
    public void assignExpense() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow("Asignar Inversión", "/images/lc_logo.png", "/views/assignProductView.fxml");
    }

    @FXML
    public void checkExpense(ActionEvent actionEvent) {
        ExpenseWarehouseDataTable selectedExpense = tvExpenses.getSelectionModel().getSelectedItem();
        if (selectedExpense == null) {
            displayAlerts.showAlert("Debe seleccionar un gasto para revisarlo");
        } else {
            switchToExpense(actionEvent);
            expenseViewController.getTfFilterId().setText(String.valueOf(selectedExpense.getExpenseId()));
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

    // Métodos de navegación entre vistas
    @FXML
    private void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchToConfiguration(actionEvent);
    }

    @FXML
    private void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchToSupport(actionEvent);
    }

    @FXML
    private void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchToRegistry(actionEvent);
    }

    @FXML
    private void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchToBalance(actionEvent);
    }

    @FXML
    private void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchToExpense(actionEvent);
    }

    @FXML
    private void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchToSell(actionEvent);
    }

    @FXML
    public void switchToBuy(ActionEvent actionEvent) {
        sceneSwitcher.switchToBuy(actionEvent);
    }

    // Clases auxiliares para cálculos
    /**
     * Resumen de valores para un almacén.
     */
    private static class WarehouseSummary {
        final double totalValue;
        final int totalAmount;

        WarehouseSummary(double totalValue, int totalAmount) {
            this.totalValue = totalValue;
            this.totalAmount = totalAmount;
        }
    }

    /**
     * Resumen de transacciones para cálculo de promedios ponderados.
     */
    private static class ProductSummary {
        double totalValue = 0.0;
        int totalAmount = 0;

        void addTransaction(double value, int amount) {
            this.totalValue += value * amount;
            this.totalAmount += amount;
        }
    }
}