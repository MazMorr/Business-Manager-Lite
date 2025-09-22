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

    public BuyViewController(
            UserLogged userLogged, SceneSwitcher sceneSwitcher, CurrencyServiceImpl currencyService, BuyServiceImpl buyService,
            ParseDataTypes parseDataTypes, BuyAndExpenseSharedMethods buyAndExpenseSharedMethods, DisplayAlerts displayAlerts,
            GeneralRegistryServiceImpl generalRegistryService, BuyRegistryServiceImpl buyRegistryService, ProductServiceImpl productService, CleanHelper cleanHelper
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
    }

    @FXML
    private Label lblClientName, lblAddDebugForm;
    @FXML
    private TableView<BuyDataTable> tvBuy;
    @FXML
    private TableColumn<BuyDataTable, String> tcBuyName, tcBuyType, tcTotalPriceAndCurrency, tcUnitaryPriceAndCurrency;
    @FXML
    private TableColumn<BuyDataTable, Long> tcId;
    @FXML
    private TableColumn<BuyDataTable, Integer> tcAmount;
    @FXML
    private TableColumn<BuyDataTable, LocalDate> tcDate;

    @FXML
    private MenuButton mbBuyType, mbTotalCurrency, mbUnitaryCurrency;

    @FXML
    private TextField tfMinFilterAmount, tfAddTotalBuyCurrency, tfFilterId, tfFilterName, tfAddUnitaryBuyCurrency,
            tfAddBuyName, tfAddUnitaryBuyPrice, tfId, tfAddTotalBuyPrice, tfMaxFilterPrice, tfMaxFilterAmount,
            tfAddBuyType, tfMinFilterPrice, tfAddBuyAmount;

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
                tfAddBuyType.setText(buy.getBuyType());
                tfAddUnitaryBuyCurrency.setText(buy.getCurrency().getCurrencyName());
                tfAddTotalBuyCurrency.setText(buy.getCurrency().getCurrencyName());
                dpAddExpenseDate.setValue(buy.getReceivedDate());
            }
        });
    }

    private void initMbs() {
        initMbBuyType();
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
        }

    }

    private void initMbBuyType() {
        List<MenuItem> items = mbBuyType.getItems().stream().toList();
        mbBuyType.getItems().clear();

        for (MenuItem mi : items) {
            mi.setOnAction(e -> tfAddBuyType.setText(mi.getText()));
            mbBuyType.getItems().add(mi);
        }
    }

    private void initializeTableColumns() {
        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcBuyName.setCellValueFactory(new PropertyValueFactory<>("buyName"));
        tcBuyType.setCellValueFactory(new PropertyValueFactory<>("buyType"));
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
                    buy.getBuyType(),
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
    public void removeBuy(ActionEvent actionEvent) {
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
                        buyDB.getBuyTotalPrice(),
                        buyDB.getCurrency().getCurrencyName(),
                        client,
                        registryType,
                        LocalDateTime.now()
                );
                buyRegistryService.save(buyRegistry);

                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null,
                        client,
                        registryZone,
                        registryType,
                        LocalDateTime.now()
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
        String buyType = tfAddBuyType.getText();
        String registryType;

        if (!buyService.existsByBuyId(buyId)) {
            registryType = "Adición";
        } else {
            registryType = "Actualización";
        }

        // Add the buy to DB
        Buy buy = new Buy(
                buyId,
                buyName,
                unitaryPrice,
                totalPrice,
                currencyService.getCurrencyByName(currencyName),
                amount,
                amount, // Initially leftAmount equals amount
                receivedDate,
                buyType,
                client
        );
        buyService.save(buy);
        initializeTableValues();

        Buy buyDBValue = buyService.getBuyListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
                buyName, unitaryPrice, totalPrice, currencyService.getCurrencyByName(currencyName), amount, amount, receivedDate, buyType, client
        ).getLast();

        if (buyType.equals("Materias Primas y Materiales") && !productService.existsByProductNameAndClient(buyName, client)) {
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
                null,
                buyDBValue.getBuyId(),
                buyName,
                totalPrice,
                currencyName,
                client,
                registryType,
                LocalDateTime.now()
        );
        buyRegistryService.save(buyRegistry);

        // Add the general registry to DB
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null,
                client,
                registryZone,
                registryType,
                LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);

        cleanForm();
    }

    private boolean validateAllFields() {
        if (tfAddBuyName.getText().isEmpty() ||
                tfAddBuyAmount.getText().isEmpty() ||
                tfAddBuyType.getText().isEmpty() ||
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
                tfId, tfAddBuyName, tfAddUnitaryBuyPrice, tfAddUnitaryBuyCurrency, tfAddTotalBuyPrice, tfAddTotalBuyCurrency);
        cleanHelper.cleanTextFields(textFieldList);

        if (mbBuyType.getItems().size() == 1) {
            tfAddBuyType.setText(mbBuyType.getItems().getFirst().getText());
        }
        tfAddBuyAmount.setText("1");
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
