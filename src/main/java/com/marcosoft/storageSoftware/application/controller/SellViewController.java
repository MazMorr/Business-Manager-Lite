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
 * The type Sell view controller.
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

    @FXML
    private Label lblWarning, lblAlerts, txtSellDebug, txtAssignPriceDebug;


    /**
     * Instantiates a new Sell view controller.
     *
     * @param currencyService the currency service
     * @param clientService the client service
     * @param displayAlerts the display alerts
     * @param userLogged the user logged
     * @param parseDataTypes the parse data types
     * @param sceneSwitcher the scene switcher
     * @param inventoryService the inventory service
     * @param productService the product service
     * @param warehouseService the warehouse service
     * @param sellRegistryService the sell registry service
     * @param generalRegistryService the general registry service
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

    private final ObservableList<TreeItem<SellDataTable>> originalItems = FXCollections.observableArrayList();

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
        lblClientName.setText(client.getClientName());

        Platform.runLater(() -> {
            setupTableColumns();
            loadProductTable();
            setupFilterListeners();
            initDatePicker();
            initMbWarehouse();
            initAllMbCurrency();
            setupTableSelectionListener();
            setupOtherTextFieldListeners();
            loadWarningAndAlertLabels();
        });
    }

    /**
     * Load warning and alert labels.
     */
    public void loadWarningAndAlertLabels() {
        int alertCounter = 0;
        int warningCounter = 0;

        try {
            // Get all inventories for the client
            List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);

            if (inventories != null) {
                for (Inventory inventory : inventories) {
                    // Skip invalid inventory records
                    if (inventory == null || inventory.getProduct() == null || inventory.getWarehouse() == null) {
                        continue;
                    }

                    // Check for alerts (more critical condition)
                    if (inventory.getAmountAlert() != null && inventory.getAmount() <= inventory.getAmountAlert()) {
                        alertCounter++;
                    }
                    // Check for warnings (less critical condition)
                    else if (inventory.getAmountWarning() != null && inventory.getAmount() <= inventory.getAmountWarning()) {
                        warningCounter++;
                    }
                }
            }

            // Update the labels with counters
            lblAlerts.setText("Alertas (" + alertCounter + ")");
            lblWarning.setText("Advertencias (" + warningCounter + ")");

            // Optional: Style the labels based on severity
            if (alertCounter > 0) {
                lblAlerts.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;"); // Red for alerts
            } else {
                lblAlerts.setStyle(""); // Reset style
            }

            if (warningCounter > 0) {
                lblWarning.setStyle("-fx-text-fill: #ff9900; -fx-font-weight: bold;"); // Orange for warnings
            } else {
                lblWarning.setStyle(""); // Reset style
            }

        } catch (Exception e) {
            // Handle any errors gracefully
            log.error("Error loading warning and alert labels", e);
            lblAlerts.setText("Alertas (Error)");
            lblWarning.setText("Advertencias (Error)");
        }
    }

    /**
     * Clean filters.
     */
    @FXML
    public void cleanFilters() {
        List<TextField> textFields = List.of(
                tfFilterProductName, tfFilterWarehouseName, tfMinFilterAmount, tfMinFilterPrice, tfMaxFilterAmount,
                tfMaxFilterPrice
        );
        cleanTextFields(textFields);

        loadProductTable();
    }


    /**
     * Clean form.
     */
    @FXML
    public void cleanForm() {
        List<TextField> textFields = List.of(
                tfAssignPriceCurrency, tfAssignPriceProductName, tfAssignPriceProductPrice, tfSellProductAmount,
                tfSellProductCurrency, tfSellProductName, tfSellProductPrice
        );
        cleanTextFields(textFields);
        dpSellProductDate.setValue(LocalDate.now());
        txtSellDebug.setText("El precio de venta es el de toda la venta, NO PRECIOS INDIVIDUALES");
    }

    private void cleanTextFields(List<TextField> textFields) {
        for (TextField tf : textFields) {
            tf.clear();
        }
    }

    /**
     * Sell product.
     */
    @FXML
    public void sellProduct() {
        if (!validateAllSellFields()) {
            return;
        }

        try {
            // 1. Obtener datos básicos
            String warehouseName = tfSellWarehouse.getText();
            String productName = tfSellProductName.getText();
            int productAmount = parseDataTypes.parseInt(tfSellProductAmount.getText());

            // 2. Obtener entidades y validar stock
            Inventory inventory = getAndValidateInventory(warehouseName, productName, productAmount);

            // 3. Procesar venta
            processSale(inventory, productAmount, productName);

            // 4. Actualizar UI
            updateUIAfterSale(inventory);

        } catch (DataIntegrityViolationException e) {
            displayAlerts.showAlert("Error de integridad de datos. Posiblemente el producto ya fue modificado.");
        } catch (EmptyResultDataAccessException e) {
            displayAlerts.showAlert("El producto o almacén ya no existe en la base de datos");
        } catch (Exception e) {
            displayAlerts.showAlert(e.getMessage());
        }
    }

    private Inventory getAndValidateInventory(String warehouseName, String productName, int productAmount) {
        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

        if (inventory.getAmount() < productAmount) {
            throw new IllegalStateException("No hay suficiente stock. Stock actual: " + inventory.getAmount());
        }

        return inventory;
    }

    private void processSale(Inventory inventory, int productAmount, String productName) {
        // Actualizar inventario
        int newAmount = inventory.getAmount() - productAmount;
        if (newAmount == 0) {
            inventoryService.deleteInventoryById(inventory.getId());
        } else {
            inventory.setAmount(newAmount);
            inventoryService.save(inventory);
        }

        // Registrar venta
        registerSaleTransaction(productName, productAmount);
    }

    private void registerSaleTransaction(String productName, int productAmount) {
        // Datos adicionales del formulario
        double price = parseDataTypes.parseDouble(tfSellProductPrice.getText());
        String currency = tfSellProductCurrency.getText();
        LocalDate date = dpSellProductDate.getValue();

        SellRegistry sellRegistry = new SellRegistry(
                null, client, "Venta", LocalDateTime.now(),
                productName, currency, price, date,
                tfSellWarehouse.getText(), productAmount
        );
        sellRegistryService.save(sellRegistry);

        // Registrar en historial general
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null, client, "Ventas",
                "Venta de " + productAmount + " unidades de " + productName,
                LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);
    }

    private void updateUIAfterSale(Inventory inventory) {
        loadProductTable();
        cleanForm();

        int remainingStock = inventory.getAmount();
        String message = remainingStock > 0
                ? "Venta registrada. Stock restante: " + remainingStock
                : "Venta registrada. Producto AGOTADO";

        txtSellDebug.setText(message);
    }

    /**
     * Assign product price.
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

    private boolean validateAllAssignPriceFields() {
        return validateTfAssignProduct() && validateTfPriceAssign() && validateTfAssignCurrency();
    }

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

    /**
     * Switch to configuration.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    /**
     * Switch to support.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    /**
     * Switch to registry.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    /**
     * Switch to warehouse.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    /**
     * Switch to investment.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/investmentView.fxml");
    }

    /**
     * Switch to balance.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    private void setupTableColumns() {
        // Configurar cada columna con su tipo específico
        configureStringColumn(ttcWarehouse, "warehouseName");
        configureStringColumn(ttcProductName, "productName");
        configureDoubleColumn(ttcSellPrice, "sellPrice"); // Asumo que el campo se llama "sellPrice" y no "formattedPrice"
        configureIntegerColumn(ttcProductAmount, "productAmount");
    }


    // Método para columnas de tipo Double
    private void configureDoubleColumn(TreeTableColumn<SellDataTable, Double> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item)); // Formatear a 2 decimales
                }
                applyRowStyle();
            }

            private void applyRowStyle() {
                TreeTableRow<SellDataTable> row = getTableRow();
                if (row != null && row.getItem() != null) {
                    setStyle(row.getItem().getStyle());
                } else {
                    setStyle("");
                }
            }
        });
        column.setSortable(false);
    }

    // Método para columnas de tipo Integer
    private void configureIntegerColumn(TreeTableColumn<SellDataTable, Integer> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                }
                applyRowStyle();
            }

            private void applyRowStyle() {
                TreeTableRow<SellDataTable> row = getTableRow();
                if (row != null && row.getItem() != null) {
                    setStyle(row.getItem().getStyle());
                } else {
                    setStyle("");
                }
            }
        });
        column.setSortable(false);
    }

    /**
     * Load product table.
     */
    public void loadProductTable() {
        try {
            // 1. Get filters
            FilterCriteria filters = getFilterCriteria();

            // 2. Get and process data
            List<Inventory> inventories = getInventories();
            TreeItem<SellDataTable> root = processInventories(inventories, filters);

            // 3. Display results
            displayResults(root);

            // 4. Apply styles with error handling
            try {
                applyStockWarningStyles(root);
            } catch (Exception e) {
                log.error("Error applying styles to table", e);
                displayAlerts.showAlert("Error al aplicar estilos a la tabla");
            }

        } catch (Exception e) {
            handleLoadError(e);
        } finally {
            Platform.runLater(() -> {
                try {
                    ttvInventory.refresh();
                } catch (Exception e) {
                    log.error("Error refreshing table", e);
                }
            });
        }
    }

    private void configureStringColumn(TreeTableColumn<SellDataTable, String> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    TreeTableRow<SellDataTable> row = getTableRow();
                    if (row != null && row.getItem() != null && row.getItem().getStyle() != null) {
                        String style = row.getItem().getStyle();
                        if (style != null && !style.isEmpty()) {
                            setStyle(style);
                            log.debug("Applied style to cell: {}", style);
                        }
                    }
                }
            }
        });
    }

    private void applyStockWarningStyles(TreeItem<SellDataTable> root) {
        if (root == null || root.getChildren() == null) {
            log.warn("Root or its children are null");
            return;
        }

        final String alertStyle = "-fx-background-color: #ffcccc;";
        final String warningStyle = "-fx-background-color: #ffff99;";

        for (TreeItem<SellDataTable> productItem : root.getChildren()) {
            try {
                if (productItem == null || productItem.getValue() == null) {
                    continue;
                }

                // Debug: Log current item
                log.debug("Processing product: {}", productItem.getValue().getProductName());

                // Reset style
                productItem.getValue().setStyle("");

                if (!productItem.getChildren().isEmpty()) {
                    processParentItem(productItem, alertStyle, warningStyle);
                } else {
                    processSingleItem(productItem, alertStyle, warningStyle);
                }
            } catch (Exception e) {
                log.error("Error processing product item", e);
            }
        }
    }

    private void processParentItem(TreeItem<SellDataTable> productItem, String alertStyle, String warningStyle) {
        boolean hasAlert = false;
        boolean hasWarning = false;

        for (TreeItem<SellDataTable> warehouseItem : productItem.getChildren()) {
            try {
                if (warehouseItem == null || warehouseItem.getValue() == null) {
                    continue;
                }

                warehouseItem.getValue().setStyle("");
                Inventory inventory = getInventoryFromTreeItem(warehouseItem);

                if (inventory != null) {
                    if (shouldShowAlert(inventory)) {
                        hasAlert = true;
                        warehouseItem.getValue().setStyle(alertStyle);
                        log.debug("Applied alert style to warehouse: {}", warehouseItem.getValue().getWarehouseName());
                    } else if (shouldShowWarning(inventory)) {
                        hasWarning = true;
                        warehouseItem.getValue().setStyle(warningStyle);
                        log.debug("Applied warning style to warehouse: {}", warehouseItem.getValue().getWarehouseName());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing warehouse item", e);
            }
        }

        if (hasAlert) {
            productItem.getValue().setStyle(alertStyle);
            log.debug("Applied alert style to product: {}", productItem.getValue().getProductName());
        } else if (hasWarning) {
            productItem.getValue().setStyle(warningStyle);
            log.debug("Applied warning style to product: {}", productItem.getValue().getProductName());
        }
    }

    private void processSingleItem(TreeItem<SellDataTable> item, String alertStyle, String warningStyle) {
        try {
            Inventory inventory = getInventoryFromTreeItem(item);
            if (inventory != null) {
                if (shouldShowAlert(inventory)) {
                    item.getValue().setStyle(alertStyle);
                    log.debug("Applied alert style to single item: {}", item.getValue().getProductName());
                } else if (shouldShowWarning(inventory)) {
                    item.getValue().setStyle(warningStyle);
                    log.debug("Applied warning style to single item: {}", item.getValue().getProductName());
                }
            }
        } catch (Exception e) {
            log.error("Error processing single item", e);
        }
    }

    private boolean shouldShowAlert(Inventory inventory) {
        try {
            return inventory != null &&
                    inventory.getAmountAlert() != null &&
                    inventory.getAmount() <= inventory.getAmountAlert();
        } catch (Exception e) {
            log.error("Error checking alert condition", e);
            return false;
        }
    }

    private boolean shouldShowWarning(Inventory inventory) {
        try {
            return inventory != null &&
                    inventory.getAmountWarning() != null &&
                    inventory.getAmount() <= inventory.getAmountWarning() &&
                    inventory.getAmount() > (inventory.getAmountAlert() != null ? inventory.getAmountAlert() : Integer.MIN_VALUE);
        } catch (Exception e) {
            log.error("Error checking warning condition", e);
            return false;
        }
    }

    private Inventory getInventoryFromTreeItem(TreeItem<SellDataTable> item) {
        try {
            if (item == null || item.getValue() == null) return null;

            String productName;
            String warehouseName = item.getValue().getWarehouseName();

            // 1. Si es nodo hijo (warehouse): obtener productName del padre
            if (item.getParent() != null && item.getParent() != ttvInventory.getRoot()) {
                productName = item.getParent().getValue().getProductName();
            }
            // 2. Si es nodo padre (producto): no necesita warehouseName
            else {
                productName = item.getValue().getProductName();
                warehouseName = null; // ← Ignora warehouseName para nodos padres
            }

            Product product = productService.getByProductNameAndClient(productName, client);
            if (product == null) {
                log.warn("Product not found: {}", productName);
                return null;
            }

            // Solo busca warehouse si es un nodo hoja (warehouse)
            Warehouse warehouse = (warehouseName != null) ?
                    warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client) : null;

            return (warehouse != null) ?
                    inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client) : null;
        } catch (Exception e) {
            log.error("Error getting inventory from tree item", e);
            return null;
        }
    }


    /**
     * Display warning manager.
     *
     * @throws SceneSwitcher.WindowLoadException the window load exception
     */
    @FXML
    public void displayWarningManager() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow(
                "Administrador de Alertas", "/images/RTS_logo.png", "/views/warningManagerView.fxml"
        );
    }

// --- Métodos auxiliares ---

    private record FilterCriteria(
            String productNameFilter,
            String warehouseNameFilter,
            Integer minAmount,
            Integer maxAmount,
            Double minPrice,
            Double maxPrice
    ) {
    }

    private FilterCriteria getFilterCriteria() {
        return new FilterCriteria(
                tfFilterProductName.getText().trim().toLowerCase(),
                tfFilterWarehouseName.getText().trim().toLowerCase(),
                tfMinFilterAmount.getText().isEmpty() ? null : parseDataTypes.parseInt(tfMinFilterAmount.getText()),
                tfMaxFilterAmount.getText().isEmpty() ? null : parseDataTypes.parseInt(tfMaxFilterAmount.getText()),
                tfMinFilterPrice.getText().isEmpty() ? null : parseDataTypes.parseDouble(tfMinFilterPrice.getText()),
                tfMaxFilterPrice.getText().isEmpty() ? null : parseDataTypes.parseDouble(tfMaxFilterPrice.getText())
        );
    }

    private List<Inventory> getInventories() {
        List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
        return inventories != null ? inventories : Collections.emptyList();
    }

    private TreeItem<SellDataTable> processInventories(List<Inventory> inventories, FilterCriteria filters) {
        originalItems.clear();
        TreeItem<SellDataTable> root = new TreeItem<>(new SellDataTable());

        Map<Product, List<Inventory>> inventoriesByProduct = groupAndFilterInventories(inventories, filters);

        inventoriesByProduct.forEach((product, invList) -> {
            List<Inventory> filteredInvList = filterByWarehouseAndAmount(invList, filters);

            if (!filteredInvList.isEmpty()) {
                addToTree(root, product, filteredInvList);
            }
        });

        return root;
    }

    private Map<Product, List<Inventory>> groupAndFilterInventories(List<Inventory> inventories, FilterCriteria filters) {
        return inventories.stream()
                .filter(inv -> inv != null && inv.getProduct() != null && inv.getWarehouse() != null)
                .filter(inv -> matchesProductFilters(inv.getProduct(), filters))
                .collect(Collectors.groupingBy(Inventory::getProduct));
    }

    private boolean matchesProductFilters(Product product, FilterCriteria filters) {
        if (!filters.productNameFilter().isEmpty() &&
                !product.getProductName().toLowerCase().contains(filters.productNameFilter())) {
            return false;
        }
        if (filters.minPrice() != null && (product.getSellPrice() == null || product.getSellPrice() < filters.minPrice())) {
            return false;
        }
        return filters.maxPrice() == null || (product.getSellPrice() != null && product.getSellPrice() <= filters.maxPrice());
    }

    private List<Inventory> filterByWarehouseAndAmount(List<Inventory> invList, FilterCriteria filters) {
        return invList.stream()
                .filter(inv -> {
                    if (!filters.warehouseNameFilter().isEmpty() &&
                            !inv.getWarehouse().getWarehouseName().toLowerCase().contains(filters.warehouseNameFilter())) {
                        return false;
                    }
                    if (filters.minAmount() != null && inv.getAmount() < filters.minAmount()) {
                        return false;
                    }
                    return filters.maxAmount() == null || inv.getAmount() <= filters.maxAmount();
                })
                .toList();
    }

    private void addToTree(TreeItem<SellDataTable> root, Product product, List<Inventory> filteredInvList) {
        String currency = product.getCurrency() != null ? product.getCurrency().getCurrencyName() : "";
        int totalAmount = filteredInvList.stream().mapToInt(Inventory::getAmount).sum();

        if (filteredInvList.size() > 1) {
            TreeItem<SellDataTable> productItem = createProductNode(product, currency, filteredInvList.size(), totalAmount);
            filteredInvList.forEach(inv -> productItem.getChildren().add(createWarehouseNode(inv)));
            root.getChildren().add(productItem);
            originalItems.add(productItem);
        } else {
            TreeItem<SellDataTable> singleNode = createSingleNode(product, currency, filteredInvList.getFirst(), totalAmount);
            root.getChildren().add(singleNode);
            originalItems.add(singleNode);
        }
    }

    private TreeItem<SellDataTable> createProductNode(Product product, String currency, int warehouseCount, int totalAmount) {
        return new TreeItem<>(
                new SellDataTable(
                        product.getProductName(),
                        product.getSellPrice(),
                        currency,
                        "Almacenes: " + warehouseCount,
                        totalAmount
                )
        );
    }

    private TreeItem<SellDataTable> createWarehouseNode(Inventory inv) {
        return new TreeItem<>(
                new SellDataTable(
                        "",
                        null,
                        null,
                        inv.getWarehouse().getWarehouseName(),
                        inv.getAmount()
                )
        );
    }

    private TreeItem<SellDataTable> createSingleNode(Product product, String currency, Inventory inv, int totalAmount) {
        return new TreeItem<>(
                new SellDataTable(
                        product.getProductName(),
                        product.getSellPrice(),
                        currency,
                        inv.getWarehouse().getWarehouseName(),
                        totalAmount
                )
        );
    }

    private void displayResults(TreeItem<SellDataTable> root) {
        ttvInventory.setRoot(root);
        ttvInventory.setShowRoot(false);
    }

    private void handleLoadError(Exception e) {
        displayAlerts.showAlert("Error al cargar inventario");
        e.printStackTrace();
    }

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
        tfSellProductAmount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                updatePricePerAmount();
            }
        });
    }

    private void updatePricePerAmount() {
        try {
            // Validar que el campo de producto no esté vacío
            if (tfSellProductName.getText() == null || tfSellProductName.getText().isEmpty()) {
                return;
            }

            // Validar que el campo de cantidad no esté vacío
            String amountText = tfSellProductAmount.getText();
            if (amountText == null || amountText.isEmpty()) {
                return;
            }

            // Obtener el producto y su precio
            Product product = productService.getByProductNameAndClient(
                    tfSellProductName.getText(),
                    client
            );

            if (product == null || product.getSellPrice() == null) {
                System.out.println("No hay precios para este producto en la base de datos");
                return;
            }

            // Calcular el precio total
            double price = product.getSellPrice();
            double amount = Double.parseDouble(amountText);
            double pricePerAmount = amount * price;

            // Actualizar el campo de precio
            tfSellProductPrice.setText(String.format("%.2f", pricePerAmount));

        } catch (NumberFormatException e) {
            System.out.println("La cantidad debe ser un número válido");
        } catch (Exception e) {
            System.out.println("Error al calcular el precio: " + e.getMessage());
        }
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
            item.setOnAction(e -> tf.setText(item.getText()));
            mb.getItems().add(item);
        }
    }
}
