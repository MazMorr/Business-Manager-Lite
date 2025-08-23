package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.*;
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
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.annotation.Lazy;
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

    /**
     * Instantiates a new Expense view controller.
     *
     * @param productService the product service
     * @param generalRegistryService the general registry service
     * @param displayAlerts the display alerts
     * @param parseDataTypes the parse data types
     * @param userLogged the user logged
     * @param expenseService the expense service
     * @param currencyService the currency service
     * @param expenseRegistryService the expense registry service
     * @param sceneSwitcher the scene switcher
     */
    @Lazy
    public ExpenseViewController(
            ProductServiceImpl productService, GeneralRegistryServiceImpl generalRegistryService, DisplayAlerts displayAlerts,
            ParseDataTypes parseDataTypes, UserLogged userLogged, ExpenseServiceImpl expenseService,
            CurrencyServiceImpl currencyService, ExpenseRegistryServiceImpl expenseRegistryService, SceneSwitcher sceneSwitcher
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
    }

    // FXML UI components
    @FXML
    private Label lblAddDebugForm, lblClientName;
    @FXML
    private TextField tfAddProductName, tfAddProductAmount, tfId, tfAddExpensePrice, tfAddExpenseCurrency, tfAddExpenseType;
    @FXML
    private TextField tfFilterId, tfFilterName, tfMinFilterPrice, tfMaxFilterPrice, tfMaxFilterAmount, tfMinFilterAmount;
    @FXML
    private DatePicker dpAddExpenseDate;
    @FXML
    private MenuButton mbCurrency, mbExpenseType;

    @FXML
    private TableView<ExpenseDataTable> tvExpense;
    @FXML
    private TableColumn<ExpenseDataTable, String> tcExpenseName, tcExpenseType, tcCurrency;
    @FXML
    private TableColumn<ExpenseDataTable, Double> tcPrice;
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
        lblClientName.setText(userLogged.getName());
        client = userLogged.getClient();
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            initDpDefaultValue();
            initMbExpenseTypeItemsOnAction();
        });
    }

    private void setupTextFieldListeners() {
        tfFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterExpenseTable());
        tfAddExpenseCurrency.textProperty().addListener((obs, oldVal, newVal) -> uppercaseCurrencyText());
    }

    private void uppercaseCurrencyText() {
        tfAddExpenseCurrency.setText(tfAddExpenseCurrency.getText().toUpperCase());
    }

    private void setupTableSelectionListener() {
        tvExpense.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfId.setText(newSel.getId() != null ? String.valueOf(newSel.getId()) : "");
                tfAddProductName.setText(newSel.getExpenseName());
                tfAddProductAmount.setText(newSel.getAmount() != null ? String.valueOf(newSel.getAmount()) : "");
                tfAddExpensePrice.setText(newSel.getPrice() != null ? String.valueOf(newSel.getPrice()) : "");
                tfAddExpenseCurrency.setText(newSel.getCurrency());
                dpAddExpenseDate.setValue(newSel.getReceivedDate());
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

    /**
     * Initialize table values.
     */
    public void initializeTableValues() {
        expenseList = FXCollections.observableArrayList();
        List<Expense> investmentsDB = expenseService.getAllExpensesByClient(client);
        expenseList.clear();

        for (Expense expense : investmentsDB) {
            expenseList.add(new ExpenseDataTable(
                    expense.getExpenseId(),
                    expense.getExpenseName(),
                    expense.getExpenseType(),
                    expense.getExpensePrice(),
                    expense.getCurrency().getCurrencyName(),
                    expense.getAmount(),
                    expense.getReceivedDate()
            ));
        }

        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcExpenseType.setCellValueFactory(new PropertyValueFactory<>("expenseType"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        tcCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));

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

        Long investmentId = parseDataTypes.parseLong(tfId.getText());
        String investmentName = tfAddProductName.getText();
        Double price = parseDataTypes.parseDouble(tfAddExpensePrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddProductAmount.getText());
        LocalDate receivedDate = dpAddExpenseDate.getValue();
        String currency = tfAddExpenseCurrency.getText();
        String investmentType = tfAddExpenseType.getText();
        String registryType;
        if (!expenseService.existsByExpenseId(investmentId)) {
            registryType = "Adición";
        } else {
            registryType = "Actualización";
        }

        //Add the expense to DB
        Expense expense = new Expense(
                investmentId,
                investmentName,
                price,
                currencyService.getCurrencyByName(currency),
                amount,
                amount,
                receivedDate,
                investmentType,
                client
        );
        expenseService.save(expense);
        initializeTableValues();

        Expense inv = expenseService
                .getByClientAndExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseType(
                        client, investmentName, price, currencyService.getCurrencyByName(currency), amount, receivedDate, investmentType
                );

        if (investmentType.equals("Producto") && !productService.existsByProductNameAndClient(investmentName, client)) {
            Product product = new Product(
                    null,
                    investmentName,
                    null,
                    client,
                    null
            );
            productService.save(product);
        }

        //Add the expense registry to DB
        ExpenseRegistry expenseRegistry = new ExpenseRegistry(
                null,
                inv.getExpenseId(),
                investmentName,
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
        updateCurrencyMenu();
    }

    private boolean validateAllFields() {
        if (tfAddProductName.getText().isEmpty() ||
                tfAddProductAmount.getText().isEmpty() ||
                tfAddExpenseType.getText().isEmpty() ||
                tfAddExpensePrice.getText().isEmpty() ||
                dpAddExpenseDate.getValue() == null) {
            displayAlerts.showAlert("Todos los campos son obligatorios.");
            return false;
        }

        Double price = parseDataTypes.parseDouble(tfAddExpensePrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddProductAmount.getText());
        String currency = tfAddExpenseCurrency.getText();

        // Additional validations
        if (price == null || price <= 0) {
            displayAlerts.showAlert("El precio debe ser un número positivo.");
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
        tfId.clear();
        tfAddExpenseType.clear();
        tfAddProductName.clear();
        tfAddProductAmount.clear();
        tfAddExpensePrice.clear();
        dpAddExpenseDate.setValue(LocalDate.now());
        tfAddExpenseCurrency.clear();
    }

    /**
     * Clean filters.
     */
    @FXML
    public void cleanFilters() {
        tfFilterId.clear();
        tfFilterName.clear();
        tfMinFilterAmount.clear();
        tfMaxFilterAmount.clear();
        tfAddExpenseCurrency.clear();
        tfMinFilterPrice.clear();
        tfMaxFilterPrice.clear();
    }

    private void filterExpenseTable() {
        String id = tfFilterId.getText().trim();
        String name = tfFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = parseDataTypes.parseDouble(tfMinFilterPrice.getText());
        Double maxPrice = parseDataTypes.parseDouble(tfMaxFilterPrice.getText());

        Predicate<ExpenseDataTable> filter = investment -> {
            boolean matches = true;

            if (!id.isEmpty())
                matches &= investment.getId() != null && investment.getId().toString().contains(id);

            if (!name.isEmpty())
                matches &= investment.getExpenseName() != null &&
                        investment.getExpenseName().toLowerCase().contains(name);

            if (minAmount != null && minAmount > 0)
                matches &= investment.getAmount() != null && investment.getAmount() >= minAmount;

            if (maxAmount != null && maxAmount > 0)
                matches &= investment.getAmount() != null && investment.getAmount() <= maxAmount;

            // Filtro para precio mínimo
            if (minPrice != null) {
                matches &= investment.getPrice() != null && investment.getPrice() >= minPrice;
            }

            // Filtro para precio máximo
            if (maxPrice != null) {
                matches &= investment.getPrice() != null && investment.getPrice() <= maxPrice;
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

    /**
     * Select inventory.
     */
    @FXML
    public void selectInventory() {
        ExpenseDataTable selectedInvestment = tvExpense.getSelectionModel().getSelectedItem();
        tfId.setText(String.valueOf(selectedInvestment.getId()));
        dpAddExpenseDate.setValue(selectedInvestment.getReceivedDate());
        tfAddProductName.setText(selectedInvestment.getExpenseName());
        tfAddExpenseType.setText(selectedInvestment.getExpenseType());
        tfAddProductAmount.setText(String.valueOf(selectedInvestment.getAmount()));
        tfAddExpenseCurrency.setText(selectedInvestment.getCurrency());
        tfAddExpensePrice.setText(String.valueOf(selectedInvestment.getPrice()));
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
     * Switch to balance.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    /**
     * Switch to sell.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }

    private void initDpDefaultValue() {
        dpAddExpenseDate.setValue(LocalDate.now());
    }
}