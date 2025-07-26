package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.application.dto.SellDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.service.WarehouseService;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Lazy
@Controller
public class SellViewController {

    private final UserLogged userLogged;
    private final ParseDataTypes parseDataTypes;
    private final SceneSwitcher sceneSwitcher;
    private final InventoryServiceImpl inventoryService;
    private final ProductServiceImpl productService;
    private final WarehouseService warehouseService;

    @Lazy
    public SellViewController(UserLogged userLogged, ParseDataTypes parseDataTypes, SceneSwitcher sceneSwitcher, InventoryServiceImpl inventoryService, ProductServiceImpl productService, WarehouseService warehouseService) {
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.parseDataTypes = parseDataTypes;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField txtSellProductCurrency, txtMinFilterAmount, txtMaxFilterAmount,
            txtAssignPriceProductPrice, txtSellProductAmount, txtFilterProductName, txtSellProductPrice,
            txtMinFilterPrice, txtSellProductName, txtMaxFilterPrice, txtFilterWarehouseName,
            txtAssignPriceCurrency, txtAssignPriceProductName;
    @FXML
    private MenuButton mbAssignPriceCurrency, mbSellCurrency;
    @FXML
    private Label txtClientName, txtAssignPriceDebug, txtSellDebug;
    @FXML
    private DatePicker txtSellProductDate;
    @FXML
    private Pagination paginator;
    @FXML
    private TreeTableView<SellDataTable> ttvInventory;
    @FXML
    private TreeTableColumn<SellDataTable, String> ttcWarehouse, ttcProductName;
    @FXML
    private TreeTableColumn<SellDataTable, Double> ttcSellPrice;
    @FXML
    private TreeTableColumn<SellDataTable, Integer> ttcProductAmount;

    private final ObservableList<SellDataTable> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            setupTableColumns();
            loadProductTable();
            setupFilterListeners();
        });
    }

    private void setupTableColumns() {
        ttcWarehouse.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcSellPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("sellPrice"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));
    }

    private void loadProductTable() {
        inventoryList.clear();
        List<Inventory> inventories = inventoryService.getAllInventories();
        List<SellDataTable> inventoriesObs = new ArrayList<>();
        for (Inventory i : inventories) {
            SellDataTable sell = new SellDataTable(i.getProduct().getProductName(),
                    i.getProduct().getSellPrice(), i.getWarehouse().getWarehouseName(),
                    i.getAmount());
            inventoriesObs.add(sell);
        }
        inventoryList.addAll(inventoriesObs);

        TreeItem<SellDataTable> root = new TreeItem<>();
        for (SellDataTable item : inventoryList) {
            root.getChildren().add(new TreeItem<>(item));
        }
        ttvInventory.setRoot(root);
        ttvInventory.setShowRoot(false);
        filterProductTable();
    }

    private void setupFilterListeners() {
        txtFilterProductName.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        txtFilterWarehouseName.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        txtMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        txtMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        txtMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        txtMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
    }

    private void filterProductTable() {
        String productName = txtFilterProductName.getText().trim().toLowerCase();
        String warehouseName = txtFilterWarehouseName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(txtMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(txtMaxFilterAmount.getText());
        Double minPrice = parseDataTypes.parseDouble(txtMinFilterPrice.getText());
        Double maxPrice = parseDataTypes.parseDouble(txtMaxFilterPrice.getText());

        Predicate<SellDataTable> filter = product -> {
            boolean matches = true;
            if (!productName.isEmpty())
                matches &= product.getProductName() != null && product.getProductName().toLowerCase().contains(productName);
            if (!warehouseName.isEmpty())
                matches &= product.getWarehouseName() != null && product.getWarehouseName().toLowerCase().contains(warehouseName);
            if (minAmount != null && minAmount > 0)
                matches &= product.getProductAmount() != null && product.getProductAmount() >= minAmount;
            if (maxAmount != null && maxAmount > 0)
                matches &= product.getProductAmount() != null && product.getProductAmount() <= maxAmount;
            if (minPrice != null && minPrice > 0.0)
                matches &= product.getSellPrice() != null && product.getSellPrice() >= minPrice;
            if (maxPrice != null && maxPrice > 0.0)
                matches &= product.getSellPrice() != null && product.getSellPrice() <= maxPrice;
            return matches;
        };

        List<SellDataTable> filtered = inventoryList.stream()
                .filter(filter)
                .toList();

        TreeItem<SellDataTable> root = new TreeItem<>();
        for (SellDataTable item : filtered) {
            root.getChildren().add(new TreeItem<>(item));
        }
        ttvInventory.setRoot(root);
        ttvInventory.setShowRoot(false);
    }

    @FXML
    public void cleanFilters(ActionEvent actionEvent) {
        txtFilterProductName.setText("");
        txtFilterWarehouseName.setText("");
        txtMinFilterAmount.setText(String.valueOf(0));
        txtMinFilterPrice.setText(String.valueOf(0.00));
        txtMaxFilterAmount.setText(String.valueOf(0));
        txtMaxFilterPrice.setText(String.valueOf(0.00));
    }

    @FXML
    public void sellProduct(ActionEvent actionEvent) {
    }

    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        // Implementa la lógica para limpiar el formulario
    }

    @FXML
    public void assignProductPrice(ActionEvent actionEvent) {
        // Implementa la lógica para asignar precio al producto
    }

    // ============================
    // NAVIGATION METHODS
    // ============================
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
    }

    private void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            sceneSwitcher.setRootWithEvent(actionEvent, fxmlPath);
        } catch (Exception e) {
            showAlert("Error al cambiar de vista: " + e.getMessage());
        }
    }

    // ============================
    // UTILITIES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
