package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.BuyDataTable;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class BuyViewController {
    // Variable de instancia para controlar eventos circulares
    private boolean isCalculating = false;
    private ObservableList<BuyDataTable> buyList;
    private Client client;
    private String registryZone;

    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;
    private final ParseDataTypes parseDataTypes;
    private final BuyAndExpenseSharedMethods buyAndExpenseSharedMethods;
    private final DisplayAlerts displayAlerts;
    private final BuyServiceImpl buyService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final BuyRegistryServiceImpl buyRegistryService;
    private final ProductServiceImpl productService;
    private final CleanHelper cleanHelper;
    private final InventoryServiceImpl inventoryService;

    public BuyViewController(
            UserLogged userLogged, SceneSwitcher sceneSwitcher, CurrencyServiceImpl currencyService, BuyServiceImpl buyService,
            ParseDataTypes parseDataTypes, BuyAndExpenseSharedMethods buyAndExpenseSharedMethods, DisplayAlerts displayAlerts,
            GeneralRegistryServiceImpl generalRegistryService, BuyRegistryServiceImpl buyRegistryService,
            ProductServiceImpl productService, CleanHelper cleanHelper, InventoryServiceImpl inventoryService
    ) {
        this.userLogged = userLogged;
        this.sceneSwitcher = sceneSwitcher;
        this.currencyService = currencyService;
        this.parseDataTypes = parseDataTypes;
        this.buyAndExpenseSharedMethods = buyAndExpenseSharedMethods;
        this.displayAlerts = displayAlerts;
        this.buyService = buyService;
        this.generalRegistryService = generalRegistryService;
        this.buyRegistryService = buyRegistryService;
        this.productService = productService;
        this.cleanHelper = cleanHelper;
        this.inventoryService = inventoryService;
    }

    @FXML
    private Label lblClientName, lblAddDebugForm;
    @FXML
    private TableView<BuyDataTable> tvBuy;
    @FXML
    private TableColumn<BuyDataTable, String> tcBuyName, tcTotalPriceAndCurrency, tcUnitaryPriceAndCurrency;
    @FXML
    private TableColumn<BuyDataTable, Long> tcId;
    @FXML
    private TableColumn<BuyDataTable, Integer> tcAmount;
    @FXML
    private TableColumn<BuyDataTable, LocalDate> tcDate;

    @FXML
    private MenuButton mbTotalCurrency, mbUnitaryCurrency;

    @FXML
    private TextField tfMinFilterAmount, tfAddTotalBuyCurrency, tfFilterId, tfFilterName, tfAddUnitaryBuyCurrency,
            tfAddBuyName, tfAddUnitaryBuyPrice, tfId, tfAddTotalBuyPrice, tfMaxFilterPrice, tfMaxFilterAmount, tfMinFilterPrice, tfAddBuyAmount;

    @FXML
    private DatePicker dpAddExpenseDate;

    @FXML
    private void initialize() {
        registryZone = "Compras";
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());

        initializeTableColumns();
        initializeTableValues();

        Platform.runLater(() -> {
            initMbs();
            cleanForm();
            setupTextFieldListeners();
            setupTableSelectionListener();
        });
    }

    private void setupTextFieldListeners() {
        tfFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());
        tfFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());
        tfMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterBuyTable());

        // Auto-calculate total price when unitary price or amount changes
        tfAddUnitaryBuyPrice.textProperty().addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        tfAddBuyAmount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        tfAddTotalBuyPrice.textProperty().addListener((obs, oldVal, newVal) -> calculateUnitaryPrice());
    }

    /**
     * Calcula el precio total basado en el precio unitario y la cantidad
     */
    private void calculateTotalPrice() {
        // Prevenir eventos circulares
        if (isCalculating) return;
        isCalculating = true;

        try {
            Double unitaryPrice = parseDataTypes.parseDouble(tfAddUnitaryBuyPrice.getText());
            Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());

            if (unitaryPrice != null && amount != null && amount > 0) {
                Double totalPrice = unitaryPrice * amount;
                tfAddTotalBuyPrice.setText(String.format("%.2f", totalPrice));
            } else {
                // Limpiar el campo si los valores no son válidos
                tfAddTotalBuyPrice.setText("");
            }
        } finally {
            isCalculating = false;
        }
    }

    /**
     * Calcula el precio unitario basado en el precio total y la cantidad
     */
    private void calculateUnitaryPrice() {
        // Prevenir eventos circulares
        if (isCalculating) return;
        isCalculating = true;

        try {
            Double totalPrice = parseDataTypes.parseDouble(tfAddTotalBuyPrice.getText());
            Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());

            if (totalPrice != null && amount != null && amount > 0) {
                Double unitaryPrice = totalPrice / amount;
                tfAddUnitaryBuyPrice.setText(String.format("%.2f", unitaryPrice));
            } else {
                // Limpiar el campo si los valores no son válidos
                tfAddUnitaryBuyPrice.setText("");
            }
        } finally {
            isCalculating = false;
        }
    }


    private void setupTableSelectionListener() {
        tvBuy.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                Buy buy = buyService.getBuyById(newSel.getId());
                tfId.setText(buy.getBuyId() != null ? String.valueOf(buy.getBuyId()) : "");
                tfAddBuyName.setText(buy.getBuyName());
                tfAddBuyAmount.setText(buy.getAmount() != null ? String.valueOf(buy.getAmount()) : "");
                tfAddUnitaryBuyPrice.setText(buy.getBuyUnitaryPrice() != null ? String.valueOf(buy.getBuyUnitaryPrice()) : "");
                tfAddTotalBuyPrice.setText(buy.getBuyTotalPrice() != null ? String.valueOf(buy.getBuyTotalPrice()) : "");
                tfAddUnitaryBuyCurrency.setText(buy.getCurrency().getCurrencyName());
                tfAddTotalBuyCurrency.setText(buy.getCurrency().getCurrencyName());
                dpAddExpenseDate.setValue(buy.getReceivedDate());
            }
        });
    }

    private void initMbs() {
        initMbCurrency(mbUnitaryCurrency);
        initMbCurrency(mbTotalCurrency);
    }

    private void initMbCurrency(MenuButton mbCurrency) {
        List<Currency> currencies = currencyService.getAllCurrencies();
        mbCurrency.getItems().clear();
        for (Currency currency : currencies) {
            MenuItem item = new MenuItem(currency.getCurrencyName());
            item.setOnAction(e -> {
                tfAddTotalBuyCurrency.setText(item.getText());
                tfAddUnitaryBuyCurrency.setText(item.getText());
            });
            mbCurrency.getItems().add(item);
        }
    }

    private void initializeTableColumns() {
        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcBuyName.setCellValueFactory(new PropertyValueFactory<>("buyName"));
        tcUnitaryPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("unitaryPriceAndCurrency"));
        tcTotalPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("totalPriceAndCurrency"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));
    }

    public void initializeTableValues() {
        buyList = FXCollections.observableArrayList();
        List<Buy> buysDB = buyService.getAllBuysByClient(client);
        buyList.clear();

        for (Buy buy : buysDB) {
            buyList.add(new BuyDataTable(
                    buy.getBuyId(),
                    buy.getBuyName(),
                    buy.getBuyUnitaryPrice() + " " + buy.getCurrency().getCurrencyName(),
                    buy.getBuyTotalPrice() + " " + buy.getCurrency().getCurrencyName(),
                    buy.getAmount(),
                    buy.getReceivedDate()
            ));
        }

        tvBuy.setItems(buyList);
        filterBuyTable();
    }

    @FXML
    public void removeBuy() {
        Long buyId = parseDataTypes.parseLong(tfId.getText());
        String registryType = "Eliminación";

        if (buyId == null) {
            displayAlerts.showAlert("Debes seleccionar un registro para eliminar.");
            return;
        }

        if (displayAlerts.showConfirmationAlert("¿Está seguro que desea eliminar esta compra?")) {
            Buy buyDB = buyService.getBuyById(buyId);
            if (buyDB != null) {
                buyService.deleteByBuyId(buyId);
                initializeTableValues();

                BuyRegistry buyRegistry = new BuyRegistry(
                        null,
                        buyDB.getBuyId(),
                        buyDB.getBuyName(),
                        buyDB.getBuyUnitaryPrice(),
                        buyDB.getBuyTotalPrice(),
                        buyDB.getCurrency().getCurrencyName(),
                        buyDB.getAmount(),
                        client,
                        registryType,
                        LocalDateTime.now()
                );
                buyRegistryService.save(buyRegistry);

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, registryZone, registryType, LocalDateTime.now()
                );
                generalRegistryService.save(generalRegistry);
                cleanForm();
            } else {
                displayAlerts.showAlert("No hay ningún registro con ese ID");
            }
        }
    }

    @FXML
    public void cleanFilters() {
        List<TextField> textFieldList = List.of(
                tfFilterId, tfFilterName, tfMaxFilterAmount, tfMaxFilterPrice, tfMinFilterAmount, tfMinFilterPrice);
        cleanHelper.cleanTextFields(textFieldList);
    }

    @FXML
    public void addOrUpgradeBuy() {
        if (!validateAllFields()) {
            return;
        }

        Long buyId = parseDataTypes.parseLong(tfId.getText());
        String buyName = tfAddBuyName.getText();
        Double unitaryPrice = parseDataTypes.parseDouble(tfAddUnitaryBuyPrice.getText());
        Double totalPrice = parseDataTypes.parseDouble(tfAddTotalBuyPrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());
        LocalDate receivedDate = dpAddExpenseDate.getValue();
        String currencyName = tfAddUnitaryBuyCurrency.getText();
        String buyType = "Materias Primas y Materiales";
        String registryType;

        // VERIFICAR SI ES UNA ACTUALIZACIÓN Y SI HAY ASIGNACIONES
        Integer originalLeftAmount = amount; // Por defecto, leftAmount = amount
        List<Inventory> existingInventories = new ArrayList<>();

        if (buyId != null && buyService.existsByBuyId(buyId)) {
            registryType = "Actualización";
            Buy existingBuy = buyService.getBuyById(buyId);

            // OBTENER INVENTARIOS EXISTENTES ASOCIADOS A ESTA COMPRA
            existingInventories = inventoryService.getInventoriesByBuyId(buyId);

            if (!existingInventories.isEmpty()) {
                // CALCULAR leftAmount CONSIDERANDO ASIGNACIONES EXISTENTES
                int totalAssignedAmount = existingInventories.stream()
                        .mapToInt(Inventory::getAmount)
                        .sum();

                originalLeftAmount = existingBuy.getLeftAmount();

                // MOSTRAR CONFIRMACIÓN AL USUARIO
                if (!displayAlerts.showConfirmationAlert(
                        "Esta compra ya tiene " + totalAssignedAmount + " productos asignados a inventarios. " +
                                "¿Desea remover estas asignaciones y recalcular el leftAmount?")) {
                    return; // El usuario canceló la operación
                }

                // ELIMINAR ASIGNACIONES EXISTENTES
                for (Inventory inventory : existingInventories) {
                    inventoryService.deleteInventoryById(inventory.getId());
                }

                // REGISTRAR LA ELIMINACIÓN DE ASIGNACIONES
                GeneralRegistry removalRegistry = new GeneralRegistry(
                        null, client, "Inventario",
                        "Remoción de asignaciones por actualización de compra: " + buyName,
                        LocalDateTime.now()
                );
                generalRegistryService.save(removalRegistry);
            }
        } else {
            registryType = "Adición";
        }

        // CALCULAR NUEVO leftAmount
        Integer newLeftAmount = amount;
        if (buyId != null && buyService.existsByBuyId(buyId)) {
            // Si no había asignaciones, mantener la relación original
            Buy existingBuy = buyService.getBuyById(buyId);
            int amountDifference = amount - existingBuy.getAmount();
            newLeftAmount = existingBuy.getLeftAmount() + amountDifference;

            // Asegurar que leftAmount no sea negativo
            if (newLeftAmount < 0) newLeftAmount = 0;
            if (newLeftAmount > amount) newLeftAmount = amount;
        }

        // Add the buy to DB
        Buy buy = new Buy(
                buyId, buyName, unitaryPrice, totalPrice,
                currencyService.getCurrencyByName(currencyName),
                amount,
                newLeftAmount, // ← USAR EL NUEVO leftAmount CALCULADO
                receivedDate, buyType, client
        );
        buyService.save(buy);
        initializeTableValues();

        Buy buyDBValue = buyService.getBuyListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
                buyName, unitaryPrice, totalPrice, currencyService.getCurrencyByName(currencyName), amount, amount, receivedDate, buyType, client).getLast();

        if (!productService.existsByProductNameAndClient(buyName, client)) {
            Product product = new Product(
                    null,
                    buyName,
                    null,
                    client,
                    null
            );
            productService.save(product);
        }

        // Add the buy registry to DB
        BuyRegistry buyRegistry = new BuyRegistry(
                null, buyDBValue.getBuyId(), buyName, unitaryPrice, totalPrice,
                currencyName, amount, client, registryType, LocalDateTime.now()
        );
        buyRegistryService.save(buyRegistry);

        // Add the general registry to DB
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null, client, registryZone, registryType, LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);
        // MOSTRAR MENSAJE INFORMATIVO
        if (!existingInventories.isEmpty()) {
            lblAddDebugForm.setText("Compra actualizada. Se removieron " + existingInventories.size() + " asignaciones de inventario.");
            lblAddDebugForm.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        } else {
            cleanForm();
        }
    }

    private boolean validateAllFields() {
        if (tfAddBuyName.getText().isEmpty() ||
                tfAddBuyAmount.getText().isEmpty() ||
                tfAddUnitaryBuyPrice.getText().isEmpty() ||
                tfAddTotalBuyPrice.getText().isEmpty() ||
                dpAddExpenseDate.getValue() == null) {
            displayAlerts.showAlert("Todos los campos son obligatorios.");
            return false;
        }

        Double unitaryPrice = parseDataTypes.parseDouble(tfAddUnitaryBuyPrice.getText());
        Double totalPrice = parseDataTypes.parseDouble(tfAddTotalBuyPrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());
        String currency = tfAddUnitaryBuyCurrency.getText();

        if (unitaryPrice == null || unitaryPrice <= 0) {
            displayAlerts.showAlert("El precio unitario debe ser un número positivo.");
            return false;
        }

        if (totalPrice == null || totalPrice <= 0) {
            displayAlerts.showAlert("El precio total debe ser un número positivo.");
            return false;
        }

        if (amount == null || amount <= 0) {
            displayAlerts.showAlert("La cantidad debe ser un número positivo.");
            return false;
        }

        if (currency.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }

        // Verify that total price equals unitary price * amount
        if (Math.abs(totalPrice - (unitaryPrice * amount)) > 0.01) {
            displayAlerts.showAlert("El precio total debe ser igual al precio unitario multiplicado por la cantidad.");
            return false;
        }

        return true;
    }

    private void filterBuyTable() {
        String id = tfFilterId.getText().trim();
        String name = tfFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMinFilterPrice.getText());
        Double maxPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMaxFilterPrice.getText());

        Predicate<BuyDataTable> filter = buy -> {
            boolean matches = true;

            if (!id.isEmpty())
                matches &= buy.getId() != null && buy.getId().toString().contains(id);

            if (!name.isEmpty())
                matches &= buy.getBuyName() != null && buy.getBuyName().toLowerCase().contains(name);

            if (minAmount != null && minAmount > 0)
                matches &= buy.getAmount() != null && buy.getAmount() >= minAmount;

            if (maxAmount != null && maxAmount > 0)
                matches &= buy.getAmount() != null && buy.getAmount() <= maxAmount;

            // Filter for minimum price
            if (minPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(buy.getUnitaryPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(buy.getUnitaryPriceAndCurrency()) >= minPrice;
            }

            // Filter for maximum price
            if (maxPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(buy.getUnitaryPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(buy.getUnitaryPriceAndCurrency()) <= maxPrice;
            }

            return matches;
        };

        ObservableList<BuyDataTable> filteredList = buyList.stream()
                .filter(filter)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tvBuy.setItems(filteredList);
    }

    @FXML
    public void cleanForm() {
        List<TextField> textFieldList = List.of(
                tfId, tfAddBuyName, tfAddUnitaryBuyPrice, tfAddUnitaryBuyCurrency, tfAddTotalBuyPrice, tfAddTotalBuyCurrency
        );
        cleanHelper.cleanTextFields(textFieldList);
        tfAddBuyAmount.setText("1");
        tfAddUnitaryBuyCurrency.setText("CUP");
        tfAddTotalBuyCurrency.setText("CUP");
        dpAddExpenseDate.setValue(LocalDate.now());
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchToRegistry(actionEvent);
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchToSupport(actionEvent);
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchToSell(actionEvent);
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchToBalance(actionEvent);
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchToExpense(actionEvent);
    }

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchToConfiguration(actionEvent);
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchToWarehouse(actionEvent);
    }
}
