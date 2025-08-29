package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.SellDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.service.WarehouseService;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.*;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private final CurrencyServiceImpl currencyService;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final CleanHelper cleanHelper;
    private final SellFieldsValidator sellFieldsValidator;
    private final SellFilterUtilities sellFilterUtilities;

    @FXML
    private Label lblWarning, lblAlerts, lblSellDebug, lblAssignPriceDebug;

    public SellViewController(
            CurrencyServiceImpl currencyService, DisplayAlerts displayAlerts, SellFieldsValidator sellFieldsValidator,
            UserLogged userLogged, ParseDataTypes parseDataTypes, SceneSwitcher sceneSwitcher,
            InventoryServiceImpl inventoryService, ProductServiceImpl productService, WarehouseService warehouseService,
            SellRegistryServiceImpl sellRegistryService, GeneralRegistryServiceImpl generalRegistryService,
            CleanHelper cleanHelper, SellFilterUtilities sellFilterUtilities
    ) {
        this.inventoryService = inventoryService;
        this.sellFilterUtilities = sellFilterUtilities;
        this.sellFieldsValidator = sellFieldsValidator;
        this.generalRegistryService = generalRegistryService;
        this.sellRegistryService = sellRegistryService;
        this.displayAlerts = displayAlerts;
        this.cleanHelper = cleanHelper;
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
    private TreeTableColumn<SellDataTable, String> ttcWarehouse, ttcProductName, ttcSellPrice;
    @FXML
    private TreeTableColumn<SellDataTable, Integer> ttcProductAmount;

    private final ObservableList<TreeItem<SellDataTable>> originalItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());

        Platform.runLater(() -> {
            setupTableColumns();
            loadProductTable();
            setupFilterListeners();
            setupMbListeners();
            initDatePicker();
            initMbWarehouse();
            initAllMbCurrency();
            setupTableSelectionListener();
            setupOtherTextFieldListeners();
            loadWarningAndAlertLabels();
        });
    }

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

    @FXML
    public void cleanFilters() {
        List<TextField> textFields = List.of(
                tfFilterProductName, tfFilterWarehouseName, tfMinFilterAmount, tfMinFilterPrice, tfMaxFilterAmount,
                tfMaxFilterPrice
        );
        cleanHelper.cleanTextFields(textFields);

        loadProductTable();
    }

    @FXML
    public void cleanForm() {
        List<TextField> textFields = List.of(
                tfAssignPriceCurrency, tfAssignPriceProductName, tfAssignPriceProductPrice, tfSellProductAmount,
                tfSellProductCurrency, tfSellProductName, tfSellProductPrice
        );
        cleanHelper.cleanTextFields(textFields);
        dpSellProductDate.setValue(LocalDate.now());
        lblSellDebug.setText("El precio de venta es el de toda la venta, NO PRECIOS INDIVIDUALES");
    }


    @FXML
    public void sellProduct() {
        if (!sellFieldsValidator.validateAllSellFields(
                tfSellProductName.getText(), tfSellWarehouse.getText(), tfSellProductAmount.getText(),
                dpSellProductDate.getValue(), tfSellProductCurrency.getText(), tfSellProductPrice.getText(), client
        )) {
            return;
        }

        try {
            // 1. Obtener datos básicos
            String warehouseName = tfSellWarehouse.getText();
            String productName = tfSellProductName.getText();
            int productAmount = parseDataTypes.parseInt(tfSellProductAmount.getText());

            // 2. Obtener entidades y validar stock
            Inventory inventory = inventoryService.getAndValidateInventory(warehouseName, productName, productAmount, client);

            // 3. Procesar venta
            sellRegistryService.processSale(
                    inventory, productAmount, productName, tfSellProductPrice.getText(), tfSellProductCurrency.getText(),
                    dpSellProductDate.getValue(), tfSellWarehouse.getText(), client
            );

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

    private void updateUIAfterSale(Inventory inventory) {
        loadProductTable();
        loadWarningAndAlertLabels();
        cleanForm();

        int remainingStock = inventory.getAmount();
        String message = "";
        String color = "black";

        if (remainingStock > 0) {
            message = "Venta registrada. Stock restante: " + remainingStock;
            color = "#16a34a"; // green
        } else if (remainingStock == 0) {
            message = "Venta registrada. Producto AGOTADO";
            color = "#dc2626"; // red
        }

        lblSellDebug.setText(message);
        lblSellDebug.setStyle("-fx-text-fill:" + color + "; -fx-font-weight: bold;");
    }

    @FXML
    public void assignProductPrice() {
        if (!sellFieldsValidator.validateAllAssignPriceFields(
                tfAssignPriceProductName.getText(),
                tfAssignPriceCurrency.getText(),
                tfAssignPriceProductPrice.getText(),
                client
        )) {
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
                String warehouseName = selectedData.getWarehouseName();
                if (isParentWithChildren) {
                    warehouseName = "";
                }

                // Populate form fields
                tfAssignPriceProductName.setText(productName);
                tfAssignPriceCurrency.setText(currency);
                tfAssignPriceProductPrice.setText(price);
                tfSellWarehouse.setText(warehouseName); // Solo se llena si es un nodo hijo (almacén)
                tfSellProductAmount.setText(1 + "");
                tfSellProductName.setText(productName);
                tfSellProductPrice.setText(price);
                tfSellProductCurrency.setText(currency);
                lblSellDebug.setText("El precio de venta es el de toda la venta, NO PRECIOS INDIVIDUALES");
            }
        });
    }


    @FXML
    private void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    @FXML
    private void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    private void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    @FXML
    private void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    @FXML
    private void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    private void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    private void setupTableColumns() {
        // Configurar cada columna con su tipo específico
        configureStringColumn(ttcWarehouse, "warehouseName");
        configureStringColumn(ttcProductName, "productName");
        configureStringColumn(ttcSellPrice, "sellPriceAndCurrency"); // Asumo que el campo se llama "sellPrice" y no "formattedPrice"
        configureIntegerColumn(ttcProductAmount, "productAmount");

        ttvInventory.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                Platform.runLater(() -> {
                    applyStockWarningStyles(ttvInventory.getRoot());
                    ttvInventory.refresh();
                });
            }
        });
    }


    private void configureStringColumn(TreeTableColumn<SellDataTable, String> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    updateStyle();
                }
            }

            private void updateStyle() {
                TreeItem<SellDataTable> treeItem = getTableRow().getTreeItem();
                if (treeItem != null && treeItem.getValue() != null) {
                    setStyle(treeItem.getValue().getStyle());
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configureDoubleColumn(TreeTableColumn<SellDataTable, Double> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }

                updateStyle();
            }

            private void updateStyle() {
                TreeItem<SellDataTable> treeItem = getTableRow().getTreeItem();
                if (treeItem != null && treeItem.getValue() != null) {
                    setStyle(treeItem.getValue().getStyle());
                } else {
                    setStyle("");
                }
            }
        });
        column.setSortable(false);
    }

    // Aplicar el mismo patrón a configureIntegerColumn
    private void configureIntegerColumn(TreeTableColumn<SellDataTable, Integer> column, String property) {
        column.setCellValueFactory(new TreeItemPropertyValueFactory<>(property));
        column.setCellFactory(col -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);

                } else {
                    setText(item.toString());
                }
                updateStyle();
            }

            private void updateStyle() {
                TreeItem<SellDataTable> treeItem = getTableRow().getTreeItem();
                if (treeItem != null && treeItem.getValue() != null) {
                    setStyle(treeItem.getValue().getStyle());
                } else {
                    setStyle("");
                }
            }
        });
        column.setSortable(false);
    }

    public void loadProductTable() {
        try {
            // 1. Get filters
            SellFilterCriteria filters = sellFilterUtilities.getFilterCriteria(
                    tfFilterProductName.getText(), tfFilterWarehouseName.getText(), tfMinFilterAmount.getText(),
                    tfMaxFilterAmount.getText(), tfMinFilterPrice.getText(), tfMaxFilterPrice.getText()
            );

            // 2. Get and process data
            List<Inventory> inventories = inventoryService.getInventories(client);
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

            loadWarningAndAlertLabels();
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

    private void applyStockWarningStyles(TreeItem<SellDataTable> root) {
        if (root == null || root.getChildren() == null) return;

        final String alertStyle = "-fx-background-color: #ff9b9b;";
        final String warningStyle = "-fx-background-color: #ffc445;";

        for (TreeItem<SellDataTable> item : root.getChildren()) {
            if (item.getChildren().isEmpty()) {
                // Es un nodo hoja (item individual)
                applyStyleToSingleItem(item, alertStyle, warningStyle);
            } else {
                // Es un nodo padre con hijos
                processParentItemWithChildren(item, alertStyle, warningStyle);
            }
        }
        Platform.runLater(() -> ttvInventory.refresh());
    }

    private void processParentItemWithChildren(TreeItem<SellDataTable> parentItem, String alertStyle, String warningStyle) {
        boolean hasAlert = false;
        boolean hasWarning = false;

        for (TreeItem<SellDataTable> child : parentItem.getChildren()) {
            applyStyleToSingleItem(child, alertStyle, warningStyle);

            Inventory inventory = getInventoryFromTreeItem(child);
            if (inventory != null) {
                if (inventoryService.shouldShowAlert(inventory)) {
                    hasAlert = true;
                } else if (inventoryService.shouldShowWarning(inventory)) {
                    hasWarning = true;
                }
            }
        }

        // Aplicar estilo al padre basado en el estado de los hijos
        if (hasAlert) {
            parentItem.getValue().setStyle(alertStyle);
        } else if (hasWarning) {
            parentItem.getValue().setStyle(warningStyle);
        } else {
            parentItem.getValue().setStyle("");
        }
    }

    private void applyStyleToSingleItem(TreeItem<SellDataTable> item, String alertStyle, String warningStyle) {
        Inventory inventory = getInventoryFromTreeItem(item);
        if (inventory != null) {
            if (inventoryService.shouldShowAlert(inventory)) {
                item.getValue().setStyle(alertStyle);
            } else if (inventoryService.shouldShowWarning(inventory)) {
                item.getValue().setStyle(warningStyle);
            } else {
                item.getValue().setStyle(""); // Limpiar estilo si no aplica
            }
        } else {
            // Manejar casos donde no se encuentra el inventory
            item.getValue().setStyle("");
        }
    }

    private Inventory getInventoryFromTreeItem(TreeItem<SellDataTable> item) {
        try {
            if (item == null || item.getValue() == null) return null;

            String productName;
            String warehouseName;

            // Determinar si es nodo hijo o individual
            if (item.getParent() != null && item.getParent() != ttvInventory.getRoot()) {
                productName = item.getParent().getValue().getProductName();
                warehouseName = item.getValue().getWarehouseName();
            } else {
                productName = item.getValue().getProductName();
                warehouseName = item.getValue().getWarehouseName();

                // Solo ignorar si es un nodo de resumen (multiple almacenes)
                if (warehouseName != null && warehouseName.startsWith("Almacenes:")) {
                    warehouseName = null;
                }
            }

            if (productName == null) return null;

            Product product = productService.getByProductNameAndClient(productName, client);
            if (product == null) return null;

            if (warehouseName == null || warehouseName.trim().isEmpty()) {
                return null; // No hay warehouse específico
            }

            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
            if (warehouse == null) return null;

            return inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
        } catch (Exception e) {
            log.error("Error getting inventory from tree item", e);
            return null;
        }
    }


    @FXML
    public void displayWarningManager() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow(
                "Administrador de Alertas", "/images/lc_logo.png", "/views/warningManagerView.fxml"
        );
    }

    @FXML
    public void loadMbSellProduct() {
        if (warehouseService.existsByWarehouseNameAndClient(tfSellWarehouse.getText(), client)) {
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfSellWarehouse.getText(), client);
            initMbSellProduct(warehouse);
        }
    }


    private TreeItem<SellDataTable> processInventories(List<Inventory> inventories, SellFilterCriteria filters) {
        originalItems.clear();
        TreeItem<SellDataTable> root = new TreeItem<>(new SellDataTable());

        Map<Product, List<Inventory>> inventoriesByProduct = sellFilterUtilities.groupAndFilterInventories(inventories, filters);

        // Ordenar productos alfabéticamente por nombre
        List<Product> sortedProducts = inventoriesByProduct.keySet().stream()
                .sorted(Comparator.comparing(Product::getProductName)).toList();

        for (Product product : sortedProducts) {
            List<Inventory> invList = inventoriesByProduct.get(product);
            List<Inventory> filteredInvList = sellFilterUtilities.filterByWarehouseAndAmount(invList, filters);

            if (!filteredInvList.isEmpty()) {
                // Crear una copia mutable de la lista para poder ordenarla
                List<Inventory> mutableList = new ArrayList<>(filteredInvList);

                // Ordenar inventarios alfabéticamente por nombre de almacén
                mutableList.sort(Comparator.comparing(inv ->
                        inv.getWarehouse().getWarehouseName().toLowerCase()));

                addToTree(root, product, mutableList);
            }
        }

        return root;
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
        String priceString = (product.getSellPrice() != null) ?
                product.getSellPrice() + " " + currency : "";

        return new TreeItem<>(
                new SellDataTable(
                        product.getProductName(),
                        priceString,  // Usar string formateado
                        "Almacenes: " + warehouseCount,
                        totalAmount
                )
        );
    }

    private TreeItem<SellDataTable> createSingleNode(Product product, String currency, Inventory inv, int totalAmount) {
        String priceString = (product.getSellPrice() != null) ?
                product.getSellPrice() + " " + currency : "";

        return new TreeItem<>(
                new SellDataTable(
                        product.getProductName(),
                        priceString,  // Usar string formateado
                        inv.getWarehouse().getWarehouseName(),
                        totalAmount
                )
        );
    }

    private TreeItem<SellDataTable> createWarehouseNode(Inventory inv) {
        return new TreeItem<>(
                new SellDataTable(
                        "",
                        "",
                        inv.getWarehouse().getWarehouseName(),
                        inv.getAmount()
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

    private void setupMbListeners() {
        mbSellProduct.onActionProperty().addListener((obs, oldVal, newVal)
                -> initMbSellProduct(warehouseService.getWarehouseByWarehouseNameAndClient(tfSellWarehouse.getText(), client)));
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
        List<Warehouse> warehouses = warehouseService.getWarehousesByClient(client);

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

    @FXML
    public void displayLastSells() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow(
                "Ventas Realizadas", "/images/lc_logo.png", "/views/realizedSells.fxml"
        );
    }
}
