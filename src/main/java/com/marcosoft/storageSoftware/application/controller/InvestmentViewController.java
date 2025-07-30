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

    /**
     * Constructor for dependency injection.
     * All required services and utilities are injected here.
     */
    @Lazy
    public InvestmentViewController(GeneralRegistryServiceImpl generalRegistryService, DisplayAlerts displayAlerts, ParseDataTypes parseDataTypes, UserLogged userLogged, ClientServiceImpl clientService, InvestmentServiceImpl investmentService, CurrencyServiceImpl currencyService, InvestmentRegistryServiceImpl investmentRegistryService, SceneSwitcher sceneSwitcher) {
        this.currencyService = currencyService;
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
    private TableView<InvestmentDataTable> tblInvestment;
    @FXML
    private TableColumn<InvestmentDataTable, Integer> amountColumn;
    @FXML
    private TableColumn<InvestmentDataTable, String> nameColumn, currencyColumn;
    @FXML
    private TableColumn<InvestmentDataTable, LocalDate> dateColumn;
    @FXML
    private TableColumn<InvestmentDataTable, Long> idColumn;
    @FXML
    private TableColumn<InvestmentDataTable, Double> priceColumn;
    @FXML
    private MenuButton mbCurrency, mbInvestmentType;
    @FXML
    private Pagination paginator;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table values, listeners, and default currencies.
     */
    @FXML
    public void initialize() {
        registryZone= "Inversiones";
        lblClientName.setText(userLogged.getName());
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            initCurrencyDefaultValues();
            initDpDefaultValue();
        });
    }

    /**
     * Initializes default currencies if they do not exist in the database.
     */
    private void initCurrencyDefaultValues() {
        List<String> defaultCurrenciesName = List.of("MLC", "CUP", "USD", "EUR");
        for (String currencyName : defaultCurrenciesName) {
            if (!currencyService.existsByCurrencyName(currencyName)) {
                Currency currency = new Currency(null, currencyName, client);
                currencyService.save(currency);
            }
        }
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
        tblInvestment.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfId.setText(newSel.getId() != null ? String.valueOf(newSel.getId()) : "");
                tfAddProductName.setText(newSel.getProductName());
                tfAddProductAmount.setText(newSel.getAmount() != null ? String.valueOf(newSel.getAmount()) : "");
                tfAddInvestmentPrice.setText(newSel.getPrice() != null ? String.valueOf(newSel.getPrice()) : "");
                tfAddInvestmentCurrency.setText(newSel.getCurrency());
                dpAddInvestmentDate.setValue(newSel.getReceivedDate());
            }
        });
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
                    investment.getInvestmentPrice(),
                    investment.getCurrency().getCurrencyName(),
                    investment.getAmount(),
                    investment.getReceivedDate()
            ));
        }


        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));

        tblInvestment.setItems(investmentList);
        filterInvestmentTable();
    }

    /**
     * Adds or updates an investment record based on form input.
     * Validates input fields and saves the investment.
     */
    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        if (!validateAllFields()) {
            return;
        }

        Long investmentId = parseDataTypes.parseLong(tfId.getText());
        String productName = tfAddProductName.getText();
        Double price = parseDataTypes.parseDouble(tfAddInvestmentPrice.getText());
        Integer amount = parseDataTypes.parseInt(tfAddProductAmount.getText());
        LocalDate receivedDate = dpAddInvestmentDate.getValue();
        String currency = tfAddInvestmentCurrency.getText();
        String investmentType = tfAddInvestmentType.getText();
        String registryType;
        if(investmentService.getInvestmentById(investmentId) == null){
            registryType = "Adición";
        }else{
            registryType= "Actualización";
        }

        //Add the investment to DB
        Investment investment = new Investment(
                investmentId,
                productName,
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

        //Add the investment registry to DB
        InvestmentRegistry investmentRegistry = new InvestmentRegistry(
                null,
                investment,
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
        } else if (!currencyService.existsByCurrencyName(currency)) {
            Currency newCurrency = new Currency(null, currency, client);
            currencyService.save(newCurrency);
        }
        return true;
    }

    /**
     * Removes the selected investment record from the database.
     * Prompts user for confirmation before deletion (alert in Spanish).
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
                        investmentDB,
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
     */
    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        tfId.clear();
        tfAddProductName.clear();
        tfAddProductAmount.setText("0");
        tfAddInvestmentPrice.setText("0.00");
        dpAddInvestmentDate.setValue(LocalDate.now());
        tfAddInvestmentCurrency.clear();
    }

    /**
     * Clears all filter fields for investment table.
     */
    @FXML
    public void cleanFilters(ActionEvent actionEvent) {
        tfFilterId.clear();
        tfFilterName.clear();
        tfMinFilterAmount.clear();
        tfMaxFilterAmount.clear();
        tfAddInvestmentCurrency.clear();
        tfMinFilterPrice.setText(String.valueOf(0.00));
        tfMaxFilterPrice.setText(String.valueOf(0.00));
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
                matches &= investment.getProductName() != null &&
                        investment.getProductName().toLowerCase().contains(name);

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

        tblInvestment.setItems(filteredList);
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
     */
    @FXML
    public void selectInventory(Event event) {
        InvestmentDataTable selectedInvestment = tblInvestment.getSelectionModel().getSelectedItem();
        tfId.setText(String.valueOf(selectedInvestment.getId()));
        dpAddInvestmentDate.setValue(selectedInvestment.getReceivedDate());
        tfAddProductName.setText(selectedInvestment.getProductName());
        tfAddProductAmount.setText(String.valueOf(selectedInvestment.getAmount()));
        tfAddInvestmentCurrency.setText(selectedInvestment.getCurrency());
        tfAddInvestmentPrice.setText(String.valueOf(selectedInvestment.getPrice()));
    }

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
     * Navigates to the balance view.
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/balanceView.fxml");
    }

    /**
     * Navigates to the sell view.
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/sellView.fxml");
    }

    /**
     * Sets the investment type to "Producto" when the corresponding button is clicked.
     */
    @FXML
    public void setInvestmentTypeToProduct(ActionEvent actionEvent) {
        tfAddInvestmentType.setText("Producto");
    }

    /**
     * Sets the investment type to "Servicio" when the corresponding button is clicked.
     */
    @FXML
    public void setInvestmentTypeToService(ActionEvent actionEvent) {
        tfAddInvestmentType.setText("Servicio");
    }

    /**
     * Initializes the date picker with the current date.
     */
    private void initDpDefaultValue() {
        dpAddInvestmentDate.setValue(LocalDate.now());
    }

    @FXML
    public void setInvestmentBill(ActionEvent actionEvent) {
        tfAddInvestmentType.setText("Factura");
    }
}