package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.ExpenseRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.GeneralRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.SellRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseRegistryDataTable;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.ExpenseRegistry;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ExpenseRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Lazy
@Controller
public class RegistryViewController {

    ObservableList<GeneralRegistryDataTable> generalRegistryDataTables;
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
    private final DisplayAlerts displayAlerts;

    public RegistryViewController(
            ExpenseRegistryServiceImpl expenseRegistryService, SellRegistryServiceImpl sellRegistryService,
            GeneralRegistryServiceImpl generalRegistryService, WarehouseRegistryServiceImpl warehouseRegistryService,
            UserLogged userLogged, SceneSwitcher sceneSwitcher, DisplayAlerts displayAlerts
    ) {
        this.userLogged = userLogged;
        this.displayAlerts = displayAlerts;
        this.expenseRegistryService = expenseRegistryService;
        this.sellRegistryService = sellRegistryService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.generalRegistryService = generalRegistryService;
        this.sceneSwitcher = sceneSwitcher;
    }

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


    //Sell Registry Table
    @FXML
    private TableView<SellRegistryDataTable> tvSell;
    @FXML
    private TableColumn<SellRegistryDataTable, String> tcSellProductName, tcSellRegistryType, tcSellPriceCurrency, tcSellWarehouse;
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
    private Label txtClientName;
    @FXML
    private Tab tabGeneral, tabSell, tabInvestment, tabWarehouse;
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

            // Listener para cambios de pestaña
            tbpRegistry.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldTab, newTab) -> loadDataForSelectedTab());
        });
    }

    private void loadDataForSelectedTab() {
        Tab selectedTab = tbpRegistry.getSelectionModel().getSelectedItem();

        if (selectedTab == null) return;

        if (selectedTab.equals(tabGeneral)) {
            initGeneralRegistryTableValues();
        } else if (selectedTab.equals(tabInvestment)) {
            initExpenseRegistryTableValues();
        } else if (selectedTab.equals(tabSell)) {
            initSellRegistryTableValues();
        } else if (selectedTab.equals(tabWarehouse)) {
            initWarehouseRegistryTableValues();
        }
    }

    private void initGeneralRegistryTableValues() {
        generalRegistryDataTables = FXCollections.observableArrayList();
        List<GeneralRegistry> generalRegistries = generalRegistryService.getAllGeneralRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        generalRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

        generalRegistryDataTables.clear();

        for (GeneralRegistry generalRegistry : generalRegistries) {
            generalRegistryDataTables.add(new GeneralRegistryDataTable(
                    generalRegistry.getAffectedZone(),
                    generalRegistry.getRegistryType(),
                    generalRegistry.getRegistryDateTime()
            ));
        }

        tcGeneralRegistryZone.setCellValueFactory(new PropertyValueFactory<>("affectedZone"));
        tcGeneralRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcGeneralRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDateTime"));

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

    private void initWarehouseRegistryTableValues() {
        warehouseRegistryDataTables = FXCollections.observableArrayList();
        List<WarehouseRegistry> warehouseRegistries = warehouseRegistryService.getAllWarehouseRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        warehouseRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

        warehouseRegistryDataTables.clear();

        for (WarehouseRegistry w : warehouseRegistries) {
            warehouseRegistryDataTables.add(new WarehouseRegistryDataTable(
                    w.getRegistryType(),
                    w.getRegistryDateTime(),
                    w.getWarehouseName(),
                    w.getProductName(),
                    w.getAmount()
            ));
        }

        tcWarehouseRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcWarehouseDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDateTime"));
        tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcWarehouseProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcWarehouseAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

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

    private void initSellRegistryTableValues() {
        sellRegistryDataTables = FXCollections.observableArrayList();
        List<SellRegistry> sellRegistries = sellRegistryService.getAllSellRegistriesByClient(client);

        // Ordenar por fecha más reciente primero
        sellRegistries.sort((r1, r2) -> r2.getRegistryDate().compareTo(r1.getRegistryDate()));

        sellRegistryDataTables.clear();

        for (SellRegistry sellRegistry : sellRegistries) {
            sellRegistryDataTables.add(new SellRegistryDataTable(
                    sellRegistry.getRegistryType(),
                    sellRegistry.getRegistryDate(),
                    sellRegistry.getProductName(),
                    sellRegistry.getSellPrice() + sellRegistry.getSellCurrency(),
                    sellRegistry.getSellDate(),
                    sellRegistry.getWarehouseName(),
                    sellRegistry.getProductAmount()
            ));
        }

        tcSellRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcSellRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
        tcSellProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcSellPriceCurrency.setCellValueFactory(new PropertyValueFactory<>("sellPriceAndCurrency"));
        tcSellDate.setCellValueFactory(new PropertyValueFactory<>("sellDate"));
        tcSellWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcSellAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

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
                        expenseRegistry.getInvestmentPrice(),
                        expenseRegistry.getCurrency());

                // Agregar a la lista observable
                expenseRegistryDataTables.add(new ExpenseRegistryDataTable(
                        expenseRegistry.getRegistryType(),
                        expenseRegistry.getRegistryDateTime(),
                        expenseRegistry.getInvestmentId(),
                        expenseRegistry.getInvestmentName(),
                        priceCurrency
                ));
            }

            tcExpenseRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
            tcExpenseRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
            tcIdExpense.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
            tcExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
            tcExpensePriceCurrency.setCellValueFactory(new PropertyValueFactory<>("buyPriceAndCurrency"));

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
    public void switchToConfiguration(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }
}
