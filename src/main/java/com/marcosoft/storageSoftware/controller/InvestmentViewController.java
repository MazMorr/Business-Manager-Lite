package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.domain.Currency;
import com.marcosoft.storageSoftware.domain.Investment;
import com.marcosoft.storageSoftware.model.InvestmentObservableList;
import com.marcosoft.storageSoftware.model.UserLogged;
import com.marcosoft.storageSoftware.service.impl.*;
import com.marcosoft.storageSoftware.util.ParseDataTypes;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class InvestmentViewController {
    private static final Logger logger = LoggerFactory.getLogger(InvestmentViewController.class);
    private ObservableList<InvestmentObservableList> investmentList;

    // ============================
    // DEPENDENCY INJECTION
    // ============================
    @Autowired
    private ParseDataTypes parseDataTypes;
    @Autowired
    private UserLogged userLogged;
    @Autowired
    private WarehouseServiceImpl warehouseService;
    @Autowired
    private ClientServiceImpl clientService;
    @Autowired
    private InvestmentServiceImpl investmentService;
    @Autowired
    private InvestmentRegistryServiceImpl investmentRegistryService;
    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    private WindowShowing windowShowing;
    @Autowired
    private CurrencyServiceImpl currencyService;

    // Labels
    @FXML
    private Label txtAddDebugForm, txtClientName;

    // TextFields - Add new Investment
    @FXML
    private TextField txtAddProductName, txtAddProductAmount, txtId, txtAddInvestmentPrice, txtAddInvestmentCurrency;

    @FXML
    private TextField txtFilterId, txtFilterName, txtMinFilterPrice, txtMaxFilterPrice, txtMaxFilterAmount, txtMinFilterAmount;

    // Date Pickers
    @FXML
    private DatePicker txtAddInvestmentDate;

    // Table Components
    @FXML
    private TableView<InvestmentObservableList> tblInvestment;
    @FXML
    private TableColumn<InvestmentObservableList, Integer> amountColumn;
    @FXML
    private TableColumn<InvestmentObservableList, String> nameColumn, currencyColumn;
    @FXML
    private TableColumn<InvestmentObservableList, LocalDate> dateColumn;
    @FXML
    private TableColumn<InvestmentObservableList, Long> idColumn;
    @FXML
    private TableColumn<InvestmentObservableList, Double> priceColumn;
    @FXML
    private MenuButton mbCurrency;


    // ============================
    // INICIALIZACIÓN
    // ============================
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initializeTableValues();
            setupTextFieldListeners();
            setupTableSelectionListener();
            updateCurrencyMenu();
            txtClientName.setText(userLogged.getName());
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
            investmentList.add(new InvestmentObservableList(
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
                receivedDate,
                clientService.getClientByName(userLogged.getName()),
                false
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

        Predicate<InvestmentObservableList> filter = investment -> {
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

        ObservableList<InvestmentObservableList> filteredList = investmentList.stream()
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
        InvestmentObservableList selectedInvestment = tblInvestment.getSelectionModel().getSelectedItem();
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
    public void switchToInventory(ActionEvent actionEvent) {
        switchView(actionEvent, "/inventoryView.fxml");
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