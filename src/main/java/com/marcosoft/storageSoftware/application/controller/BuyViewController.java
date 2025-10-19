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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
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

    @FXML
    private Label lblClientName;
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
    private TextField tfMinFilterAmount, tfAddTotalBuyCurrency, tfFilterName, tfAddUnitaryBuyCurrency,
            tfAddBuyName, tfAddUnitaryBuyPrice, tfId, tfAddTotalBuyPrice, tfMaxFilterPrice, tfMaxFilterAmount,
            tfMinFilterPrice, tfAddBuyAmount;
    @Getter
    @FXML
    private TextField tfFilterId;
    @FXML
    private DatePicker dpAddExpenseDate;

    /**
     * Inicializa el controlador después de que se haya cargado el FXML.
     */
    @FXML
    private void initialize() {
        registryZone = "Compras";
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());

        initializeTableColumns();
        initializeTableValues();

        Platform.runLater(() -> {
            initCurrencyMenus();
            cleanForm();
            setupTextFieldListeners();
            setupTableSelectionListener();
        });
    }

    /**
     * Configura los listeners para los campos de texto de filtros y cálculos automáticos.
     */
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
     * Calcula el precio total basado en el precio unitario y la cantidad.
     */
    private void calculateTotalPrice() {
        if (isCalculating) return;
        isCalculating = true;

        try {
            Double unitaryPrice = parseDataTypes.parseDouble(tfAddUnitaryBuyPrice.getText());
            Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());

            if (unitaryPrice != null && amount != null && amount > 0) {
                Double totalPrice = unitaryPrice * amount;
                tfAddTotalBuyPrice.setText(String.format("%.2f", totalPrice));
            } else {
                tfAddTotalBuyPrice.setText("");
            }
        } finally {
            isCalculating = false;
        }
    }

    /**
     * Calcula el precio unitario basado en el precio total y la cantidad.
     */
    private void calculateUnitaryPrice() {
        if (isCalculating) return;
        isCalculating = true;

        try {
            Double totalPrice = parseDataTypes.parseDouble(tfAddTotalBuyPrice.getText());
            Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());

            if (totalPrice != null && amount != null && amount > 0) {
                Double unitaryPrice = totalPrice / amount;
                tfAddUnitaryBuyPrice.setText(String.format("%.2f", unitaryPrice));
            } else {
                tfAddUnitaryBuyPrice.setText("");
            }
        } finally {
            isCalculating = false;
        }
    }

    /**
     * Configura el listener para la selección de la tabla.
     */
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

    /**
     * Inicializa los menús desplegables de moneda.
     */
    private void initCurrencyMenus() {
        initCurrencyMenu(mbUnitaryCurrency);
        initCurrencyMenu(mbTotalCurrency);
    }

    /**
     * Inicializa un menú desplegable de moneda con las opciones disponibles.
     */
    private void initCurrencyMenu(MenuButton mbCurrency) {
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

    /**
     * Configura las columnas de la tabla con sus respectivas propiedades.
     */
    private void initializeTableColumns() {
        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcBuyName.setCellValueFactory(new PropertyValueFactory<>("buyName"));
        tcUnitaryPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("unitaryPriceAndCurrency"));
        tcTotalPriceAndCurrency.setCellValueFactory(new PropertyValueFactory<>("totalPriceAndCurrency"));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));
    }

    /**
     * Inicializa los valores de la tabla cargando todas las compras del cliente.
     */
    public void initializeTableValues() {
        buyList = FXCollections.observableArrayList();
        List<Buy> buysDB = buyService.getAllBuysByClient(client);
        buyList.clear();

        for (Buy buy : buysDB) {
            buyList.add(new BuyDataTable(buy.getBuyId(), buy.getBuyName(),
                    buy.getBuyUnitaryPrice() + " " + buy.getCurrency().getCurrencyName(),
                    buy.getBuyTotalPrice() + " " + buy.getCurrency().getCurrencyName(),
                    buy.getAmount(), buy.getReceivedDate()));
        }

        tvBuy.setItems(buyList);
        filterBuyTable();
    }

    /**
     * Elimina una compra seleccionada después de confirmación.
     */
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

                BuyRegistry buyRegistry = new BuyRegistry(null, buyDB.getBuyId(), buyDB.getBuyName(),
                        buyDB.getBuyUnitaryPrice(), buyDB.getBuyTotalPrice(),
                        buyDB.getCurrency().getCurrencyName(), buyDB.getAmount(),
                        client, registryType, LocalDateTime.now());
                buyRegistryService.save(buyRegistry);

                GeneralRegistry generalRegistry = new GeneralRegistry(null, client, registryZone,
                        registryType, LocalDateTime.now());
                generalRegistryService.save(generalRegistry);
                cleanForm();
            } else {
                displayAlerts.showAlert("No hay ningún registro con ese ID");
            }
        }
    }

    /**
     * Limpia todos los campos de filtro de la interfaz.
     */
    @FXML
    public void cleanFilters() {
        List<TextField> textFieldList = List.of(tfFilterId, tfFilterName, tfMaxFilterAmount,
                tfMaxFilterPrice, tfMinFilterAmount, tfMinFilterPrice);
        cleanHelper.cleanTextFields(textFieldList);
    }

    /**
     * Agrega o actualiza una compra en el sistema.
     * No permite modificar compras que ya han sido asignadas a inventarios.
     */
    @FXML
    public void addOrUpgradeBuy() {
        if (!validateAllFields()) return;

        try {
            Long buyId = parseDataTypes.parseLong(tfId.getText());
            String buyName = tfAddBuyName.getText();
            Double unitaryPrice = parseDataTypes.parseDouble(tfAddUnitaryBuyPrice.getText());
            Double totalPrice = parseDataTypes.parseDouble(tfAddTotalBuyPrice.getText());
            Integer amount = parseDataTypes.parseInt(tfAddBuyAmount.getText());
            LocalDate receivedDate = dpAddExpenseDate.getValue();
            String currencyName = tfAddUnitaryBuyCurrency.getText();

            boolean isUpdate = buyId != null && buyService.existsByBuyId(buyId);
            String registryType = isUpdate ? "Actualización" : "Adición";

            // Verificar si es una actualización y si la compra ya ha sido asignada
            if (isUpdate) {
                Buy existingBuy = buyService.getBuyById(buyId);
                if (existingBuy != null && !existingBuy.getAmount().equals(existingBuy.getLeftAmount())) {
                    displayAlerts.showAlert("No se puede modificar una compra que ya ha sido asignada a algún almacén.");
                    return;
                }
            }

            // Guardar la compra
            saveBuy(buyId, buyName, unitaryPrice, totalPrice, amount, receivedDate, currencyName, registryType);

            initializeTableValues();
            cleanForm();
        } catch (Exception e) {
            log.error("Error al procesar la compra", e);
            displayAlerts.showAlert("Error al procesar la compra: " + e.getMessage());
        }
    }

    /**
     * Guarda una nueva compra o actualiza una existente.
     */
    private void saveBuy(Long buyId, String buyName, Double unitaryPrice, Double totalPrice,
                         Integer amount, LocalDate receivedDate, String currencyName, String registryType) {

        // Para compras nuevas, leftAmount es igual a amount
        // Para compras existentes que se actualizan, leftAmount debe ajustarse
        Integer leftAmount = amount;
        if (buyId != null) {
            Buy existingBuy = buyService.getBuyById(buyId);
            if (existingBuy != null) {
                // Calcular el nuevo leftAmount basado en la diferencia
                int amountDifference = amount - existingBuy.getAmount();
                leftAmount = existingBuy.getLeftAmount() + amountDifference;
                // Asegurar que leftAmount no sea negativo
                leftAmount = Math.max(0, leftAmount);
            }
        }

        Buy buy = new Buy(buyId, buyName, unitaryPrice, totalPrice,
                currencyService.getCurrencyByName(currencyName), amount, leftAmount,
                receivedDate, "Materias Primas y Materiales", client);

        Buy savedBuy = buyService.save(buy);

        // Crear el producto si no existe
        if (!productService.existsByProductNameAndClient(buyName, client)) {
            Product product = new Product(null, buyName, null, client, null);
            productService.save(product);
        }

        createBuyRegistry(savedBuy, registryType);
        createGeneralRegistry(registryType);
    }

    /**
     * Crea un registro específico de compra para auditoría.
     */
    private void createBuyRegistry(Buy buy, String registryType) {
        BuyRegistry buyRegistry = new BuyRegistry(null, buy.getBuyId(), buy.getBuyName(),
                buy.getBuyUnitaryPrice(), buy.getBuyTotalPrice(),
                buy.getCurrency().getCurrencyName(), buy.getAmount(),
                client, registryType, LocalDateTime.now());
        buyRegistryService.save(buyRegistry);
    }

    /**
     * Crea un registro general del sistema para auditoría.
     */
    private void createGeneralRegistry(String registryType) {
        GeneralRegistry generalRegistry = new GeneralRegistry(null, client, registryZone,
                registryType, LocalDateTime.now());
        generalRegistryService.save(generalRegistry);
    }

    /**
     * Valida todos los campos del formulario antes de procesar.
     */
    private boolean validateAllFields() {
        if (tfAddBuyName.getText().isEmpty() || tfAddBuyAmount.getText().isEmpty() ||
                tfAddUnitaryBuyPrice.getText().isEmpty() || tfAddTotalBuyPrice.getText().isEmpty() ||
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

        if (Math.abs(totalPrice - (unitaryPrice * amount)) > 0.01) {
            displayAlerts.showAlert("El precio total debe ser igual al precio unitario multiplicado por la cantidad.");
            return false;
        }

        return true;
    }

    /**
     * Filtra la tabla de compras basándose en los criterios de búsqueda.
     */
    private void filterBuyTable() {
        String id = tfFilterId.getText().trim();
        String name = tfFilterName.getText().trim().toLowerCase();
        Integer minAmount = parseDataTypes.parseInt(tfMinFilterAmount.getText());
        Integer maxAmount = parseDataTypes.parseInt(tfMaxFilterAmount.getText());
        Double minPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMinFilterPrice.getText());
        Double maxPrice = buyAndExpenseSharedMethods.parsePriceFromString(tfMaxFilterPrice.getText());

        Predicate<BuyDataTable> filter = buy -> {
            boolean matches = true;

            if (!id.isEmpty()) matches &= buy.getId() != null && buy.getId().toString().contains(id);
            if (!name.isEmpty()) matches &= buy.getBuyName() != null && buy.getBuyName().toLowerCase().contains(name);
            if (minAmount != null && minAmount > 0) matches &= buy.getAmount() != null && buy.getAmount() >= minAmount;
            if (maxAmount != null && maxAmount > 0) matches &= buy.getAmount() != null && buy.getAmount() <= maxAmount;
            if (minPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(buy.getTotalPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(buy.getTotalPriceAndCurrency()) >= minPrice;
            }
            if (maxPrice != null) {
                matches &= buyAndExpenseSharedMethods.parsePriceFromString(buy.getTotalPriceAndCurrency()) != null &&
                        buyAndExpenseSharedMethods.parsePriceFromString(buy.getTotalPriceAndCurrency()) <= maxPrice;
            }

            return matches;
        };

        ObservableList<BuyDataTable> filteredList = buyList.stream()
                .filter(filter)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tvBuy.setItems(filteredList);
    }

    /**
     * Limpia el formulario de entrada y restablece valores por defecto.
     */
    @FXML
    public void cleanForm() {
        List<TextField> textFieldList = List.of(tfId, tfAddBuyName, tfAddUnitaryBuyPrice,
                tfAddUnitaryBuyCurrency, tfAddTotalBuyPrice, tfAddTotalBuyCurrency);
        cleanHelper.cleanTextFields(textFieldList);
        tfAddBuyAmount.setText("1");
        tfAddUnitaryBuyCurrency.setText("CUP");
        tfAddTotalBuyCurrency.setText("CUP");
        dpAddExpenseDate.setValue(LocalDate.now());
    }

    // Métodos de navegación entre vistas
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