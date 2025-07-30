package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.GeneralRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.InvestmentRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.SellRegistryDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseRegistryDataTable;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.model.InvestmentRegistry;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.GeneralRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InvestmentRegistryServiceImpl;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Lazy
@Controller
public class RegistryViewController {

    ObservableList<GeneralRegistryDataTable> generalRegistryDataTables;
    ObservableList<InvestmentRegistryDataTable> investmentRegistryDataTables;
    ObservableList<SellRegistryDataTable> sellRegistryDataTables;
    ObservableList<WarehouseRegistryDataTable> warehouseRegistryDataTables;

    private Client client;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;
    private final SellRegistryServiceImpl sellRegistryService;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final InvestmentRegistryServiceImpl investmentRegistryService;
    private final DisplayAlerts displayAlerts;

    @Lazy
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

    @FXML
    public void initialize() {
        client = clientService.getClientByName(userLogged.getName());
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

        tvGeneralRegistry.setItems(generalRegistryDataTables);

    }

    private void initWarehouseRegistryTableValues() {

        warehouseRegistryDataTables = FXCollections.observableArrayList();
        List<WarehouseRegistry> warehouseRegistries = warehouseRegistryService.getAllWarehouseRegistriesByClient(client);
        warehouseRegistryDataTables.clear();

        for (WarehouseRegistry w : warehouseRegistries) {
            warehouseRegistryDataTables.add(new WarehouseRegistryDataTable(
                    w.getRegistryType(),
                    w.getRegistryDateTime(),
                    w.getWarehouse().getWarehouseName(),
                    w.getProduct().getProductName(),
                    w.getAmount()
            ));
        }

        tcWarehouseRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
        tcWarehouseDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDateTime"));
        tcWarehouseName.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        tcWarehouseProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        tcWarehouseAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        tvWarehouse.setItems(warehouseRegistryDataTables);
    }

    private void initSellRegistryTableValues() {

        sellRegistryDataTables = FXCollections.observableArrayList();
        List<SellRegistry> sellRegistries = sellRegistryService.getAllSellRegistriesByClient(client);
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
        tcSellAmount.setCellValueFactory(new PropertyValueFactory<>("productAmount"));

        tvSell.setItems(sellRegistryDataTables);
    }

    private void initInvestmentRegistryTableValues() {
        try {
            investmentRegistryDataTables = FXCollections.observableArrayList();
            List<InvestmentRegistry> investmentRegistries = investmentRegistryService.getAllInvestmentRegistryByClient(client);
            investmentRegistryDataTables.clear();

            for (InvestmentRegistry investmentRegistry : investmentRegistries) {
                Investment investment = investmentRegistry.getInvestment();

                // Verificar si la inversión existe
                if (investment != null) {
                    // Formatear el precio y moneda
                    String priceCurrency = String.format("%.2f %s",
                            investment.getInvestmentPrice(),
                            investment.getCurrency());

                    // Agregar a la lista observable
                    investmentRegistryDataTables.add(new InvestmentRegistryDataTable(
                            investmentRegistry.getRegistryType(),
                            investmentRegistry.getRegistryDateTime(),
                            investment.getInvestmentId(),
                            investment.getInvestmentName(),
                            priceCurrency
                    ));
                }
            }

            tcInvestmentRegistryType.setCellValueFactory(new PropertyValueFactory<>("registryType"));
            tcInvestmentRegistryDateTime.setCellValueFactory(new PropertyValueFactory<>("registryDate"));
            tcIdInvestment.setCellValueFactory(new PropertyValueFactory<>("investmentId"));
            tcInvestmentName.setCellValueFactory(new PropertyValueFactory<>("investmentName"));
            tcInvestmentPriceCurrency.setCellValueFactory(new PropertyValueFactory<>("buyPriceAndCurrency"));

            tvInvestment.setItems(investmentRegistryDataTables);

        } catch (Exception e) {
            displayAlerts.showAlert("Ha ocurrido un error: " + e.getMessage());
        }
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
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/investmentView.fxml");
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
