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
    private final ExpenseViewController expenseViewController;
    private final BuyServiceImpl buyService;
    private final CurrencyServiceImpl currencyService;

    public WarehouseViewController(
            DisplayAlerts displayAlerts, WarehouseServiceImpl warehouseService, BuyServiceImpl buyService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, InventoryServiceImpl inventoryService,
            GeneralRegistryServiceImpl generalRegistryService, ExpenseViewController expenseViewController, CurrencyServiceImpl currencyService
    ) {
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

    // FXML UI components
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

    @FXML
    public void initialize() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        initTableValues();
        initTreeTable();
        initTableLabels();
    }

    public void initTableValues() {
        // Clear previous data
        tvExpenses.getItems().clear();

        // Obtener todas las compras de tipo "Materias Primas y Materiales" con leftAmount > 0
        List<Buy> buys = buyService.getAllBuysGreaterThanZeroByClient(client).stream()
                .filter(buy -> "Materias Primas y Materiales".equals(buy.getBuyType()))
                .toList();

        // Mapear las compras a ExpenseWarehouseDataTable
        List<ExpenseWarehouseDataTable> buyData = buys.stream()
                .map(buy -> new ExpenseWarehouseDataTable(
                        buy.getBuyId(),
                        buy.getBuyName(),
                        buy.getLeftAmount(),
                        buy.getReceivedDate()
                ))
                .toList();

        // Configurar columnas
        tcIdExpense.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        tcProductName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcProductAmount.setCellValueFactory(new PropertyValueFactory<>("productAmount"));
        tcProductDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));

        // Agregar datos a la tabla
        tvExpenses.getItems().addAll(buyData);
    }

    private void initTableLabels() {
        // Placeholder para TreeTableView (ttvWarehouse)
        ttvWarehouse.setPlaceholder(new Label("""
                📦 ¡Vaya! No hay almacenes registrados
                Añade tu primer almacén con el botón\s\s
                '(+) Agregar Almacén'"""));

        // Placeholder actualizado para TableView (tvExpenses)
        tvExpenses.setPlaceholder(new Label("""
                💼 Aquí aparecerán tus compras de Materias Primas
                Cuando hagas COMPRAS y no hayan sido asignadas,
                Se mostrarán aquí para su asignación"""));

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
        ttcValue.setCellValueFactory(new TreeItemPropertyValueFactory<>("valueInCUP"));

        List<Inventory> inventory = inventoryService.getAllInventoriesByClient(client);

        // Obtener todas las compras de Materias Primas y Materiales
        List<Buy> materialBuys = buyService.getAllBuysByClient(client).stream()
                .filter(buy -> "Materias Primas y Materiales".equals(buy.getBuyType())).toList();

        // CORRECIÓN: Calcular precio promedio ponderado por producto
        Map<String, Double> productAvgPricesInCUP = calculateWeightedAveragePrices(materialBuys);

        // Group inventories by warehouse
        Map<Warehouse, List<Inventory>> inventoriesByWarehouse = inventory.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouse));

        TreeItem<WarehouseDataTable> root = new TreeItem<>();

        inventoriesByWarehouse.forEach((warehouse, inventories) -> {
            // Calcular el valor total del almacén EN CUP
            double totalWarehouseValue = 0.0;

            for (Inventory inv : inventories) {
                if (inv.getProduct() != null && inv.getAmount() != null) {
                    Double avgPriceInCUP = productAvgPricesInCUP.get(inv.getProduct().getProductName());
                    if (avgPriceInCUP != null) {
                        totalWarehouseValue += avgPriceInCUP * inv.getAmount();
                    }
                }
            }

            int totalAmount = inventories.stream()
                    .mapToInt(inv -> inv.getAmount() != null ? inv.getAmount() : 0)
                    .sum();
            int invCount = inventories.size() - 1;

            WarehouseDataTable warehouseNode = new WarehouseDataTable(
                    warehouse.getWarehouseName(),
                    "Productos: " + invCount,
                    totalAmount,
                    String.format("%.2f CUP", totalWarehouseValue) // Valor en CUP
            );

            TreeItem<WarehouseDataTable> warehouseItem = new TreeItem<>(warehouseNode);

            // Children (products) - con valor individual EN CUP
            inventories.forEach(inv -> {
                if (!(inv.getProduct() == null || inv.getAmount() == null)) {
                    Double avgPriceInCUP = productAvgPricesInCUP.get(inv.getProduct().getProductName());
                    String productValue = (avgPriceInCUP != null) ?
                            String.format("%.2f CUP", avgPriceInCUP * inv.getAmount()) : "N/A";

                    WarehouseDataTable productNode = new WarehouseDataTable(
                            "",
                            inv.getProduct().getProductName(),
                            inv.getAmount(),
                            productValue // Valor individual en CUP
                    );
                    warehouseItem.getChildren().add(new TreeItem<>(productNode));
                }
            });

            root.getChildren().add(warehouseItem);
        });

        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false);
    }

    // Nuevo método para calcular precios promedio ponderados en CUP
    private Map<String, Double> calculateWeightedAveragePrices(List<Buy> buys) {
        Map<String, ProductSummary> productSummaries = new HashMap<>();

        for (Buy buy : buys) {
            ProductSummary summary = productSummaries.getOrDefault(buy.getBuyName(), new ProductSummary());

            double priceInCUP = convertToCUP(buy.getBuyUnitaryPrice(),
                    buy.getCurrency().getCurrencyName());

            summary.totalValue += priceInCUP * buy.getAmount();
            summary.totalAmount += buy.getAmount();
            productSummaries.put(buy.getBuyName(), summary);
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, ProductSummary> entry : productSummaries.entrySet()) {
            ProductSummary summary = entry.getValue();
            if (summary.totalAmount > 0) {
                result.put(entry.getKey(), summary.totalValue / summary.totalAmount);
            }
        }

        return result;
    }

    @FXML
    public void productionInProgress(ActionEvent actionEvent) {
        displayAlerts.showAlert("Aún en desarrollo");
    }

    // Clase auxiliar para el cálculo de promedios
    private static class ProductSummary {
        double totalValue = 0.0;
        int totalAmount = 0;
    }

    // Método de conversión a CUP (similar al de SellViewController)
    private Double convertToCUP(Double amount, String currency) {
        if (amount == null) return 0.0;
        if ("CUP".equalsIgnoreCase(currency) || currency == null || currency.trim().isEmpty()) {
            return amount;
        }

        try {
            // Necesitas inyectar CurrencyService en WarehouseViewController
            Currency currencyEntity = currencyService.getCurrencyByName(currency);
            if (currencyEntity != null && currencyEntity.getCurrencyPriceInCUP() != null) {
                return amount * currencyEntity.getCurrencyPriceInCUP();
            }
            return amount;
        } catch (Exception e) {
            return amount;
        }
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
        ExpenseWarehouseDataTable expenseWarehouseDataTable = tvExpenses.getSelectionModel().getSelectedItem();
        if (expenseWarehouseDataTable == null) {
            displayAlerts.showAlert("Debe seleccionar un gasto para revisarlo");
        } else {
            switchToExpense(actionEvent);
            expenseViewController.getTfFilterId().setText(expenseWarehouseDataTable.getExpenseId() + "");
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
}
