package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseDataTable;
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
import lombok.Getter;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The type Expense view controller.
 */
@Controller
public class ExpenseViewController {

    // Observable list for table data
    private ObservableList<ExpenseDataTable> expenseList;
    private Client client;
    private String registryZone;

    // Dependency injection for required services and utilities
    private final ParseDataTypes parseDataTypes;
    private final UserLogged userLogged;
    private final ExpenseServiceImpl expenseService;
    private final ExpenseRegistryServiceImpl expenseRegistryService;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final ProductServiceImpl productService;
    private final BuyAndExpenseSharedMethods buyAndExpenseSharedMethods;
    private final CleanHelper cleanHelper;

    public ExpenseViewController(
            ProductServiceImpl productService, GeneralRegistryServiceImpl generalRegistryService, DisplayAlerts displayAlerts,
            ParseDataTypes parseDataTypes, UserLogged userLogged, ExpenseServiceImpl expenseService,
            CurrencyServiceImpl currencyService, ExpenseRegistryServiceImpl expenseRegistryService, SceneSwitcher sceneSwitcher, BuyAndExpenseSharedMethods buyAndExpenseSharedMethods, CleanHelper cleanHelper
    ) {
        this.currencyService = currencyService;
        this.productService = productService;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.sceneSwitcher = sceneSwitcher;
        this.expenseService = expenseService;
        this.expenseRegistryService = expenseRegistryService;
        this.userLogged = userLogged;
        this.parseDataTypes = parseDataTypes;
        this.buyAndExpenseSharedMethods = buyAndExpenseSharedMethods;
        this.cleanHelper = cleanHelper;
    }

    // FXML UI components
    @FXML
    private Label lblAddDebugForm, lblClientName;
    @FXML
    private TextField tfAddProductName, tfId, tfAddExpensePrice, tfAddExpenseCurrency, tfAddExpenseType;
    @Getter
    @FXML
    private TextField tfFilterId;
    @FXML
    private TextField tfFilterName, tfMinFilterAmount, tfMaxFilterAmount, tfMaxFilterPrice, tfMinFilterPrice;
    @FXML
    private DatePicker dpAddExpenseDate;
    @FXML
    private MenuButton mbCurrency, mbExpenseType;

    @FXML
    private TableView<ExpenseDataTable> tvExpense;
    @FXML
    private TableColumn<ExpenseDataTable, String> tcExpenseName, tcExpenseType, tcPriceAndCurrency;
    @FXML
    private TableColumn<ExpenseDataTable, Integer> tcAmount;
    @FXML
    private TableColumn<ExpenseDataTable, Long> tcId;
    @FXML
    private TableColumn<ExpenseDataTable, LocalDate> tcDate;

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        registryZone = "Gastos";
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        initializeTableColumns();
        initializeTableValues();

        Platform.runLater(() -> {
            initializeTableColumns();
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            cleanForm();
            initMbExpenseTypeItemsOnAction();
        });
    }


    private void setupTextFieldListeners() {
        tfFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfAddExpenseCurrency.textProperty().addListener((obs, oldVal, newVal) -> uppercaseCurrencyText());
    }

    private void uppercaseCurrencyText() {
        tfAddExpenseCurrency.setText(tfAddExpenseCurrency.getText().toUpperCase());
    }

    private void setupTableSelectionListener() {
        tvExpense.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                if (newSel.getExpenseType().equals("Materias Primas y Materiales")) {
                    displayAlerts.showAlert("Para modificar un producto de tipo *Materias Primas y Materiales* " +
                            "debe dirigirse a la ventana de VENTAS y corregir las ventas realizadas");
                    return;
                }
                Expense expense = expenseService.getExpenseById(newSel.getId());
                tfId.setText(expense.getExpenseId() != null ? String.valueOf(expense.getExpenseId()) : "");
                tfAddProductName.setText(expense.getExpenseName());
                tfAddExpensePrice.setText(expense.getExpensePrice() != null ? String.valueOf(expense.getExpensePrice()) : "");
                tfAddExpenseCurrency.setText(expense.getCurrency().getCurrencyName());
                tfAddExpenseType.setText(expense.getExpenseType());
                dpAddExpenseDate.setValue(expense.getReceivedDate());
            }
        });
    }

    private void initMbExpenseTypeItemsOnAction() {
        List<MenuItem> items = mbExpenseType.getItems().stream().toList();
        mbExpenseType.getItems().clear();

        for (MenuItem mi : items) {
            mi.setOnAction(e -> tfAddExpenseType.setText(mi.getText()));
            mbExpenseType.getItems().add(mi);
        }
    }

    private void initializeTableColumns() {
        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcExpenseType.setCellValueFactory(new PropertyValueFactory<>("expenseType"));
        tcPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("priceAndCurrency"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));

    }

    public void initializeTableValues() {
        expenseList = FXCollections.observableArrayList();
        List<Expense> investmentsDB = expenseService.getAllExpensesByClient(client);
        expenseList.clear();

        for (Expense expense : investmentsDB) {
            expenseList.add(new ExpenseDataTable(
                    expense.getExpenseId(),
                    expense.getExpenseName(),
                    expense.getExpenseType(),
                    expense.getExpensePrice() + " " + expense.getCurrency().getCurrencyName(),
                    expense.getAmount(),
                    expense.getReceivedDate()
            ));
        }

        tvExpense.setItems(expenseList);
        filterExpenseTable();
    }

    /**
     * Add or upgrade product.
     */
    @FXML
    public void addOrUpgradeProduct() {
        if (!validateAllFields()) {
            return;
        }

        Long expenseId = parseDataTypes.parseLong(tfId.getText());
        String expenseName = tfAddProductName.getText();
        Double price = parseDataTypes.parseDouble(tfAddExpensePrice.getText());
        LocalDate receivedDate = dpAddExpenseDate.getValue();
        String currency = tfAddExpenseCurrency.getText();
        String expenseType = tfAddExpenseType.getText();
        String registryType;

        if (expenseId == null || !expenseService.existsByExpenseId(expenseId)) {
            registryType = "Adición";
        } else {
            registryType = "Actualización";
        }

        //Add the expense to DB
        Expense expense = new Expense(
                expenseId,
                expenseName,
                price,
                currencyService.getCurrencyByName(currency),
                null,
                receivedDate,
                expenseType,
                client
        );
        expenseService.save(expense);
        initializeTableValues();

        Expense expenseDBValue =
                expenseService.getExpenseListByExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseTypeAndClientOrderByExpenseIdAsc(
                        expenseName, price, currencyService.getCurrencyByName(currency), null, receivedDate, expenseType, client
                ).getLast();

        //Add the expense registry to DB
        ExpenseRegistry expenseRegistry = new ExpenseRegistry(
                null,
                expenseDBValue.getExpenseId(),
                expenseName,
                price,
                currency,
                client,
                registryType,
                LocalDateTime.now()
        );
        expenseRegistryService.save(expenseRegistry);

        //Add the general registry to DB
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
        if (tfAddProductName.getText().isEmpty() ||
                tfAddExpenseType.getText().isEmpty() ||
                tfAddExpensePrice.getText().isEmpty() ||
                dpAddExpenseDate.getValue() == null) {
            displayAlerts.showAlert("Todos los campos son obligatorios.");
            return false;
        }

        Double price = parseDataTypes.parseDouble(tfAddExpensePrice.getText());
        String currency = tfAddExpenseCurrency.getText();

        // Additional validations
        if (price == null || price <= 0) {
            displayAlerts.showAlert("El precio debe ser un número positivo.");
            return false;
        }
        if (currency.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }
        return true;
    }

    /**
     * Remove investment.
     */
    @FXML
    public void removeExpense() {
        Long expenseId = parseDataTypes.parseLong(tfId.getText());
        String registryType = "Eliminación";
        if (expenseId == null) {
            displayAlerts.showAlert("Debes seleccionar un registro para eliminar.");
            return;
        }

        if (displayAlerts.showConfirmationAlert("¿Está seguro que desea eliminar este gasto?")) {
            Expense expenseDB = expenseService.getExpenseById(expenseId);
            if (expenseDB != null) {
                expenseService.deleteExpenseById(expenseId);
                initializeTableValues();

                ExpenseRegistry expenseRegistry = new ExpenseRegistry(
                        null,
                        expenseDB.getExpenseId(),
                        expenseDB.getExpenseName(),
                        expenseDB.getExpensePrice(),
                        expenseDB.getCurrency().getCurrencyName(),
                        client,
                        registryType,
                        LocalDateTime.now()
                );
                expenseRegistryService.save(expenseRegistry);

                //Add the general registry to DB
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

    /**
     * Clean form.
     */
    @FXML
    public void cleanForm() {
        List<TextField> textFieldList = List.of
                (tfId, tfAddExpenseType, tfAddProductName, tfAddExpensePrice, tfAddExpenseCurrency);
        cleanHelper.cleanTextFields(textFieldList);
        tfAddExpenseCurrency.setText("CUP");
        dpAddExpenseDate.setValue(LocalDate.now());
    }

    /**
     * Clean filters.
     */
    @FXML
    public void cleanFilters() {
        List<TextField> textFieldList = List.of(
                tfFilterId, tfFilterName, tfMinFilterAmount, tfMaxFilterAmount, tfAddExpenseCurrency, tfMinFilterPrice,
                tfMaxFilterPrice);
        cleanHelper.cleanTextFields(textFieldList);
    }

    private void filterExpenseTable() {
        String id = tfFilterId.getText().trim();
        String name = tfFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMinFilterPrice.getText());
        Double maxPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMaxFilterPrice.getText());

        Predicate<ExpenseDataTable> filter = expense -> {
            boolean matches = true;

            if (!id.isEmpty())
                matches &= expense.getId() != null && expense.getId().toString().contains(id);

            if (!name.isEmpty())
                matches &= expense.getExpenseName() != null &&
                        expense.getExpenseName().toLowerCase().contains(name);

            if (minAmount != null && minAmount > 0)
                matches &= expense.getAmount() != null && expense.getAmount() >= minAmount;

            if (maxAmount != null && maxAmount > 0)
                matches &= expense.getAmount() != null && expense.getAmount() <= maxAmount;

            // Filtro para precio mínimo
            if (minPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(expense.getPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(expense.getPriceAndCurrency()) >= minPrice;
            }

            // Filtro para precio máximo
            if (maxPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(expense.getPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(expense.getPriceAndCurrency()) <= maxPrice;
            }
            return matches;
        };

        ObservableList<ExpenseDataTable> filteredList = expenseList.stream()
                .filter(filter)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tvExpense.setItems(filteredList);
    }

    private void updateCurrencyMenu() {
        mbCurrency.getItems().clear();
        List<Currency> currencyList = currencyService.getAllCurrencies();
        List<String> currencies = new ArrayList<>();

        for (Currency c : currencyList) {
            currencies.add(c.getCurrencyName());
        }

        for (String currency : currencies) {
            MenuItem item = new MenuItem(currency);
            item.setOnAction(e -> tfAddExpenseCurrency.setText(currency));
            mbCurrency.getItems().add(item);
        }
    }

    @FXML
    private void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchToConfiguration(actionEvent);
    }

    @FXML
    public void switchToBuy(ActionEvent actionEvent) {
        sceneSwitcher.switchToBuy(actionEvent);
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
    private void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchToWarehouse(actionEvent);
    }

    @FXML
    private void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchToBalance(actionEvent);
    }

    @FXML
    private void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchToSell(actionEvent);
    }


}