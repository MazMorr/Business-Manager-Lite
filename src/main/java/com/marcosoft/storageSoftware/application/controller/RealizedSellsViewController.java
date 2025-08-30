package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.RealizedSellsDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.CleanHelper;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SellFieldsValidator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RealizedSellsViewController {
    private ObservableList<RealizedSellsDataTable> realizedSellsDataTableList;
    private Client client;

    private final SellFieldsValidator sellFieldsValidator;
    private final CleanHelper cleanHelper;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final UserLogged userLogged;
    private final DisplayAlerts displayAlerts;
    private final InventoryServiceImpl inventoryService;
    private final WarehouseServiceImpl warehouseService;
    private final ProductServiceImpl productService;
    private final ParseDataTypes parseDataTypes;
    private final SellViewController sellViewController;
    private final CurrencyServiceImpl currencyService;
    @FXML
    private DatePicker dpFilterDate;

    public RealizedSellsViewController(
            SellFieldsValidator sellFieldsValidator, CleanHelper cleanHelper, SellRegistryServiceImpl sellRegistryService,
            GeneralRegistryServiceImpl generalRegistryService, UserLogged userLogged, DisplayAlerts displayAlerts,
            InventoryServiceImpl inventoryService, WarehouseServiceImpl warehouseService, ParseDataTypes parseDataTypes,
            ProductServiceImpl productService, SellViewController sellViewController, CurrencyServiceImpl currencyService
    ) {
        this.sellFieldsValidator = sellFieldsValidator;
        this.cleanHelper = cleanHelper;
        this.sellRegistryService = sellRegistryService;
        this.generalRegistryService = generalRegistryService;
        this.userLogged = userLogged;
        this.displayAlerts = displayAlerts;
        this.inventoryService = inventoryService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.parseDataTypes = parseDataTypes;
        this.sellViewController = sellViewController;
        this.currencyService = currencyService;
    }

    @FXML
    private MenuButton mbId, mbCurrency;
    @FXML
    private TextField tfProduct, tfProductCurrency, tfProductPrice, tfId, tfWarehouse, tfProductAmount;
    @FXML
    private DatePicker dpSellProductDate;

    @FXML
    private TableView<RealizedSellsDataTable> tvSells;
    @FXML
    private TableColumn<RealizedSellsDataTable, Integer> tcAmount;
    @FXML
    private TableColumn<RealizedSellsDataTable, LocalDate> tcDate;
    @FXML
    private TableColumn<RealizedSellsDataTable, Long> tcId;
    @FXML
    private TableColumn<RealizedSellsDataTable, String> tcProduct, tcWarehouse, tcSellPriceAndCurrency;

    @FXML
    void initialize() {
        client = userLogged.getClient();
        Platform.runLater(() -> {
            initTableColumns();
            setupListeners();
            initMbId();
            initMbCurrency(mbCurrency, tfProductCurrency);
        });

        // Listener para selección de fila en la tabla
        tvSells.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadSellProduct();
            }
        });
    }

    private void setupListeners() {
        tfId.textProperty().addListener((obs, oldVal, newVal) -> searchSell());
        dpFilterDate.valueProperty().addListener((obs, oldVal, newVal) -> refreshTableData());
    }

    private void searchSell() {
        String idText = tfId.getText();
        if (idText == null || idText.trim().isEmpty()) {
            clearFieldsExceptId();
            return;
        }

        try {
            Long id = parseDataTypes.parseLong(idText);
            if (sellRegistryService.existsByIdAndClient(id, client)) {
                SellRegistry sellRegistry = sellRegistryService.getByIdAndClient(id, client);
                tfProduct.setText(sellRegistry.getProductName());
                tfWarehouse.setText(sellRegistry.getWarehouseName());
                tfProductAmount.setText(sellRegistry.getProductAmount() + "");
                tfProductCurrency.setText(sellRegistry.getSellCurrency());
                tfProductPrice.setText(sellRegistry.getSellPrice() + "");
                dpSellProductDate.setValue(sellRegistry.getSellDate());
            } else {
                clearFieldsExceptId();
            }
        } catch (NumberFormatException e) {
            clearFieldsExceptId();
        }

    }

    private void clearFieldsExceptId() {
        List<TextField> textFields = List.of(tfProduct, tfWarehouse, tfProductAmount, tfProductCurrency, tfProductPrice);
        cleanHelper.cleanTextFields(textFields);
        dpSellProductDate.setValue(null);
    }

    private void initTableColumns() {
        realizedSellsDataTableList = FXCollections.observableArrayList();
        refreshTableData();

        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("productAmount"));
        tcSellPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("sellPriceAndCurrency"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("sellDate"));

        tvSells.setItems(realizedSellsDataTableList);
    }

    private void refreshTableData() {
        realizedSellsDataTableList.clear();
        List<SellRegistry> sellRegistryList = sellRegistryService.getAllSellRegistriesByClient(client);


        // Ordenar la lista por fecha (más recientes primero)
        sellRegistryList.sort((s1, s2) -> s2.getSellDate().compareTo(s1.getSellDate()));

        if (dpFilterDate.getValue() != null) {
            for (SellRegistry sell : sellRegistryList) {
                if (sell.getSellDate().equals(dpFilterDate.getValue())) {
                    realizedSellsDataTableList.add(new RealizedSellsDataTable(
                            sell.getId(),
                            sell.getWarehouseName(),
                            sell.getProductName(),
                            sell.getProductAmount(),
                            sell.getSellPrice() + " " + sell.getSellCurrency(),
                            sell.getSellDate()
                    ));
                }
            }
        } else {
            for (SellRegistry sell : sellRegistryList) {
                realizedSellsDataTableList.add(new RealizedSellsDataTable(
                        sell.getId(),
                        sell.getWarehouseName(),
                        sell.getProductName(),
                        sell.getProductAmount(),
                        sell.getSellPrice() + " " + sell.getSellCurrency(),
                        sell.getSellDate()
                ));
            }
        }

    }

    @FXML
    public void updateSell() {
        // 1. Validación básica de formato de campos (sin validar existencias)
        if (!validateAllTextFieldsForUpdate()) {
            return;
        }

        // 2. Obtener datos de los campos
        Long id = Long.parseLong(tfId.getText());
        String productName = tfProduct.getText().trim();
        String warehouseName = tfWarehouse.getText().trim();
        int newAmount = Integer.parseInt(tfProductAmount.getText().trim());
        String currency = tfProductCurrency.getText().trim();
        double price = Double.parseDouble(tfProductPrice.getText().replace(",", ".").trim());
        LocalDate sellDate = dpSellProductDate.getValue();

        // 3. Obtener venta original
        SellRegistry originalSell = sellRegistryService.getByIdAndClient(id, client);
        if (originalSell == null) {
            displayAlerts.showAlert("No se encontró el registro de venta a actualizar.");
            return;
        }

        // 4. Verificar si hay cambios
        if (!hasChanges(originalSell, productName, warehouseName, newAmount, currency, price, sellDate)) {
            displayAlerts.showAlert("Debe modificar al menos un campo para actualizar la venta.");
            return;
        }

        // 5. Determinar qué cambió
        boolean productOrWarehouseChanged = !productName.equals(originalSell.getProductName())
                || !warehouseName.equals(originalSell.getWarehouseName());
        boolean amountChanged = newAmount != originalSell.getProductAmount();

        try {
            // 6. Manejar cambios en el inventario
            if (productOrWarehouseChanged) {
                handleProductOrWarehouseChange(originalSell, productName, warehouseName, newAmount);
            } else if (amountChanged) {
                handleAmountChange(originalSell, newAmount);
            }

            // 7. Actualizar registro de venta
            updateSellRecord(originalSell, productName, warehouseName, newAmount, currency, price, sellDate);

            GeneralRegistry generalRegistry = new GeneralRegistry(
                    null, client, "Ventas", "Corrección de Venta para: ID " + id, LocalDateTime.now()
            );
            generalRegistryService.save(generalRegistry);

            displayAlerts.showAlert("Venta actualizada correctamente.");
            refreshTableData();
            clearFields();
            sellViewController.loadProductTable();
        } catch (IllegalStateException e) {
            displayAlerts.showAlert(e.getMessage());
        }
    }

    private boolean validateAllTextFieldsForUpdate() {
        String idText = tfId.getText();
        long id;
        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El ID debe ser un número válido");
            return false;
        }

        return sellFieldsValidator.validateAllSellFieldsForUpdate(
                tfProduct.getText(), tfWarehouse.getText(), tfProductAmount.getText(), dpSellProductDate.getValue(),
                tfProductCurrency.getText(), tfProductPrice.getText(), client)
                && sellFieldsValidator.validateId(id, client);
    }

    private boolean hasChanges(SellRegistry original, String productName, String warehouseName,
                               int newAmount, String currency, double price, LocalDate date) {
        return !productName.equals(original.getProductName())
                || !warehouseName.equals(original.getWarehouseName())
                || newAmount != original.getProductAmount()
                || !currency.equals(original.getSellCurrency())
                || Double.compare(price, original.getSellPrice()) != 0
                || !date.equals(original.getSellDate());
    }

    // Métodos auxiliares para mejor organización
    private void handleProductOrWarehouseChange(SellRegistry originalSell, String newProductName,
                                                String newWarehouseName, int newAmount) {

        Product newProduct = productService.getByProductNameAndClient(newProductName, client);
        Warehouse newWarehouse = warehouseService.getWarehouseByWarehouseNameAndClient(newWarehouseName, client);
        Inventory newInventory = inventoryService.getByProductAndWarehouseAndClient(newProduct, newWarehouse, client);

        if (newInventory == null) {
            throw new IllegalStateException("No existe inventario para el nuevo producto/almacén seleccionado.");
        }

        if (newInventory.getAmount() < newAmount) {
            throw new IllegalStateException("No hay suficientes existencias en el nuevo almacén seleccionado.");
        }

        // Devolver al inventario anterior
        Product oldProduct = productService.getByProductNameAndClient(originalSell.getProductName(), client);
        Warehouse oldWarehouse = warehouseService.getWarehouseByWarehouseNameAndClient(originalSell.getWarehouseName(), client);
        Inventory oldInventory = inventoryService.getByProductAndWarehouseAndClient(oldProduct, oldWarehouse, client);

        oldInventory.setAmount(oldInventory.getAmount() + originalSell.getProductAmount());
        inventoryService.save(oldInventory);

        newInventory.setAmount(newInventory.getAmount() - newAmount);
        inventoryService.save(newInventory);
    }

    private void handleAmountChange(SellRegistry originalSell, int newAmount) {
        Product product = productService.getByProductNameAndClient(originalSell.getProductName(), client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(originalSell.getWarehouseName(), client);
        Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

        int available = inventory.getAmount() + originalSell.getProductAmount();
        if (newAmount > available) {
            throw new IllegalStateException("No hay suficientes existencias disponibles.");
        }

        inventory.setAmount(available - newAmount);
        inventoryService.save(inventory);
    }

    private void updateSellRecord(SellRegistry sell, String productName, String warehouseName,
                                  int amount, String currency, double price, LocalDate date) {
        sell.setRegistryType("Correción Venta: ID " + sell.getId());
        sell.setProductName(productName);
        sell.setWarehouseName(warehouseName);
        sell.setProductAmount(amount);
        sell.setSellCurrency(currency);
        sell.setSellPrice(price);
        sell.setSellDate(date);
        sellRegistryService.save(sell);
    }

    public void loadSellProduct() {
        // Aquí puedes cargar los datos de una venta seleccionada en la tabla
        RealizedSellsDataTable selected = tvSells.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SellRegistry sellRegistry = sellRegistryService.getByIdAndClient(selected.getId(), client);
            tfId.setText(sellRegistry.getId() + "");
            tfProduct.setText(sellRegistry.getProductName());
            tfWarehouse.setText(sellRegistry.getWarehouseName());
            tfProductAmount.setText(sellRegistry.getProductAmount() + "");
            tfProductCurrency.setText(sellRegistry.getSellCurrency());
            tfProductPrice.setText(sellRegistry.getSellPrice() + "");
            dpSellProductDate.setValue(sellRegistry.getSellDate());
        }
    }

    @FXML
    public void goOut() {
        Stage stage = (Stage) mbId.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        List<TextField> textFields = List.of(
                tfId, tfProduct, tfWarehouse, tfProductAmount, tfProductCurrency, tfProductPrice
        );
        cleanHelper.cleanTextFields(textFields);
        dpSellProductDate.setValue(null);
    }

    private void initMbId() {
        mbId.getItems().clear();
        List<SellRegistry> sellRegistryList = sellRegistryService.getAllSellRegistriesByClient(client);
        for (SellRegistry sellRegistry : sellRegistryList) {
            MenuItem item = new MenuItem(sellRegistry.getId() + "");
            item.setOnAction(e -> {
                tfId.setText(sellRegistry.getId() + "");
                tfProduct.setText(sellRegistry.getProductName());
                tfProductAmount.setText(sellRegistry.getProductAmount() + "");
                tfProductCurrency.setText(sellRegistry.getSellCurrency());
                tfProductPrice.setText(sellRegistry.getSellPrice() + "");
                dpSellProductDate.setValue(sellRegistry.getSellDate());
            });
            mbId.getItems().add(item);
        }
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
    public void cleanFilters() {
        dpFilterDate.setValue(null);
    }
}
