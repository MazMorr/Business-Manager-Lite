package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.InvestmentDataTable;
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
import javafx.event.Event;
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
 * Controller for the Investment view.
 * Handles CRUD operations, filtering, and navigation for investments.
 */
@Lazy
@Controller
public class InvestmentViewController {

    // Observable list for table data
    private ObservableList<InvestmentDataTable> investmentList;
    private Client client;
    private String registryZone;

    // Dependency injection for required services and utilities
    private final ParseDataTypes parseDataTypes;
    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final InvestmentServiceImpl investmentService;
    private final InvestmentRegistryServiceImpl investmentRegistryService;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;
    private final DisplayAlerts displayAlerts;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final ProductServiceImpl productService;

    /**
     * Constructor for dependency injection.
     * All required services and utilities are injected here.
     * @param productService the product service
     * @param generalRegistryService the general registry service
     * @param displayAlerts the display alerts
     * @param parseDataTypes the parse data types
     * @param userLogged the user logged
     * @param clientService the client service
     * @param investmentService the investment service
     * @param currencyService the currency service
     * @param investmentRegistryService the investment registry service
     * @param sceneSwitcher the scene switcher
     */
    @Lazy
    public InvestmentViewController(ProductServiceImpl productService, GeneralRegistryServiceImpl generalRegistryService, DisplayAlerts displayAlerts, ParseDataTypes parseDataTypes, UserLogged userLogged, ClientServiceImpl clientService, InvestmentServiceImpl investmentService, CurrencyServiceImpl currencyService, InvestmentRegistryServiceImpl investmentRegistryService, SceneSwitcher sceneSwitcher) {
        this.currencyService = currencyService;
        this.productService = productService;
        this.generalRegistryService = generalRegistryService;
        this.displayAlerts = displayAlerts;
        this.sceneSwitcher = sceneSwitcher;
        this.investmentService = investmentService;
        this.investmentRegistryService = investmentRegistryService;
        this.clientService = clientService;
        this.userLogged = userLogged;
        this.parseDataTypes = parseDataTypes;
    }

    // FXML UI components
    @FXML
    private Label lblAddDebugForm, lblClientName;
    @FXML
    private TextField tfAddProductName, tfAddProductAmount, tfId, tfAddInvestmentPrice, tfAddInvestmentCurrency, tfAddInvestmentType;
    @FXML
    private TextField tfFilterId, tfFilterName, tfMinFilterPrice, tfMaxFilterPrice, tfMaxFilterAmount, tfMinFilterAmount;
    @FXML
    private DatePicker dpAddInvestmentDate;
    @FXML
    private MenuButton mbCurrency, mbInvestmentType;
    @FXML
    private Pagination paginator;

    @FXML
    private TableView<InvestmentDataTable> tvInvestment;
    @FXML
    private TableColumn<InvestmentDataTable, String> tcInvestmentName, tcTypeInvestment, tcCurrency;
    @FXML
    private TableColumn<InvestmentDataTable, Double> tcPrice;
    @FXML
    private TableColumn<InvestmentDataTable, Integer> tcAmount;
    @FXML
    private TableColumn<InvestmentDataTable, Long> tcId;
    @FXML
    private TableColumn<InvestmentDataTable, LocalDate> tcDate;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table values, listeners, and default currencies.
     */
    @FXML
    public void initialize() {
        registryZone = "Inversiones";
        lblClientName.setText(userLogged.getName());
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            initDpDefaultValue();
            initMbInvestmentTypeItemsOnAction();
        });
    }



    /**
     * Sets up listeners for filter and input text fields.
     * Triggers table filtering and formatting.
     */
    private void setupTextFieldListeners() {
        tfFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        tfFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        tfMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        tfMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        tfMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        tfAddInvestmentCurrency.textProperty().addListener((obs, oldVal, newVal) -> uppercaseCurrencyText());
    }

    /**
     * Converts the currency text to uppercase for consistency.
     */
    private void uppercaseCurrencyText() {
        tfAddInvestmentCurrency.setText(tfAddInvestmentCurrency.getText().toUpperCase());
    }

    /**
     * Sets up listener for table row selection.
     * Populates form fields with selected investment data.
     */
    private void setupTableSelectionListener() {
        tvInvestment.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfId.setText(newSel.getId() != null ? String.valueOf(newSel.getId()) : "");
                tfAddProductName.setText(newSel.getInvestmentName());
                tfAddProductAmount.setText(newSel.getAmount() != null ? String.valueOf(newSel.getAmount()) : "");
                tfAddInvestmentPrice.setText(newSel.getPrice() != null ? String.valueOf(newSel.getPrice()) : "");
                tfAddInvestmentCurrency.setText(newSel.getCurrency());
                dpAddInvestmentDate.setValue(newSel.getReceivedDate());
            }
        });
    }

    private void initMbInvestmentTypeItemsOnAction() {
        List<MenuItem> items = mbInvestmentType.getItems().stream().toList();
        mbInvestmentType.getItems().clear();

        for (MenuItem mi : items) {
            mi.setOnAction(e -> tfAddInvestmentType.setText(mi.getText()));
            mbInvestmentType.getItems().add(mi);
        }
    }

    /**
     * Loads investment data from the database and populates the table.
     * Also sets up table columns and triggers filtering.
     */
    public void initializeTableValues() {
        investmentList = FXCollections.observableArrayList();
        List<Investment> investmentsDB = investmentService.getAllInvestments();
        investmentList.clear();

        for (Investment investment : investmentsDB) {
            investmentList.add(new InvestmentDataTable(
                    investment.getInvestmentId(),
                    investment.getInvestmentName(),
                    investment.getInvestmentType(),
                    investment.getInvestmentPrice(),
                    investment.getCurrency().getCurrencyName(),
                    investment.getAmount(),
                    investment.getReceivedDate()
            ));
        }

        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcInvestmentName.setCellValueFactory(new PropertyValueFactory<>("investmentName"));
        tcTypeInvestment.setCellValueFactory(new PropertyValueFactory<>("investmentType"));
        tcPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        tcCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));

        tvInvestment.setItems(investmentList);
        filterInvestmentTable();
    }

    /**
     * Adds or updates an investment record based on form input.
     * Validates input fields and saves the investment.
     * @param actionEvent the action event
     */
    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        if (!validateAllFields()) {
            return;
        }

        Long investmentId = parseDataTypes.parseLong(tfId.getText());
        String investmentName = tfAddProductName.getText();
        Double price = parseDataTypes.parseDouble(tfAddInvestmentPrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddProductAmount.getText());
        LocalDate receivedDate = dpAddInvestmentDate.getValue();
        String currency = tfAddInvestmentCurrency.getText();
        String investmentType = tfAddInvestmentType.getText();
        String registryType;
        if (!investmentService.existsByInvestmentId(investmentId)) {
            registryType = "Adición";
        } else {
            registryType = "Actualización";
        }

        //Add the investment to DB
        Investment investment = new Investment(
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
        investmentService.save(investment);
        initializeTableValues();

        Investment inv = investmentService
                .getByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(
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

        //Add the investment registry to DB
        InvestmentRegistry investmentRegistry = new InvestmentRegistry(
                null,
                inv.getInvestmentId(),
                investmentName,
                price,
                currency,
                client,
                registryType,
                LocalDateTime.now()
        );
        investmentRegistryService.save(investmentRegistry);

        //Add the general registry to DB
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null,
                client,
                registryZone,
                registryType,
                LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);

        cleanForm(null);
        updateCurrencyMenu();
    }

    /**
     * Validates all required fields before saving or updating an investment.
     * Shows alerts in Spanish if validation fails.
     */
    private boolean validateAllFields() {
        if (tfAddProductName.getText().isEmpty() ||
                tfAddProductAmount.getText().isEmpty() ||
                tfAddInvestmentType.getText().isEmpty() ||
                tfAddInvestmentPrice.getText().isEmpty() ||
                dpAddInvestmentDate.getValue() == null) {
            displayAlerts.showAlert("Todos los campos son obligatorios.");
            return false;
        }

        Double price = parseDataTypes.parseDouble(tfAddInvestmentPrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddProductAmount.getText());
        String currency = tfAddInvestmentCurrency.getText();

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
     * Removes the selected investment record from the database.
     * Prompts user for confirmation before deletion (alert in Spanish).
     * @param actionEvent the action event
     */
    @FXML
    public void removeInvestment(ActionEvent actionEvent) {
        Long investmentId = parseDataTypes.parseLong(tfId.getText());
        String registryType = "Eliminación";
        if (investmentId == null) {
            displayAlerts.showAlert("Debes seleccionar un registro para eliminar.");
            return;
        }

        if (displayAlerts.showConfirmationAlert("¿Está seguro que desea eliminar esta inversión?")) {
            Investment investmentDB = investmentService.getInvestmentById(investmentId);
            if (investmentDB != null) {
                investmentService.deleteInvestmentById(investmentId);
                initializeTableValues();

                InvestmentRegistry investmentRegistry = new InvestmentRegistry(
                        null,
                        investmentDB.getInvestmentId(),
                        investmentDB.getInvestmentName(),
                        investmentDB.getInvestmentPrice(),
                        investmentDB.getCurrency().getCurrencyName(),
                        client,
                        registryType,
                        LocalDateTime.now()
                );
                investmentRegistryService.save(investmentRegistry);

                //Add the general registry to DB
                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null,
                        client,
                        registryZone,
                        registryType,
                        LocalDateTime.now()
                );
                generalRegistryService.save(generalRegistry);
                cleanForm(null);
            } else {
                displayAlerts.showAlert("No hay ningún registro con ese ID");
            }
        }
    }

    /**
     * Clears all input fields in the investment form.
     * @param actionEvent the action event
     */
    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        tfId.clear();
        tfAddInvestmentType.clear();
        tfAddProductName.clear();
        tfAddProductAmount.clear();
        tfAddInvestmentPrice.clear();
        dpAddInvestmentDate.setValue(LocalDate.now());
        tfAddInvestmentCurrency.clear();
    }

    /**
     * Clears all filter fields for investment table.
     * @param actionEvent the action event
     */
    @FXML
    public void cleanFilters(ActionEvent actionEvent) {
        tfFilterId.clear();
        tfFilterName.clear();
        tfMinFilterAmount.clear();
        tfMaxFilterAmount.clear();
        tfAddInvestmentCurrency.clear();
        tfMinFilterPrice.clear();
        tfMaxFilterPrice.clear();
    }

    /**
     * Filters the investment table based on filter field values.
     */
    private void filterInvestmentTable() {
        String id = tfFilterId.getText().trim();
        String name = tfFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = parseDataTypes.parseDouble(tfMinFilterPrice.getText());
        Double maxPrice = parseDataTypes.parseDouble(tfMaxFilterPrice.getText());

        Predicate<InvestmentDataTable> filter = investment -> {
            boolean matches = true;

            if (!id.isEmpty())
                matches &= investment.getId() != null && investment.getId().toString().contains(id);

            if (!name.isEmpty())
                matches &= investment.getInvestmentName() != null &&
                        investment.getInvestmentName().toLowerCase().contains(name);

            if (minAmount != null && minAmount > 0)
                matches &= investment.getAmount() != null && investment.getAmount() >= minAmount;

            if (maxAmount != null && maxAmount > 0)
                matches &= investment.getAmount() != null && investment.getAmount() <= maxAmount;

            if (minPrice != null && minPrice > 0.0)
                matches &= investment.getPrice() != null && investment.getPrice() >= minPrice;

            if (maxPrice != null && maxPrice > 0.0)
                matches &= investment.getPrice() != null && investment.getPrice() <= maxPrice;

            return matches;
        };

        ObservableList<InvestmentDataTable> filteredList = investmentList.stream()
                .filter(filter)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tvInvestment.setItems(filteredList);
    }

    /**
     * Updates the currency menu with all available currencies from the database.
     */
    private void updateCurrencyMenu() {
        mbCurrency.getItems().clear();
        List<Currency> currencyList = currencyService.getAllCurrencies();
        List<String> currencies = new ArrayList<>();

        for (Currency c : currencyList) {
            currencies.add(c.getCurrencyName());
        }

        for (String currency : currencies) {
            MenuItem item = new MenuItem(currency);
            item.setOnAction(e -> {
                tfAddInvestmentCurrency.setText(currency);
            });
            mbCurrency.getItems().add(item);
        }
    }

    /**
     * Populates form fields with the selected investment from the table.
     * @param event the event
     */
    @FXML
    public void selectInventory(Event event) {
        InvestmentDataTable selectedInvestment = tvInvestment.getSelectionModel().getSelectedItem();
        tfId.setText(String.valueOf(selectedInvestment.getId()));
        dpAddInvestmentDate.setValue(selectedInvestment.getReceivedDate());
        tfAddProductName.setText(selectedInvestment.getInvestmentName());
        tfAddInvestmentType.setText(selectedInvestment.getInvestmentType());
        tfAddProductAmount.setText(String.valueOf(selectedInvestment.getAmount()));
        tfAddInvestmentCurrency.setText(selectedInvestment.getCurrency());
        tfAddInvestmentPrice.setText(String.valueOf(selectedInvestment.getPrice()));
    }

    /**
     * Navigates to the configuration view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    /**
     * Navigates to the support view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    /**
     * Navigates to the registry view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    /**
     * Navigates to the warehouse view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    /**
     * Navigates to the balance view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    /**
     * Navigates to the sell view.
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }


    /**
     * Initializes the date picker with the current date.
     */
    private void initDpDefaultValue() {
        dpAddInvestmentDate.setValue(LocalDate.now());
    }

}