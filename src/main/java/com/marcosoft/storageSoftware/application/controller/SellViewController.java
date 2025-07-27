package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.SellDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.service.WarehouseService;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Lazy
@Controller
public class SellViewController {
    private Client client;

    private final UserLogged userLogged;
    private final ParseDataTypes parseDataTypes;
    private final SceneSwitcher sceneSwitcher;
    private final InventoryServiceImpl inventoryService;
    private final ProductServiceImpl productService;
    private final WarehouseService warehouseService;
    private final DisplayAlerts displayAlerts;
    private final ClientServiceImpl clientService;
    private final CurrencyServiceImpl currencyService;

    @Lazy
    public SellViewController(
            CurrencyServiceImpl currencyService, ClientServiceImpl clientService, DisplayAlerts displayAlerts,
            UserLogged userLogged, ParseDataTypes parseDataTypes, SceneSwitcher sceneSwitcher,
            InventoryServiceImpl inventoryService, ProductServiceImpl productService, WarehouseService warehouseService
    ) {
        this.inventoryService = inventoryService;
        this.displayAlerts = displayAlerts;
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.parseDataTypes = parseDataTypes;
        this.userLogged = userLogged;
    }

    @FXML
    private TextField tfSellProductCurrency, tfMinFilterAmount, tfMaxFilterAmount,
            tfAssignPriceProductPrice, tfSellProductAmount, tfFilterProductName, tfSellProductPrice,
            tfMinFilterPrice, tfSellProductName, tfMaxFilterPrice, tfFilterWarehouseName,
            tfAssignPriceCurrency, tfAssignPriceProductName, tfSellWarehouse;
    @FXML
    private MenuButton mbAssignPriceCurrency, mbSellCurrency, mbSellWarehouse, mbSellProduct;
    @FXML
    private Label txtClientName, txtAssignPriceDebug, txtSellDebug;
    @FXML
    private DatePicker dpSellProductDate;
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
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            setupTableColumns();
            loadProductTable();
            setupFilterListeners();
            initDatePicker();
            initMbWarehouse();
            initAllMbCurrency();
        });
    }

    @FXML
    public void cleanFilters(ActionEvent actionEvent) {
        tfFilterProductName.setText("");
        tfFilterWarehouseName.setText("");
        tfMinFilterAmount.setText(String.valueOf(0));
        tfMinFilterPrice.setText(String.valueOf(0.00));
        tfMaxFilterAmount.setText(String.valueOf(0));
        tfMaxFilterPrice.setText(String.valueOf(0.00));
    }

    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        tfAssignPriceCurrency.clear();
        tfAssignPriceProductName.clear();
        tfAssignPriceProductPrice.clear();
        tfSellProductAmount.clear();
        tfSellProductCurrency.clear();
        tfSellProductName.clear();
        tfSellProductPrice.clear();
        dpSellProductDate.setValue(LocalDate.now());
    }

    @FXML
    public void sellProduct(ActionEvent actionEvent) {
        if (!validateAllSellFields()) {
            return;
        }

        String warehouseName = tfSellWarehouse.getText();
        String productName = tfSellProductName.getText();
        Integer productAmount = parseDataTypes.parseInt(tfSellProductAmount.getText());
        Double productSellPrice = parseDataTypes.parseDouble(tfSellProductPrice.getText());
        String currencyName = tfSellProductCurrency.getText();
        LocalDate date = dpSellProductDate.getValue();

        try {
            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
            Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
            inventory.setAmount(inventory.getAmount() - productAmount);
            inventoryService.save(inventory);

            //TODO: IMPLEMENTAR la lógica de guardado en base de datos el registro de la venta
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
        }
    }

    @FXML
    public void assignProductPrice(ActionEvent actionEvent) {
        if (!validateAllAssignPriceFields()) {
            return;
        }

        String productName = tfAssignPriceProductName.getText();
        Double productPrice = parseDataTypes.parseDouble(tfAssignPriceProductPrice.getText());
        String productCurrency = tfAssignPriceCurrency.getText();

        try {
            Currency currency;
            if (!currencyService.existsByCurrencyName(productCurrency)) {
                currency = new Currency(null, productCurrency, client);
            } else {
                currency = currencyService.getCurrencyByName(productCurrency);
            }

            Product product = productService.getByProductNameAndClient(productName, client);
            product.setSellPrice(productPrice);
            product.setCurrency(currency);
            productService.save(product);
            displayAlerts.showAlert("El precio del producto ha sido guardado satisfactoriamente");
            cleanForm(null);
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
        }
    }

    private boolean validateAllAssignPriceFields() {
        return validateTfAssignProduct() && validateTfPriceAssign() && validateTfAssignCurrency();
    }

    // ============================
    // VALIDATION METHODS FOR ASSIGN PRICE FORM
    // ============================

    private boolean validateTfAssignCurrency() {
        String currencyName = tfAssignPriceCurrency.getText();

        // Check if currency field is empty
        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Currency field cannot be empty");
            return false;
        }

        // Check if currency name contains only letters
        if (!currencyName.matches("[a-zA-Z]+")) {
            displayAlerts.showAlert("Currency must contain only letters");
            return false;
        }

        return true;
    }

    private boolean validateTfAssignProduct() {
        String productName = tfAssignPriceProductName.getText();

        // Check if product field is empty
        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Product field cannot be empty");
            return false;
        }

        // Check if product exists for this client
        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("Product does not exist in inventory");
            return false;
        }

        return true;
    }

    private boolean validateTfPriceAssign() {
        String priceText = tfAssignPriceProductPrice.getText();

        // Check if price field is empty
        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("Price field cannot be empty");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);

            // Check if price is positive
            if (price <= 0) {
                displayAlerts.showAlert("Price must be greater than zero");
                return false;
            }

            // Check if price has valid decimal format
            if (priceText.split("\\.")[1].length() > 2) {
                displayAlerts.showAlert("Price can have maximum 2 decimal places");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("Price must be a valid number");
            return false;
        }
    }


    private boolean validateAllSellFields() {
        return validateTfSellProduct() && validateTfSellAmount() && validateSellPrice() && validateSellCurrency() && validateDatePicker();
    }

    // ============================
    // VALIDATION METHODS FOR SELL FORM
    // ============================

    private boolean validateSellCurrency() {
        String currencyName = tfSellProductCurrency.getText();

        // Check if currency field is empty
        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Currency field cannot be empty");
            return false;
        }

        // Check if currency exists in system
        if (!currencyService.existsByCurrencyName(currencyName)) {
            displayAlerts.showAlert("Selected currency is not available");
            return false;
        }

        return true;
    }

    private boolean validateSellPrice() {
        String priceText = tfSellProductPrice.getText();

        // Check if price field is empty
        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("Price field cannot be empty");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);

            // Check if price is positive
            if (price <= 0) {
                displayAlerts.showAlert("Price must be greater than zero");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("Price must be a valid number");
            return false;
        }
    }

    private boolean validateDatePicker() {
        LocalDate selectedDate = dpSellProductDate.getValue();

        // Check if date is selected
        if (selectedDate == null) {
            displayAlerts.showAlert("Please select a valid date");
            return false;
        }

        // Check if date is not in the future
        if (selectedDate.isAfter(LocalDate.now())) {
            displayAlerts.showAlert("Sale date cannot be in the future");
            return false;
        }

        return true;
    }

    private boolean validateTfSellAmount() {
        String amountText = tfSellProductAmount.getText();

        // Check if amount field is empty
        if (amountText == null || amountText.isEmpty()) {
            displayAlerts.showAlert("Amount field cannot be empty");
            return false;
        }

        try {
            int amount = Integer.parseInt(amountText);

            // Check if amount is positive
            if (amount <= 0) {
                displayAlerts.showAlert("Amount must be greater than zero");
                return false;
            }

            // Check if product and warehouse are valid before checking stock
            if (validateTfSellProduct() && !tfSellWarehouse.getText().isEmpty()) {
                Product product = productService.getByProductNameAndClient(tfSellProductName.getText(), client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfSellWarehouse.getText(), client);
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                // Check if there's enough stock
                if (inventory.getAmount() < amount) {
                    displayAlerts.showAlert("Not enough stock available");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("Amount must be a valid integer");
            return false;
        }
    }

    private boolean validateTfSellProduct() {
        String productName = tfSellProductName.getText();
        String warehouseName = tfSellWarehouse.getText();

        // Check if product field is empty
        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Product field cannot be empty");
            return false;
        }

        // Check if warehouse field is empty
        if (warehouseName == null || warehouseName.isEmpty()) {
            displayAlerts.showAlert("Warehouse field cannot be empty");
            return false;
        }

        // Check if product exists for this client
        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("Product does not exist in inventory");
            return false;
        }

        // Check if warehouse exists for this client
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("Warehouse does not exist");
            return false;
        }

        // Check if product exists in the selected warehouse
        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("Product is not available in selected warehouse");
            return false;
        }

        return true;
    }

    // ============================
    // NAVIGATION METHODS
    // ============================
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/balanceView.fxml");
    }


    // ============================
    // UTILITIES
    // ============================
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
        tfFilterProductName.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        tfFilterWarehouseName.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
        tfMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterProductTable());
    }

    private void filterProductTable() {
        String productName = tfFilterProductName.getText().trim().toLowerCase();
        String warehouseName = tfFilterWarehouseName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = parseDataTypes.parseDouble(tfMinFilterPrice.getText());
        Double maxPrice = parseDataTypes.parseDouble(tfMaxFilterPrice.getText());

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

    private void initDatePicker() {
        dpSellProductDate.setValue(LocalDate.now());
    }

    private void initMbWarehouse() {
        mbSellWarehouse.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getWarehousesByClient(clientService.getClientByName(userLogged.getName()));

        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e_ -> {
                tfSellWarehouse.setText(item.getText());
                initMbSellProduct(w);
            });
            mbSellWarehouse.getItems().add(item);
        }
    }

    private void initMbSellProduct(Warehouse w) {
        if (w == null || w.getWarehouseName() == null) return;

        mbSellProduct.getItems().clear();
        List<Inventory> inventory = inventoryService.getAllInventoriesByWarehouseAndClient(w, client);

        if (inventory.isEmpty()) {
            MenuItem item = new MenuItem("No hay productos disponibles");
            item.setDisable(true);
            mbSellProduct.getItems().add(item);
            return;
        }

        for (Inventory i : inventory) {
            if (i.getProduct() != null && i.getProduct().getProductName() != null) {
                MenuItem item = new MenuItem(i.getProduct().getProductName());
                item.setOnAction(e -> tfSellProductName.setText(item.getText()));
                mbSellProduct.getItems().add(item);
            }
        }
    }

    private void initAllMbCurrency() {
        initMbCurrency(mbSellCurrency, tfSellProductCurrency);
        initMbCurrency(mbAssignPriceCurrency, tfAssignPriceCurrency);
    }

    private void initMbCurrency(MenuButton mb, TextField tf) {
        mb.getItems().clear();
        List<Currency> currencies = currencyService.getAllCurrencies();

        for (Currency currency : currencies) {
            MenuItem item = new MenuItem(currency.getCurrencyName());
            item.setOnAction(e -> {
                tf.setText(item.getText());
            });
            mb.getItems().add(item);
        }
    }
}
