package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.*;
import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The type Registry view controller.
 */
@Lazy
@Controller
public class RegistryViewController {

    /**
     * The General registry data tables.
     */
    ObservableList<GeneralRegistryDataTable> generalRegistryDataTables;
    /**
     * The Investment registry data tables.
     */
    ObservableList<InvestmentRegistryDataTable> investmentRegistryDataTables;
    /**
     * The Sell registry data tables.
     */
    ObservableList<SellRegistryDataTable> sellRegistryDataTables;
    /**
     * The Warehouse registry data tables.
     */
    ObservableList<WarehouseRegistryDataTable> warehouseRegistryDataTables;

    private Client client;
    private DateTimeFormatter formatter;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final InvestmentRegistryServiceImpl investmentRegistryService;
    private final DisplayAlerts displayAlerts;

    /**
     * Instantiates a new Registry view controller.
     *
     * @param investmentRegistryService the investment registry service
     * @param sellRegistryService the sell registry service
     * @param generalRegistryService the general registry service
     * @param warehouseRegistryService the warehouse registry service
     * @param userLogged the user logged
     * @param clientService the client service
     * @param sceneSwitcher the scene switcher
     * @param displayAlerts the display alerts
     */
    public RegistryViewController(
            InvestmentRegistryServiceImpl investmentRegistryService, SellRegistryServiceImpl sellRegistryService,
            GeneralRegistryServiceImpl generalRegistryService, WarehouseRegistryServiceImpl warehouseRegistryService,
            UserLogged userLogged, ClientServiceImpl clientService, SceneSwitcher sceneSwitcher, DisplayAlerts displayAlerts
    ) {
        this.userLogged = userLogged;
        this.displayAlerts = displayAlerts;
        this.investmentRegistryService = investmentRegistryService;
        this.sellRegistryService = sellRegistryService;
        this.warehouseRegistryService = warehouseRegistryService;
        this.generalRegistryService = generalRegistryService;
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
    }

    //General Registry Table
    @FXML
    private TableView<GeneralRegistryDataTable> tvGeneralRegistry;
    @FXML
    private TableColumn<GeneralRegistryDataTable, LocalDateTime> tcGeneralRegistryDateTime;
    @FXML
    private TableColumn<GeneralRegistryDataTable, String> tcGeneralRegistryType, tcGeneralRegistryZone;

    //Investment Registry Table
    @FXML
    private TableView<InvestmentRegistryDataTable> tvInvestment;
    @FXML
    private TableColumn<InvestmentRegistryDataTable, String> tcInvestmentName, tcInvestmentPriceCurrency, tcInvestmentRegistryType;
    @FXML
    private TableColumn<InvestmentRegistryDataTable, Long> tcIdInvestment;
    @FXML
    private TableColumn<InvestmentRegistryDataTable, LocalDateTime> tcInvestmentRegistryDateTime;


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

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
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
            initInvestmentRegistryTableValues();
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

    private void initInvestmentRegistryTableValues() {
        try {
            investmentRegistryDataTables = FXCollections.observableArrayList();
            List<InvestmentRegistry> investmentRegistries = investmentRegistryService.getAllInvestmentRegistryByClient(client);

            // Ordenar por fecha más reciente primero
            investmentRegistries.sort((r1, r2) -> r2.getRegistryDateTime().compareTo(r1.getRegistryDateTime()));

            investmentRegistryDataTables.clear();

            for (InvestmentRegistry investmentRegistry : investmentRegistries) {
                // Formatear el precio y moneda
                String priceCurrency = String.format("%.2f %s",
                        investmentRegistry.getInvestmentPrice(),
                        investmentRegistry.getCurrency());

                // Agregar a la lista observable
                investmentRegistryDataTables.add(new InvestmentRegistryDataTable(
                        investmentRegistry.getRegistryType(),
                        investmentRegistry.getRegistryDateTime(),
                        investmentRegistry.getInvestmentId(),
                        investmentRegistry.getInvestmentName(),
                        priceCurrency
                ));
            }

            tcInvestmentRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
            tcInvestmentRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
            tcIdInvestment.setCellValueFactory(new PropertyValueFactory<>("investmentId"));
            tcInvestmentName.setCellValueFactory(new PropertyValueFactory<>("investmentName"));
            tcInvestmentPriceCurrency.setCellValueFactory(new PropertyValueFactory<>("buyPriceAndCurrency"));

            // Formatear LocalDateTime
            tcInvestmentRegistryDateTime.setCellFactory(column -> new TableCell<>() {
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

            tvInvestment.setItems(investmentRegistryDataTables);

        } catch (Exception e) {
            displayAlerts.showError("Ha ocurrido un error: " + e.getMessage());
        }
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
     * Switch to investment.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/investmentView.fxml");
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
}
