package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.application.dto.InvestmentDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Lazy
@Controller
public class InvestmentViewController {
    private static final Logger logger = LoggerFactory.getLogger(InvestmentViewController.class);
    private ObservableList<InvestmentDataTable> investmentList;

    // ============================
    // DEPENDENCY INJECTION
    // ============================
    private final ParseDataTypes parseDataTypes;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final ClientServiceImpl clientService;
    private final InvestmentServiceImpl investmentService;
    private final InvestmentRegistryServiceImpl investmentRegistryService;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;

    @Lazy
    public InvestmentViewController(ParseDataTypes parseDataTypes, UserLogged userLogged, WarehouseServiceImpl warehouseService, ClientServiceImpl clientService, InvestmentServiceImpl investmentService, CurrencyServiceImpl currencyService, InvestmentRegistryServiceImpl investmentRegistryService, SceneSwitcher sceneSwitcher) {
        this.currencyService = currencyService;
        this.sceneSwitcher = sceneSwitcher;
        this.investmentService = investmentService;
        this.investmentRegistryService = investmentRegistryService;
        this.clientService = clientService;
        this.warehouseService = warehouseService;
        this.userLogged = userLogged;
        this.parseDataTypes = parseDataTypes;
    }

    // Labels
    @FXML
    private Label txtAddDebugForm, txtClientName;

    // TextFields - Add new Investment
    @FXML
    private TextField txtAddProductName, txtAddProductAmount, txtId, txtAddInvestmentPrice, txtAddInvestmentCurrency, txtAddInvestmentType;

    @FXML
    private TextField txtFilterId, txtFilterName, txtMinFilterPrice, txtMaxFilterPrice, txtMaxFilterAmount, txtMinFilterAmount;

    // Date Pickers
    @FXML
    private DatePicker txtAddInvestmentDate;

    // Table Components
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
    private MenuButton mbCurrency;
    @FXML
    private MenuButton mbInvestmentType;
    @FXML
    private Pagination paginator;

    // ============================
    // INICIALIZACIÓN
    // ============================
    @FXML
    public void initialize() {
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            initCurrencyDefaultValues();
        });
    }

    private void initCurrencyDefaultValues() {
        List<String> defaultCurrenciesName = List.of("MLC", "CUP", "USD", "EUR", "CAD");
        for (String currencyName : defaultCurrenciesName) {
            if (!currencyService.existsByCurrencyName(currencyName)) {
                Currency currency = new Currency(null, currencyName);
                currencyService.save(currency);
            }
        }
    }

    private void setupTextFieldListeners() {
        txtFilterId.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtFilterName.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMinFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMaxFilterAmount.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
        txtMaxFilterPrice.textProperty().addListener((obs, oldVal, newVal) -> filterInvestmentTable());
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
            showAlert("Todos los campos son obligatorios.");
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
            showAlert("El precio debe ser un número positivo.");
            return;
        }
        if (amount == null || amount <= 0) {
            showAlert("La cantidad debe ser un número positivo.");
            return;
        }
        if (currency.isEmpty()) {
            showAlert("Debe seleccionar una moneda");
            return;
        } else if (!currencyService.existsByCurrencyName(currency)) {
            Currency newCurrency = new Currency(null, currency);
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
            showAlert("Debes seleccionar un registro para eliminar.");
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
                showAlert("No hay ningún registro con ese ID");
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
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
        switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        switchView(actionEvent, "/sellView.fxml");
    }

    private void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            sceneSwitcher.setRootWithEvent(actionEvent, fxmlPath);
        } catch (Exception e) {
            logger.error("Error al cambiar de vista", e);
            showAlert("Error al cambiar de vista: " + e.getMessage());
        }
    }
}