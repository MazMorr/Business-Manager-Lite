package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.SellDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.service.WarehouseService;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the sell view.
 * Handles logic for selling products, assigning prices, filtering inventory, and navigation.
 */
@Slf4j
@Controller
public class SellViewController {
    private Client client;

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final ParseDataTypes parseDataTypes;
    private final SceneSwitcher sceneSwitcher;
    private final InventoryServiceImpl inventoryService;
    private final ProductServiceImpl productService;
    private final WarehouseService warehouseService;
    private final DisplayAlerts displayAlerts;
    private final ClientServiceImpl clientService;
    private final CurrencyServiceImpl currencyService;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;

    /**
     * Constructor for dependency injection.
     */
    public SellViewController(
            CurrencyServiceImpl currencyService, ClientServiceImpl clientService, DisplayAlerts displayAlerts,
            UserLogged userLogged, ParseDataTypes parseDataTypes, SceneSwitcher sceneSwitcher,
            InventoryServiceImpl inventoryService, ProductServiceImpl productService, WarehouseService warehouseService,
            SellRegistryServiceImpl sellRegistryService, GeneralRegistryServiceImpl generalRegistryService
    ) {
        this.inventoryService = inventoryService;
        this.generalRegistryService = generalRegistryService;
        this.sellRegistryService = sellRegistryService;
        this.displayAlerts = displayAlerts;
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.parseDataTypes = parseDataTypes;
        this.userLogged = userLogged;
    }

    // FXML UI components
    @FXML
    private TextField tfSellProductCurrency, tfMinFilterAmount, tfMaxFilterAmount, tfAssignPriceProductPrice,
            tfSellProductAmount, tfFilterProductName, tfSellProductPrice, tfMinFilterPrice, tfSellProductName,
            tfMaxFilterPrice, tfFilterWarehouseName, tfAssignPriceCurrency, tfAssignPriceProductName, tfSellWarehouse;
    @FXML
    private MenuButton mbAssignPriceCurrency, mbSellCurrency, mbSellWarehouse, mbSellProduct;
    @FXML
    private Label lblClientName;
    @FXML
    private DatePicker dpSellProductDate;
    @FXML
    private TreeTableView<SellDataTable> ttvInventory;
    @FXML
    private TreeTableColumn<SellDataTable, String> ttcWarehouse, ttcProductName;
    @FXML
    private TreeTableColumn<SellDataTable, Double> ttcSellPrice;
    @FXML
    private TreeTableColumn<SellDataTable, Integer> ttcProductAmount;

    private ObservableList<TreeItem<SellDataTable>> originalItems = FXCollections.observableArrayList();
    private FilteredList<TreeItem<SellDataTable>> filteredItems;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table columns, loads inventory, listeners, date picker, warehouse and currency menus.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        lblClientName.setText(client.getClientName());

        // Initialize filteredItems with originalItems as source
        filteredItems = new FilteredList<>(originalItems, p -> true);

        Platform.runLater(() -> {
            setupTableColumns();
            loadProductTable();
            setupFilterListeners();
            initDatePicker();
            initMbWarehouse();
            initAllMbCurrency();
            setupTableSelectionListener();
            setupOtherTextFieldListeners();
        });
    }

    @FXML
    public void cleanFilters() {
        tfFilterProductName.clear();
        tfFilterWarehouseName.clear();
        tfMinFilterAmount.clear();
        tfMinFilterPrice.clear();
        tfMaxFilterAmount.clear();
        tfMaxFilterPrice.clear();

        loadProductTable();
    }

    /**
     * Clears all input fields in the sell and assign price forms.
     */
    @FXML
    public void cleanForm() {
        tfAssignPriceCurrency.clear();
        tfAssignPriceProductName.clear();
        tfAssignPriceProductPrice.clear();
        tfSellProductAmount.clear();
        tfSellProductCurrency.clear();
        tfSellProductName.clear();
        tfSellProductPrice.clear();
        dpSellProductDate.setValue(LocalDate.now());
    }

    /**
     * Handles the selling of a product.
     * Validates input fields and updates inventory.
     * Improved version with:
     * - Better transaction handling
     * - Stock validation before modification
     * - Clearer error messages
     */
    @FXML
    public void sellProduct() {
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
            // 1. Obtener entidades necesarias
            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
            Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

            // 2. Validación adicional del stock (por si acaso)
            if (inventory.getAmount() < productAmount) {
                displayAlerts.showAlert("No hay suficiente stock disponible. Stock actual: " + inventory.getAmount());
                return;
            }

            // 3. Actualizar inventario (manejar caso de venta total)
            int newAmount = inventory.getAmount() - productAmount;
            if (newAmount == 0) {
                // Eliminar el registro de inventario si el stock llega a cero
                inventoryService.deleteInventoryById(inventory.getId());
            } else {
                // Actualizar la cantidad normalmente
                inventory.setAmount(newAmount);
                inventoryService.save(inventory);
            }

            // 4. Registrar la venta
            SellRegistry sellRegistry = new SellRegistry(
                    null, client, "Venta", LocalDateTime.now(), productName,
                    currencyName, productSellPrice, date, warehouseName, productAmount
            );
            sellRegistryService.save(sellRegistry);

            // 5. Registrar en el historial general
            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Ventas", "Venta de " + productAmount + " unidades de " + productName,
                    LocalDateTime.now()
            );
            generalRegistryService.save(generalRegistry);

            // 6. Actualizar UI y mostrar confirmación
            loadProductTable();
            cleanForm();
            displayAlerts.showAlert("Venta registrada exitosamente. Stock restante: " + (newAmount > 0 ? newAmount : "AGOTADO"));

        } catch (DataIntegrityViolationException e) {
            displayAlerts.showAlert("Error de integridad de datos. Posiblemente el producto ya fue modificado.");
        } catch (EmptyResultDataAccessException e) {
            displayAlerts.showAlert("El producto o almacén ya no existe en la base de datos");
        } catch (Exception e) {
            displayAlerts.showAlert("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Handles the assignment of a price to a product.
     * Validates input fields and updates product price.
     */
    @FXML
    public void assignProductPrice() {
        if (!validateAllAssignPriceFields()) {
            return;
        }

        String productName = tfAssignPriceProductName.getText();
        Double productPrice = parseDataTypes.parseDouble(tfAssignPriceProductPrice.getText());
        String productCurrency = tfAssignPriceCurrency.getText();

        try {
            Currency currency = currencyService.getCurrencyByName(productCurrency);
            Product product = productService.getByProductNameAndClient(productName, client);
            product.setSellPrice(productPrice);
            product.setCurrency(currency);
            productService.save(product);

            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Ventas", "Asignación Precio", LocalDateTime.now()
            );
            generalRegistryService.save(generalRegistry);

            displayAlerts.showAlert("El precio del producto ha sido guardado satisfactoriamente");
            cleanForm();
            loadProductTable();
        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
        }
    }

    /**
     * Validates all fields in the assign price form.
     */
    private boolean validateAllAssignPriceFields() {
        return validateTfAssignProduct() && validateTfPriceAssign() && validateTfAssignCurrency();
    }

    /**
     * Validates the currency field in the assign price form.
     */
    private boolean validateTfAssignCurrency() {
        String currencyName = tfAssignPriceCurrency.getText();

        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }

        if (!currencyName.matches("[a-zA-Z]+")) {
            displayAlerts.showAlert("La moneda solo puede contener letras");
            return false;
        }

        return true;
    }

    /**
     * Validates the product field in the assign price form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfAssignProduct() {
        String productName = tfAssignPriceProductName.getText();

        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Debe asignar un producto");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto no existe en la base de datos");
            return false;
        }

        return true;
    }

    /**
     * Validates the price field in the assign price form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfPriceAssign() {
        String priceText = tfAssignPriceProductPrice.getText();

        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("El precio no debe estar vacío");
            return false;
        }

        try {
            // Handle locale-specific decimal separators
            priceText = priceText.replace(",", ".");
            double price = Double.parseDouble(priceText);

            if (price <= 0) {
                displayAlerts.showAlert("El precio debe ser mayor que 0");
                return false;
            }

            // Check decimal places safely
            String[] parts = priceText.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                displayAlerts.showAlert("El precio solo puede tener 2 decimales");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El precio debe ser un número válido");
            return false;
        }
    }

    /**
     * Validates all fields in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateAllSellFields() {
        return validateTfSellProduct() && validateTfSellAmount() && validateSellPrice() && validateSellCurrency() && validateDatePicker();
    }

    private void setupTableSelectionListener() {
        ttvInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // If this is a child node (warehouse), we need to get product info from parent
                TreeItem<SellDataTable> parentItem = newSel.getParent();
                boolean isChildNode = parentItem != null && parentItem != ttvInventory.getRoot();
                boolean isParentWithChildren = !isChildNode && !newSel.getChildren().isEmpty();

                // Get the appropriate SellDataTable (child or parent)
                SellDataTable selectedData = newSel.getValue();
                SellDataTable productData = isChildNode ? parentItem.getValue() : selectedData;

                // Get product info from the product node (parent or self)
                String productName = productData.getProductName();
                Product product = productService.getByProductNameAndClient(productName, client);

                // Format price and currency
                String price = product.getSellPrice() != null ? String.valueOf(product.getSellPrice()) : "";
                String currency = product.getCurrency() != null ? product.getCurrency().getCurrencyName() : "";

                // Get warehouse name - only if it's a child node (warehouse)
                String warehouseName = warehouseName = selectedData.getWarehouseName();
                if (isParentWithChildren) {
                    warehouseName = "";
                }

                // Populate form fields
                tfAssignPriceProductName.setText(productName);
                tfAssignPriceCurrency.setText(currency);
                tfAssignPriceProductPrice.setText(price);
                tfSellWarehouse.setText(warehouseName); // Solo se llena si es un nodo hijo (almacén)
                tfSellProductName.setText(productName);
                tfSellProductPrice.setText(price);
                tfSellProductCurrency.setText(currency);
            }
        });
    }

    /**
     * Validates the currency field in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateSellCurrency() {
        String currencyName = tfSellProductCurrency.getText();

        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }

        if (!currencyService.existsByCurrencyName(currencyName)) {
            displayAlerts.showAlert("La moneda seleccionada no existe en la base de datos");
            return false;
        }

        return true;
    }

    /**
     * Validates the price field in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateSellPrice() {
        String priceText = tfSellProductPrice.getText();

        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("El precio no puede estar vacío");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);

            if (price <= 0) {
                displayAlerts.showAlert("El precio debe ser mayor que cero");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El precio debe ser un número válido");
            return false;
        }
    }

    /**
     * Validates the date picker field in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateDatePicker() {
        LocalDate selectedDate = dpSellProductDate.getValue();

        if (selectedDate == null) {
            displayAlerts.showAlert("Por favor selecciona una fecha válida");
            return false;
        }

        if (selectedDate.isAfter(LocalDate.now())) {
            displayAlerts.showAlert("La fecha de venta no puede ser en el futuro");
            return false;
        }

        return true;
    }

    /**
     * Validates the amount field in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfSellAmount() {
        String amountText = tfSellProductAmount.getText();

        if (amountText == null || amountText.isEmpty()) {
            displayAlerts.showAlert("La cantidad no pueda estar vacía");
            return false;
        }

        try {
            int amount = Integer.parseInt(amountText);

            if (amount <= 0) {
                displayAlerts.showAlert("La cantidad debe ser mayor que cero");
                return false;
            }

            if (validateTfSellProduct() && !tfSellWarehouse.getText().isEmpty()) {
                Product product = productService.getByProductNameAndClient(tfSellProductName.getText(), client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfSellWarehouse.getText(), client);
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                if (inventory.getAmount() < amount) {
                    displayAlerts.showAlert("No hay suficiente existencias disponibles");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }
    }

    /**
     * Validates the product and warehouse fields in the sell form.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateTfSellProduct() {
        String productName = tfSellProductName.getText();
        String warehouseName = tfSellWarehouse.getText();

        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un producto");
            return false;
        }

        if (warehouseName == null || warehouseName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un almacén");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto no existe");
            return false;
        }

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("El almacén no existe");
            return false;
        }

        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("El producto no está disponible en el almacén seleccionado");
            return false;
        }

        return true;
    }

    // ============================
    // NAVIGATION METHODS
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
     * Navigates to the warehouse view.
     */
    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/warehouseView.fxml");
    }

    /**
     * Navigates to the investment view.
     */
    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/investmentView.fxml");
    }

    /**
     * Navigates to the balance view.
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/balanceView.fxml");
    }

    /**
     * Sets up the columns for the inventory table.
     */
    private void setupTableColumns() {
        ttcWarehouse.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcSellPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("sellPrice"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));
    }

    private void loadProductTable() {
        try {
            // Obtener valores de filtro
            String productNameFilter = tfFilterProductName.getText().trim().toLowerCase();
            String warehouseNameFilter = tfFilterWarehouseName.getText().trim().toLowerCase();
            Integer minAmount = tfMinFilterAmount.getText().isEmpty() ? null : parseDataTypes.parseInt(tfMinFilterAmount.getText());
            Integer maxAmount = tfMaxFilterAmount.getText().isEmpty() ? null : parseDataTypes.parseInt(tfMaxFilterAmount.getText());
            Double minPrice = tfMinFilterPrice.getText().isEmpty() ? null : parseDataTypes.parseDouble(tfMinFilterPrice.getText());
            Double maxPrice = tfMaxFilterPrice.getText().isEmpty() ? null : parseDataTypes.parseDouble(tfMaxFilterPrice.getText());

            // Limpiar listas
            originalItems.clear();

            // Obtener datos de la base de datos
            List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
            if (inventories == null) {
                inventories = Collections.emptyList();
            }

            TreeItem<SellDataTable> root = new TreeItem<>(new SellDataTable());

            // Filtrar y agrupar durante la carga
            Map<Product, List<Inventory>> inventoriesByProduct = inventories.stream()
                    .filter(inv -> inv != null && inv.getProduct() != null && inv.getWarehouse() != null)
                    .filter(inv -> {
                        Product product = inv.getProduct();
                        // Aplicar filtros directamente aquí
                        if (!productNameFilter.isEmpty() &&
                                !product.getProductName().toLowerCase().contains(productNameFilter)) {
                            return false;
                        }
                        if (minPrice != null &&
                                (product.getSellPrice() == null || product.getSellPrice() < minPrice)) {
                            return false;
                        }
                        return maxPrice == null ||
                                (product.getSellPrice() != null && product.getSellPrice() <= maxPrice);
                    })
                    .collect(Collectors.groupingBy(Inventory::getProduct));

            // Construir el árbol con los datos filtrados
            inventoriesByProduct.forEach((product, invList) -> {
                // Filtrar por almacén y cantidad
                List<Inventory> filteredInvList = invList.stream()
                        .filter(inv -> {
                            if (!warehouseNameFilter.isEmpty() &&
                                    !inv.getWarehouse().getWarehouseName().toLowerCase().contains(warehouseNameFilter)) {
                                return false;
                            }
                            if (minAmount != null && inv.getAmount() < minAmount) {
                                return false;
                            }
                            return maxAmount == null || inv.getAmount() <= maxAmount;
                        })
                        .toList();

                if (!filteredInvList.isEmpty()) {
                    int totalAmount = filteredInvList.stream().mapToInt(Inventory::getAmount).sum();

                    if (filteredInvList.size() > 1) {
                        TreeItem<SellDataTable> productItem = new TreeItem<>(
                                new SellDataTable(
                                        product.getProductName(),
                                        product.getSellPrice(),
                                        "Almacenes: " + filteredInvList.size(),
                                        totalAmount
                                )
                        );

                        filteredInvList.forEach(inv -> {
                            TreeItem<SellDataTable> warehouseNode = new TreeItem<>(
                                    new SellDataTable(
                                            "",
                                            null,
                                            inv.getWarehouse().getWarehouseName(),
                                            inv.getAmount()
                                    )
                            );
                            productItem.getChildren().add(warehouseNode);
                        });

                        root.getChildren().add(productItem);
                        originalItems.add(productItem);
                    } else {
                        TreeItem<SellDataTable> singleNode = new TreeItem<>(
                                new SellDataTable(
                                        product.getProductName(),
                                        product.getSellPrice(),
                                        filteredInvList.getFirst().getWarehouse().getWarehouseName(),
                                        totalAmount
                                )
                        );
                        root.getChildren().add(singleNode);
                        originalItems.add(singleNode);
                    }
                }
            });

            ttvInventory.setRoot(root);
            ttvInventory.setShowRoot(false);

        } catch (Exception e) {
            displayAlerts.showAlert("Error al cargar inventario");
            e.printStackTrace();
        }
    }

    /**
     * Sets up listeners for filter fields to trigger table filtering.
     */
    private void setupFilterListeners() {
        // Add listeners to all filter fields
        tfFilterProductName.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
        tfFilterWarehouseName.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
        tfMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> loadProductTable());
    }

    private void setupOtherTextFieldListeners() {
        tfSellProductAmount.textProperty().addListener((obs, oldVal, newVal) -> updatePricePerAmount());
    }

    private void updatePricePerAmount() {
        try {
            double price = productService.getByProductNameAndClient(tfSellProductName.getText(), client).getSellPrice();
            double pricePerAmount = Double.parseDouble(tfSellProductAmount.getText()) * price;
            tfSellProductPrice.setText(String.valueOf(pricePerAmount));
        } catch (NullPointerException e) {
            System.out.println("No hay precios para este producto en la base de datos");
        }
    }

    /**
     * Initializes the date picker with the current date.
     */
    private void initDatePicker() {
        dpSellProductDate.setValue(LocalDate.now());
    }

    /**
     * Initializes the warehouse menu with all warehouses for the current client.
     */
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

    /**
     * Initializes the product menu for the selected warehouse.
     */
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

    /**
     * Initializes both currency menus for selling and assigning price.
     */
    private void initAllMbCurrency() {
        initMbCurrency(mbSellCurrency, tfSellProductCurrency);
        initMbCurrency(mbAssignPriceCurrency, tfAssignPriceCurrency);
    }

    /**
     * Initializes a currency menu with all available currencies.
     */
    private void initMbCurrency(MenuButton mb, TextField tf) {
        mb.getItems().clear();
        List<Currency> currencies = currencyService.getAllCurrencies();

        for (Currency currency : currencies) {
            MenuItem item = new MenuItem(currency.getCurrencyName());
            item.setOnAction(e -> tf.setText(item.getText()));
            mb.getItems().add(item);
        }
    }
}
