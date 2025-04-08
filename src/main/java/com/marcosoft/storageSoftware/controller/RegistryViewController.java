package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.Product;
import com.marcosoft.storageSoftware.model.Transaction;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.service.impl.TransactionServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class RegistryViewController implements Initializable {

    ObservableList<Transaction> transactions;

    @FXML
    private TableView<Transaction> tblRegistry;
    @FXML
    public TextField txtFilterName;
    @FXML
    private TableColumn quantityColumn, currencyColumn, nameColumn, dateColumn, categoryColumn,
            priceColumn, transactionTypeColumn, storedInColumn;
    @FXML
    private Label txtClientName;

    @Autowired
    WindowShowing windowShowing;
    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    TransactionServiceImpl transactionServiceImpl;
    @Autowired
    private SceneSwitcher sceneSwitcher;

    @FXML
    private void switchToSupport(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/supportView.fxml");
        windowShowing.closeAllWindows();
    }

    @Deprecated
    private void switchToExistency(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/stockView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/configurationView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void selected(MouseEvent event) {
        Transaction transaction = tblRegistry.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        transactions = FXCollections.observableArrayList();
        tblRegistry.setItems(transactions);

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

        // Cargar productos existentes y actualizar contadores
        loadProductsAsync();

        txtClientName.setText(clientServiceImpl.getByIsClientActive(true).getClientName());
    }

    @FXML
    public void switchToWallet(ActionEvent event) {
        try {
            sceneSwitcher.setRoot(event, "/walletView.fxml");
            windowShowing.closeAllWindows();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void txtFilterNameChanged(KeyEvent event) {
    }

    @FXML
    public void switchToStock(ActionEvent event) {
        try {
            sceneSwitcher.setRoot(event, "/stockView.fxml");
            windowShowing.closeAllWindows();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProductsAsync() {
        Task<java.util.List<Transaction>> task = new Task<>() {
            @Override
            protected java.util.List<Transaction> call() {
                // Obtener transacciones del servicio
                return transactionServiceImpl.getByClientId_IsClientActiveOrderByTransactionIdAsc(true);
            }
        };

        task.setOnSucceeded(event -> {
            List<Transaction> loadedTrasactions = task.getValue();

            // Limpia la lista observable antes de agregar nuevos productos
            transactions.clear();
            transactions.addAll(loadedTrasactions);
            tblRegistry.refresh();

        });

        new Thread(task).start();
    }
}
