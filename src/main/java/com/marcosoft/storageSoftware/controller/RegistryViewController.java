package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.domain.Investment;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.service.impl.InvestmentServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class RegistryViewController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RegistryViewController.class);

    private ObservableList<Investment> investments;
    private FilteredList<Investment> filteredInvestments;

    @FXML
    private TableView<Investment> tblRegistry;
    @FXML
    public TextField txtFilterName;
    @FXML
    private TableColumn<?, ?> quantityColumn, currencyColumn, nameColumn, dateColumn, categoryColumn,
            priceColumn, transactionTypeColumn, storedInColumn;
    @FXML
    private Label txtClientName;

    @Autowired
    private WindowShowing windowShowing;
    @Autowired
    private ClientServiceImpl clientServiceImpl;
    @Autowired
    private InvestmentServiceImpl investmentService;
    @Autowired
    private SceneSwitcher sceneSwitcher;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        investments = FXCollections.observableArrayList();
        filteredInvestments = new FilteredList<>(investments, t -> true);
        tblRegistry.setItems(filteredInvestments);

        // Configuración de columnas
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("transactionStock"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("transactionPrice"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currencyName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        storedInColumn.setCellValueFactory(new PropertyValueFactory<>("transactionStorage"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));

        // Placeholder para tabla vacía
        Label placeholder = new Label("No hay transacciones registradas");
        placeholder.setPadding(new Insets(20));
        tblRegistry.setPlaceholder(placeholder);

        // Filtro reactivo por nombre
        txtFilterName.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredInvestments.setPredicate(transaction -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String name = transaction.getProductName();
                return name != null && name.toLowerCase().contains(newVal.toLowerCase());
            });
        });

        loadTransactionsAsync();

        txtClientName.setText(clientServiceImpl.getByIsClientActive(true).getClientName());
    }

    @FXML
    private void switchToSupport(ActionEvent event) {
        switchView(event, "/supportView.fxml");
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) {
        switchView(event, "/configurationView.fxml");
    }

    @FXML
    private void switchToWallet(ActionEvent event) {
        switchView(event, "/walletView.fxml");
    }

    @FXML
    private void switchToStock(ActionEvent event) {
        switchView(event, "/investmentView.fxml");
    }

    private void switchView(ActionEvent event, String fxml) {
        sceneSwitcher.setRootWithEvent(event, fxml);
        windowShowing.closeAllWindows();
    }

    @FXML
    private void selected(MouseEvent event) {
        // Puedes implementar lógica adicional aquí si lo necesitas
        Investment investment = tblRegistry.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void txtFilterNameChanged(KeyEvent event) {
        // Ya no es necesario, el filtro es reactivo con el listener en initialize()
    }

    private void loadTransactionsAsync() {
        //TODO
    }
}
