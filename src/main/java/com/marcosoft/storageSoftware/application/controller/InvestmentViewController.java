package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.InvestmentDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // ============================
    // DEPENDENCY INJECTION
    // ============================
    private final ParseDataTypes parseDataTypes;
    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final InvestmentServiceImpl investmentService;
    private final InvestmentRegistryServiceImpl investmentRegistryService;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;
    private final DisplayAlerts displayAlerts;

    /**
     * Constructor for dependency injection.
     * All required services and utilities are injected here.
     */
    @Lazy
    public InvestmentViewController(DisplayAlerts displayAlerts, ParseDataTypes parseDataTypes, UserLogged userLogged, ClientServiceImpl clientService, InvestmentServiceImpl investmentService, CurrencyServiceImpl currencyService, InvestmentRegistryServiceImpl investmentRegistryService, SceneSwitcher sceneSwitcher) {
        this.currencyService = currencyService;
        this.displayAlerts = displayAlerts;
        this.sceneSwitcher = sceneSwitcher;
        this.investmentService = investmentService;
        this.investmentRegistryService = investmentRegistryService;
        this.clientService = clientService;
        this.userLogged = userLogged;
        this.parseDataTypes = parseDataTypes;
    }

    // UI components
    @FXML private Label txtAddDebugForm, txtClientName;
    @FXML private TextField txtAddProductName, txtAddProductAmount, txtId, txtAddInvestmentPrice, txtAddInvestmentCurrency, txtAddInvestmentType;
    @FXML private TextField txtFilterId, txtFilterName, txtMinFilterPrice, txtMaxFilterPrice, txtMaxFilterAmount, txtMinFilterAmount;
    @FXML private DatePicker txtAddInvestmentDate;
    @FXML private TableView<InvestmentDataTable> tblInvestment;
    @FXML private TableColumn<InvestmentDataTable, Integer> amountColumn;
    @FXML private TableColumn<InvestmentDataTable, String> nameColumn, currencyColumn;
    @FXML private TableColumn<InvestmentDataTable, LocalDate> dateColumn;
    @FXML private TableColumn<InvestmentDataTable, Long> idColumn;
    @FXML private TableColumn<InvestmentDataTable, Double> priceColumn;
    @FXML private MenuButton mbCurrency, mbInvestmentType;
    @FXML private Pagination paginator;

    // ============================
    // INITIALIZATION
    // ============================
    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up table values, listeners, and default currencies.
     */
    @FXML
    public void initialize() {
        txtClientName.setText(userLogged.getName());
        client = clientService.getClientByName(userLogged.getName());
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            initCurrencyDefaultValues();
        });
    }

    /**
     * Initializes default currencies if they do not exist in the database.
     */
    private void initCurrencyDefaultValues() {
        List<String> defaultCurrenciesName = List.of("MLC", "CUP", "USD", "EUR", "CAD");
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
        txtFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMinFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtAddInvestmentCurrency.textProperty().addListener((obs, oldVal, newVal) -> uppercaseCurrencyText());
    }

    private void uppercaseCurrencyText() {
        txtAddInvestmentCurrency.setText(txtAddInvestmentCurrency.getText().toUpperCase());
    }

    private void setupTableSelectionListener() {
        tblInvestment.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtId.setText(newSel.getId() != null ? String.valueOf(newSel.getId()) : "");
                txtAddProductName.setText(newSel.getProductName());
                txtAddProductAmount.setText(newSel.getAmount() != null ? String.valueOf(newSel.getAmount()) : "");
                txtAddInvestmentPrice.setText(newSel.getPrice() != null ? String.valueOf(newSel.getPrice()) : "");
                txtAddInvestmentCurrency.setText(newSel.getCurrency());
                txtAddInvestmentDate.setValue(newSel.getReceivedDate());
            }
        });
    }

    // ============================
    // CARGA Y REFRESCO DE TABLA
    // ============================
    public void initializeTableValues() {
        investmentList = FXCollections.observableArrayList();
        List<Investment> investmentsDB = investmentService.getAllInvestments();
        investmentList.clear();

        for (Investment investment : investmentsDB) {
            investmentList.add(new InvestmentDataTable(
                    investment.getInvestmentId(),
                    investment.getProductName(),
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

    // ============================
    // CRUD DE INVERSIONES
    // ============================
    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        if (txtAddProductName.getText().isEmpty() ||
                txtAddProductAmount.getText().isEmpty() ||
                txtAddInvestmentPrice.getText().isEmpty() ||
                txtAddInvestmentDate.getValue() == null) {
            displayAlerts.showAlert("Todos los campos son obligatorios.");
            return;
        }

        Long investmentId = parseDataTypes.parseLong(txtId.getText());
        String productName = txtAddProductName.getText();
        Double price = parseDataTypes.parseDouble(txtAddInvestmentPrice.getText());
        Integer amount = parseDataTypes.parseInt(txtAddProductAmount.getText());
        LocalDate receivedDate = txtAddInvestmentDate.getValue();
        String currency = txtAddInvestmentCurrency.getText();

        // Validaciones adicionales
        if (price == null || price <= 0) {
            displayAlerts.showAlert("El precio debe ser un número positivo.");
            return;
        }
        if (amount == null || amount <= 0) {
            displayAlerts.showAlert("La cantidad debe ser un número positivo.");
            return;
        }
        if (currency.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return;
        } else if (!currencyService.existsByCurrencyName(currency)) {
            Currency newCurrency = new Currency(null, currency, client);
            currencyService.save(newCurrency);
        }

        // Aquí deberías implementar la lógica para guardar/actualizar la inversión
        Investment investment = new Investment(
                investmentId,
                productName,
                price,
                currencyService.getCurrencyByName(currency),
                amount,
                amount,
                receivedDate,
                txtAddInvestmentType.getText(),
                clientService.getClientByName(userLogged.getName())
        );
        investmentService.save(investment);
        initializeTableValues();
        cleanForm(null);
    }

    @FXML
    public void removeProduct(ActionEvent actionEvent) {
        Long investmentId = parseDataTypes.parseLong(txtId.getText());
        if (investmentId == null) {
            displayAlerts.showAlert("Debes seleccionar un registro para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("¿Está seguro de eliminar esta inversión?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Investment investmentDB = investmentService.getInvestmentById(investmentId);
            if (investmentDB != null) {
                investmentService.deleteInvestmentById(investmentId);
                initializeTableValues();
                cleanForm(null);
            } else {
                displayAlerts.showAlert("No hay ningún registro con ese ID");
            }
        }
    }

    // ============================
    // LIMPIEZA DE FORMULARIOS Y FILTROS
    // ============================
    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        txtId.clear();
        txtAddProductName.clear();
        txtAddProductAmount.clear();
        txtAddInvestmentPrice.clear();
        txtAddInvestmentDate.setValue(null);
        txtAddInvestmentCurrency.clear();
    }

    @FXML
    public void cleanFilters(ActionEvent actionEvent) {
        txtFilterId.clear();
        txtFilterName.clear();
        txtMinFilterAmount.clear();
        txtMaxFilterAmount.clear();
        txtAddInvestmentCurrency.clear();
        txtMinFilterPrice.setText(String.valueOf(0.00));
        txtMaxFilterPrice.setText(String.valueOf(0.00));
    }

    // ============================
    // FILTRADO DE TABLA
    // ============================
    private void filterInvestmentTable() {
        String id = txtFilterId.getText().trim();
        String name = txtFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(txtMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(txtMaxFilterAmount.getText());
        Double minPrice = parseDataTypes.parseDouble(txtMinFilterPrice.getText());
        Double maxPrice = parseDataTypes.parseDouble(txtMaxFilterPrice.getText());

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

    // ============================
    // MENÚ DINÁMICO DE MONEDAS
    // ============================
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
                txtAddInvestmentCurrency.setText(currency);
            });
            mbCurrency.getItems().add(item);
        }
    }

    // ============================
    // UTILIDADES
    // ============================

    @FXML
    public void selectInventory(Event event) {
        InvestmentDataTable selectedInvestment = tblInvestment.getSelectionModel().getSelectedItem();
        txtId.setText(String.valueOf(selectedInvestment.getId()));
        txtAddInvestmentDate.setValue(selectedInvestment.getReceivedDate());
        txtAddProductName.setText(selectedInvestment.getProductName());
        txtAddProductAmount.setText(String.valueOf(selectedInvestment.getAmount()));
        txtAddInvestmentCurrency.setText(selectedInvestment.getCurrency());
        txtAddInvestmentPrice.setText(String.valueOf(selectedInvestment.getPrice()));
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/balanceView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/sellView.fxml");
    }


}