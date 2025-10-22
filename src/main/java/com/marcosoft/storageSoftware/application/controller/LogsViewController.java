package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.*;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class LogsViewController {

    ObservableList<GeneralRegistryDataTable> generalRegistryDataTables;
    ObservableList<BuyRegistryDataTable> buyRegistryDataTables;
    ObservableList<ExpenseRegistryDataTable> expenseRegistryDataTables;
    ObservableList<SellRegistryDataTable> sellRegistryDataTables;
    ObservableList<WarehouseRegistryDataTable> warehouseRegistryDataTables;

    private Client client;
    private DateTimeFormatter formatter;

    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final ExpenseRegistryServiceImpl expenseRegistryService;
    private final BuyRegistryServiceImpl buyRegistryService;
    private final DisplayAlerts displayAlerts;

    //General Registry Table
    @FXML
    private TableView<GeneralRegistryDataTable> tvGeneralRegistry;
    @FXML
    private TableColumn<GeneralRegistryDataTable, LocalDateTime> tcGeneralRegistryDateTime;
    @FXML
    private TableColumn<GeneralRegistryDataTable, String> tcGeneralRegistryType, tcGeneralRegistryZone;

    //Expense Registry Table
    @FXML
    private TableView<ExpenseRegistryDataTable> tvExpense;
    @FXML
    private TableColumn<ExpenseRegistryDataTable, String> tcExpenseName, tcExpensePriceCurrency, tcExpenseRegistryType;
    @FXML
    private TableColumn<ExpenseRegistryDataTable, Long> tcIdExpense;
    @FXML
    private TableColumn<ExpenseRegistryDataTable, LocalDateTime> tcExpenseRegistryDateTime;

    // Buy Registry Table
    @FXML
    private TableView<BuyRegistryDataTable> tvBuy;
    @FXML
    private TableColumn<BuyRegistryDataTable, Long> tcIdBuy;
    @FXML
    private TableColumn<BuyRegistryDataTable, String> tcBuyRegistryType, tcBuyName, tcBuyTotalPrice, tcBuyUnitaryPrice;
    @FXML
    private TableColumn<BuyRegistryDataTable, LocalDateTime> tcBuyRegistryDateTime;
    @FXML
    private TableColumn<BuyRegistryDataTable, Integer> tcBuyAmount;


    //Sell Registry Table
    @FXML
    private TableView<SellRegistryDataTable> tvSell;
    @FXML
    private TableColumn<SellRegistryDataTable, String>
            tcSellProductName, tcSellRegistryType, tcSellPriceCurrency, tcSellWarehouse;
    @FXML
    private TableColumn<SellRegistryDataTable, LocalDate> tcSellDate;
    @FXML
    private TableColumn<SellRegistryDataTable, LocalDateTime> tcSellRegistryDateTime;
    @FXML
    private TableColumn<SellRegistryDataTable, Integer> tcSellAmount;


    //Warehouse Registry Table
    @FXML
    private TableView<WarehouseRegistryDataTable> tvWarehouse;
    @FXML
    private TableColumn<WarehouseRegistryDataTable, String> tcWarehouseName, tcWarehouseProduct, tcWarehouseRegistryType;
    @FXML
    private TableColumn<WarehouseRegistryDataTable, LocalDateTime> tcWarehouseDateTime;
    @FXML
    private TableColumn<WarehouseRegistryDataTable, Integer> tcWarehouseAmount;

    @FXML
    private DatePicker dpBuyFilter;
    @FXML
    private Label txtClientName;
    @FXML
    private DatePicker dpGeneralFilter, dpExpenseFilter, dpWarehouseFilter, dpSellFilter;
    @FXML
    private Tab tabGeneral, tabSell, tabInvestment, tabWarehouse, tabBuy;
    @FXML
    private TabPane tbpRegistry;

    @FXML
    public void initialize() {
        client = userLogged.getClient();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            // Load initial data for tables
            loadDataForSelectedTab();
            establishFilterListener();
            // Listener para cambios de pestaña
            tbpRegistry.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldTab, newTab) -> loadDataForSelectedTab());
        });
    }

    private void establishFilterListener() {
        dpGeneralFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDataForSelectedTab());
        dpExpenseFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDataForSelectedTab());
        dpBuyFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDataForSelectedTab());
        dpSellFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDataForSelectedTab());
        dpWarehouseFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDataForSelectedTab());
    }

    private void loadDataForSelectedTab() {
        Tab selectedTab = tbpRegistry.getSelectionModel().getSelectedItem();

        if (selectedTab == null) return;

        if (selectedTab.equals(tabGeneral)) {
            initGeneralTableColumns();
            initGeneralRegistryTableValues();
        } else if (selectedTab.equals(tabInvestment)) {
            initExpenseTableColumns();
            initExpenseRegistryTableValues();
        } else if (selectedTab.equals(tabSell)) {
            initSellTableColumns();
            initSellRegistryTableValues();
        } else if (selectedTab.equals(tabWarehouse)) {
            initWarehouseTableColumns();
            initWarehouseRegistryTableValues();
        } else if (selectedTab.equals(tabBuy)) {
            initBuyTableColumns();
            initBuyRegistryTableValues();
        }
    }

    @FXML
    private void cleanDatePickers() {
        dpExpenseFilter.setValue(null);
        dpBuyFilter.setValue(null);
        dpSellFilter.setValue(null);
        dpWarehouseFilter.setValue(null);
        dpGeneralFilter.setValue(null);
    }

    private void initGeneralTableColumns(){
        tcGeneralRegistryZone.setCellValueFactory(new PropertyValueFactory<>("affectedZone"));
        tcGeneralRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcGeneralRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDateTime"));
    }

    private void initGeneralRegistryTableValues() {
        generalRegistryDataTables = FXCollections.observableArrayList();
        List<GeneralRegistry> generalRegistries = generalRegistryService.getAllGeneralRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        generalRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));
        generalRegistryDataTables.clear();

        for (GeneralRegistry generalRegistry : generalRegistries) {
            if (dpGeneralFilter.getValue() != null) {
                if (generalRegistry.getRegistryDateTime().toLocalDate().isEqual(dpGeneralFilter.getValue())) {
                    generalRegistryDataTables.add(new GeneralRegistryDataTable(
                            generalRegistry.getAffectedZone(),
                            generalRegistry.getRegistryType(),
                            generalRegistry.getRegistryDateTime()
                    ));
                }
            } else {
                generalRegistryDataTables.add(new GeneralRegistryDataTable(
                        generalRegistry.getAffectedZone(),
                        generalRegistry.getRegistryType(),
                        generalRegistry.getRegistryDateTime()
                ));
            }
        }

        // Formatear LocalDateTime
        tcGeneralRegistryDateTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        tvGeneralRegistry.setItems(generalRegistryDataTables);
    }

    private void initBuyTableColumns(){
        tcBuyRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcBuyRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
        tcIdBuy.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcBuyName.setCellValueFactory(new PropertyValueFactory<>("buyName"));
        tcBuyUnitaryPrice.setCellValueFactory(new PropertyValueFactory<>("unitaryPriceAndCurrency"));
        tcBuyTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPriceAndCurrency"));
        tcBuyAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

    }

    private void initBuyRegistryTableValues() {
        buyRegistryDataTables = FXCollections.observableArrayList();
        List<BuyRegistry> buyRegistries = buyRegistryService.getAllBuyRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        buyRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

        buyRegistryDataTables.clear();

        for (BuyRegistry buy : buyRegistries) {
            LocalDate dpBuyValue = dpBuyFilter.getValue();
            if (dpBuyValue != null) {
                if (buy.getRegistryDateTime().toLocalDate().isEqual(dpBuyValue)) {
                    buyRegistryDataTables.add(new BuyRegistryDataTable(
                            buy.getRegistryType(),
                            buy.getRegistryDateTime(),
                            buy.getBuyId(),
                            buy.getBuyName(),
                            buy.getBuyUnitaryPrice() + buy.getCurrency(),
                            buy.getBuyTotalPrice() + buy.getCurrency(),
                            buy.getAmount()
                    ));
                }
            } else {
                buyRegistryDataTables.add(new BuyRegistryDataTable(
                        buy.getRegistryType(),
                        buy.getRegistryDateTime(),
                        buy.getBuyId(),
                        buy.getBuyName(),
                        buy.getBuyUnitaryPrice() + buy.getCurrency(),
                        buy.getBuyTotalPrice() + buy.getCurrency(),
                        buy.getAmount()
                ));
            }
        }

        // Formatear LocalDateTime
        tcBuyRegistryDateTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        tvBuy.setItems(buyRegistryDataTables);
    }

    private void initWarehouseTableColumns(){
        tcWarehouseRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcWarehouseDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDateTime"));
        tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcWarehouseProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcWarehouseAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void initWarehouseRegistryTableValues() {
        warehouseRegistryDataTables = FXCollections.observableArrayList();
        List<WarehouseRegistry> warehouseRegistries = warehouseRegistryService.getAllWarehouseRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        warehouseRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

        warehouseRegistryDataTables.clear();

        for (WarehouseRegistry w : warehouseRegistries) {
            LocalDate dpWarehouseValue = dpWarehouseFilter.getValue();
            if (dpWarehouseValue != null) {
                if (w.getRegistryDateTime().toLocalDate().isEqual(dpWarehouseValue)) {
                    warehouseRegistryDataTables.add(new WarehouseRegistryDataTable(
                            w.getRegistryType(),
                            w.getRegistryDateTime(),
                            w.getWarehouseName(),
                            w.getProductName(),
                            w.getAmount()
                    ));
                }
            } else {
                warehouseRegistryDataTables.add(new WarehouseRegistryDataTable(
                        w.getRegistryType(),
                        w.getRegistryDateTime(),
                        w.getWarehouseName(),
                        w.getProductName(),
                        w.getAmount()
                ));
            }
        }

        // Formatear LocalDateTime
        tcWarehouseDateTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        tvWarehouse.setItems(warehouseRegistryDataTables);
    }

    private void initSellTableColumns(){
        tcSellRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcSellRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
        tcSellProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcSellPriceCurrency.setCellValueFactory(new PropertyValueFactory<>("sellPriceAndCurrency"));
        tcSellDate.setCellValueFactory(new PropertyValueFactory<>("sellDate"));
        tcSellWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcSellAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void initSellRegistryTableValues() {
        sellRegistryDataTables = FXCollections.observableArrayList();
        List<SellRegistry> sellRegistries = sellRegistryService.getAllSellRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        sellRegistries.sort((r1, r2) -> r2.getRegistryDate().compareTo(r1.getRegistryDate()));

        sellRegistryDataTables.clear();

        for (SellRegistry sellRegistry : sellRegistries) {
            LocalDate dpSellValue = dpSellFilter.getValue();
            if (dpSellValue != null) {
                if (sellRegistry.getRegistryDate().toLocalDate().isEqual(dpSellValue)) {
                    sellRegistryDataTables.add(new SellRegistryDataTable(
                            sellRegistry.getRegistryType(),
                            sellRegistry.getRegistryDate(),
                            sellRegistry.getProductName(),
                            sellRegistry.getSellPrice() + " " + sellRegistry.getSellCurrency(),
                            sellRegistry.getSellDate(),
                            sellRegistry.getWarehouseName(),
                            sellRegistry.getProductAmount()
                    ));
                }
            } else {
                sellRegistryDataTables.add(new SellRegistryDataTable(
                        sellRegistry.getRegistryType(),
                        sellRegistry.getRegistryDate(),
                        sellRegistry.getProductName(),
                        sellRegistry.getSellPrice() + " " + sellRegistry.getSellCurrency(),
                        sellRegistry.getSellDate(),
                        sellRegistry.getWarehouseName(),
                        sellRegistry.getProductAmount()
                ));
            }
        }

        // Formatear LocalDateTime
        tcSellRegistryDateTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        tvSell.setItems(sellRegistryDataTables);
    }

    private void initExpenseTableColumns(){
        tcExpenseRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcExpenseRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
        tcIdExpense.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        tcExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tcExpensePriceCurrency.setCellValueFactory(new PropertyValueFactory<>("buyPriceAndCurrency"));
    }

    private void initExpenseRegistryTableValues() {
        try {
            expenseRegistryDataTables = FXCollections.observableArrayList();
            List<ExpenseRegistry> investmentRegistries = expenseRegistryService.getAllExpenseRegistryByClient(client);

            // Ordenar por fecha más reciente primero
            investmentRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

            expenseRegistryDataTables.clear();

            for (ExpenseRegistry expenseRegistry : investmentRegistries) {
                // Formatear el precio y moneda
                String priceCurrency = String.format("%.2f %s",
                        expenseRegistry.getExpensePrice(),
                        expenseRegistry.getCurrency());
                LocalDate dpExpenseValue = dpExpenseFilter.getValue();

                if (dpExpenseValue != null) {
                    if (expenseRegistry.getRegistryDateTime().toLocalDate().isEqual(dpExpenseValue)) {
                        expenseRegistryDataTables.add(new ExpenseRegistryDataTable(
                                expenseRegistry.getRegistryType(),
                                expenseRegistry.getRegistryDateTime(),
                                expenseRegistry.getExpenseId(),
                                expenseRegistry.getExpenseName(),
                                priceCurrency
                        ));
                    }
                } else {
                    expenseRegistryDataTables.add(new ExpenseRegistryDataTable(
                            expenseRegistry.getRegistryType(),
                            expenseRegistry.getRegistryDateTime(),
                            expenseRegistry.getExpenseId(),
                            expenseRegistry.getExpenseName(),
                            priceCurrency
                    ));
                }

            }

            // Formatear LocalDateTime
            tcExpenseRegistryDateTime.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(formatter));
                    }
                }
            });

            tvExpense.setItems(expenseRegistryDataTables);

        } catch (Exception e) {
            displayAlerts.showError("Ha ocurrido un error: " + e.getMessage());
        }
    }

    @FXML
    private void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchToConfiguration(actionEvent);
    }

    @FXML
    private void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchToSupport(actionEvent);
    }

    @FXML
    private void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchToExpense(actionEvent);
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
    public void switchToBuy(ActionEvent actionEvent) {
        sceneSwitcher.switchToBuy(actionEvent);
    }

    @FXML
    private void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchToSell(actionEvent);
    }
}
