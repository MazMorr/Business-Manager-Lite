package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.domain.Investment;
import com.marcosoft.storageSoftware.domain.Product;
import com.marcosoft.storageSoftware.model.RegistryType;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.transformation.FilteredList;

@Controller
public class StockViewController {

    private static final Logger logger = LoggerFactory.getLogger(StockViewController.class);

    @FXML
    private TableView<Product> tblStock;
    @FXML
    private TableColumn nameColumn, amountColumn, currencySellColumn, currencyBuyColumn, storedInColumn, sellColumn, buyColumn, categoryColumn;
    @FXML
    private TextField txtAddProductCategory, txtAddProductName, txtAddProductStoredIn, txtAddProductQuantity, txtAddProductBuyPrice, txtAddProductSellPrice, txtSellProductSellCategory, txtFilterName, txtSellProductSellName, txtSellProductSellStored, txtSellProductSellQuantity, txtSellProductSellPrice;
    @FXML
    private DatePicker txtSellProductDatePicker;
    @FXML
    private RadioMenuItem rmiSellCUP, rmiSellMLC, rmiSellUSD, rmiSellEUR, rmiBuyMLC, rmiBuyCUP, rmiBuyEUR, rmiBuyUSD, radioSellUSD, radioSellEUR, radioSellCUP, radioSellMLC;
    @FXML
    private ToggleGroup sellCurrency, buyCurrency, sellCurrencyInSell;
    @FXML
    private Label txtSellDebugForm, txtAddDebugForm, txtClientName, txtWarning, txtAlert;
    @FXML
    private Tab sellPane, addPane;

    private final WindowShowing windowShowing;
    private ObservableList<Product> products;
    private FilteredList<Product> filteredProducts;

    @Autowired
    ProductServiceImpl productServiceImpl;
    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    TransactionServiceImpl transactionServiceImpl;
    @Autowired
    private ClientRepository clientRepository;

    public StockViewController() {
        windowShowing = new WindowShowing();
    }

    @FXML
    private void switchToSupport(ActionEvent event) throws IOException {
        sceneSwitcher.setRootWithEvent(event, "/supportView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToRegistry(ActionEvent event) throws IOException {
        sceneSwitcher.setRootWithEvent(event, "/registryView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) throws IOException {
        sceneSwitcher.setRootWithEvent(event, "/configurationView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToWallet(ActionEvent event) throws IOException {
        sceneSwitcher.setRootWithEvent(event, "/walletView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    public void initialize() {
        products = FXCollections.observableArrayList();
        filteredProducts = new FilteredList<>(products, p -> true);
        tblStock.setItems(filteredProducts);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("quantityInStorage"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        buyColumn.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        currencyBuyColumn.setCellValueFactory(new PropertyValueFactory<>("currencyBuyName"));
        sellColumn.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        currencySellColumn.setCellValueFactory(new PropertyValueFactory<>("currencySellName"));
        storedInColumn.setCellValueFactory(new PropertyValueFactory<>("storedIn"));

        Label placeholder = new Label("No hay productos registrados");
        placeholder.setPadding(new Insets(20));
        tblStock.setPlaceholder(placeholder);

        tblStock.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (product == null || empty) {
                    setStyle("");
                } else if (product.getQuantityInStorage() < 4) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else if (product.getQuantityInStorage() < 10) {
                    setStyle("-fx-background-color: #ffaa00;");
                } else {
                    setStyle("");
                }
            }
        });

        txtFilterName.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return product.getProductName().toLowerCase().contains(newVal.toLowerCase());
            });
        });

        loadProductsAsync();
        txtClientName.setText(clientServiceImpl.getByIsClientActive(true).getClientName());
        addPane.setStyle("-fx-opacity: 0.6; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        sellPane.setStyle("-fx-opacity: 1; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        txtSellProductDatePicker.setValue(LocalDate.now());
    }

    @FXML
    public void selectProduct(MouseEvent event) {
        Product p = tblStock.getSelectionModel().getSelectedItem();
        if (p == null) return;
        txtAddProductName.setText(p.getProductName());
        txtAddProductQuantity.setText(String.valueOf(p.getQuantityInStorage()));
        txtAddProductBuyPrice.setText(String.valueOf(p.getBuyPrice()));
        txtAddProductSellPrice.setText(String.valueOf(p.getSellPrice()));
        txtAddProductCategory.setText(p.getCategoryName());
        txtAddProductStoredIn.setText(p.getStoredIn());
        selectCurrencyRadio(rmiBuyCUP, rmiBuyUSD, rmiBuyEUR, rmiBuyMLC, p.getCurrencyBuyName());
        selectCurrencyRadio(rmiSellCUP, rmiSellUSD, rmiSellEUR, rmiSellMLC, p.getCurrencySellName());
        txtSellProductSellCategory.setText(p.getCategoryName());
        txtSellProductSellName.setText(p.getProductName());
        txtSellProductSellPrice.setText(String.valueOf(p.getSellPrice()));
        txtSellProductSellStored.setText(p.getStoredIn());
        txtSellProductSellQuantity.setText("0");
        selectCurrencyRadio(radioSellCUP, radioSellUSD, radioSellEUR, radioSellMLC, p.getCurrencySellName());
    }

    private void selectCurrencyRadio(RadioMenuItem cup, RadioMenuItem usd, RadioMenuItem eur, RadioMenuItem mlc, String value) {
        cup.setSelected("CUP".equals(value));
        usd.setSelected("USD".equals(value));
        eur.setSelected("EUR".equals(value));
        mlc.setSelected("MLC".equals(value));
    }

    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        try {
            Product product = buildProductFromForm();
            productServiceImpl.validateProductInputs(product);
            boolean isNewProduct = !productServiceImpl.productExists(product.getProductName());
            Product updatedProduct = productServiceImpl.createOrUpdateProduct(product);

            Investment investment = new Investment();
            investment.setTransactionPrice(product.getBuyPrice().multiply(new BigDecimal(product.getQuantityInStorage())));
            investment.setTransactionStock(product.getQuantityInStorage());
            investment.setTransactionDate(txtSellProductDatePicker.getValue());
            investment.setCurrencyName(product.getCurrencyBuyName());
            investment.setClientId(clientServiceImpl.getByIsClientActive(true));
            investment.setProductName(product.getProductName());
            investment.setCategoryName(product.getCategoryName());
            investment.setTransactionType(isNewProduct ? RegistryType.ADD : RegistryType.UPDATE);
            investment.setTransactionStorage(product.getStoredIn());
            transactionServiceImpl.save(investment);

            if (isNewProduct) {
                products.add(updatedProduct);
            } else {
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getProductName().equals(updatedProduct.getProductName())) {
                        products.set(i, updatedProduct);
                        break;
                    }
                }
            }
            updateCounters();
            showAlert("Éxito", isNewProduct ? "Producto agregado correctamente." : "Producto actualizado correctamente.", Alert.AlertType.INFORMATION);
            cleanForm(null);
        } catch (Exception e) {
            logger.error("Error al procesar la acción: {}", e.getMessage(), e);
            showAlert("Error", "Ocurrió un error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Product buildProductFromForm() {
        String name = txtAddProductName.getText();
        Integer quantity = Integer.parseInt(txtAddProductQuantity.getText());
        BigDecimal buyPrice = new BigDecimal(txtAddProductBuyPrice.getText());
        BigDecimal sellPrice = new BigDecimal(txtAddProductSellPrice.getText());
        String category = txtAddProductCategory.getText();
        String buyCurrency = getSelectedBuyCurrency();
        String sellCurrency = getSelectedSellCurrency();
        String storedIn = txtAddProductStoredIn.getText();
        return new Product(name, category, quantity, buyPrice, buyCurrency, sellPrice, sellCurrency, storedIn, clientServiceImpl.getByIsClientActive(true));
    }

    @FXML
    public void removeProduct(ActionEvent actionEvent) {
        Product selectedProduct = tblStock.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Por favor, selecciona un producto de la tabla para eliminarlo.", Alert.AlertType.ERROR);
            return;
        }
        boolean confirmed = showConfirmationDialog(
                "Confirmación de eliminación",
                "¿Estás seguro de que deseas eliminar el producto?",
                "Producto: " + selectedProduct.getProductName() +
                        "\nCantidad: " + selectedProduct.getQuantityInStorage() +
                        "\nPrecio de compra: " + selectedProduct.getBuyPrice() +
                        "\nTipo de moneda para compra: " + selectedProduct.getCurrencyBuyName() +
                        "\nAlmacenado en: " + selectedProduct.getStoredIn()
        );
        if (confirmed) {
            try {
                Investment investment = new Investment();
                investment.setTransactionStorage(selectedProduct.getStoredIn());
                investment.setTransactionDate(LocalDate.now());
                investment.setTransactionStock(selectedProduct.getQuantityInStorage());
                investment.setTransactionPrice(selectedProduct.getBuyPrice());
                investment.setTransactionType(RegistryType.DELETE);
                investment.setCategoryName(selectedProduct.getCategoryName());
                investment.setClientId(clientServiceImpl.getByIsClientActive(true));
                investment.setProductName(selectedProduct.getProductName());
                investment.setCurrencyName(selectedProduct.getCurrencyBuyName());
                transactionServiceImpl.save(investment);

                productServiceImpl.deleteByProductName(selectedProduct.getProductName());
                products.remove(selectedProduct);
                updateCounters();
                showAlert("Éxito", "El producto ha sido eliminado correctamente.", Alert.AlertType.INFORMATION);
                cleanForm(null);
            } catch (Exception e) {
                logger.error("Error al eliminar el producto", e);
                showAlert("Error", "Ocurrió un error al intentar eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void sellProduct(ActionEvent actionEvent) {
        try {
            String productName = txtSellProductSellName.getText();
            Integer sellQuantity = Integer.parseInt(txtSellProductSellQuantity.getText());
            BigDecimal sellPrice = new BigDecimal(txtSellProductSellPrice.getText());
            String storedIn = txtSellProductSellStored.getText();
            LocalDate date = txtSellProductDatePicker.getValue();

            Product product = productServiceImpl.getByProductName(productName);
            if (product == null) throw new IllegalArgumentException("Producto no encontrado.");

            if (sellQuantity > product.getQuantityInStorage()) {
                throw new IllegalArgumentException("La cantidad a vender excede la cantidad almacenada.");
            } else if (sellQuantity < 1) {
                showAlert("Error", "No puede vender 0 unidades de un producto, o usar valores negativos.", Alert.AlertType.ERROR);
                return;
            }

            int newQuantity = product.getQuantityInStorage() - sellQuantity;
            product.setQuantityInStorage(newQuantity);
            productServiceImpl.createOrUpdateProduct(product);

            Investment investment = new Investment();
            investment.setTransactionPrice(sellPrice.multiply(new BigDecimal(sellQuantity)));
            investment.setTransactionStock(sellQuantity);
            investment.setTransactionDate(date);
            investment.setCurrencyName(product.getCurrencySellName());
            investment.setClientId(clientServiceImpl.getByIsClientActive(true));
            investment.setProductName(product.getProductName());
            investment.setTransactionStorage(product.getStoredIn());
            investment.setCategoryName(product.getCategoryName());
            investment.setTransactionType(RegistryType.SALE);
            transactionServiceImpl.save(investment);

            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getProductName().equals(product.getProductName())) {
                    products.set(i, product);
                    break;
                }
            }
            updateCounters();
            showAlert("Éxito", "La venta se ha registrado correctamente.", Alert.AlertType.INFORMATION);
            cleanForm(null);
        } catch (Exception e) {
            logger.error("Error al procesar la venta: {}", e.getMessage(), e);
            showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        txtAddProductName.clear();
        txtAddProductQuantity.clear();
        txtAddProductBuyPrice.clear();
        txtAddProductSellPrice.clear();
        txtAddProductCategory.clear();
        txtAddProductStoredIn.clear();
        rmiBuyCUP.setSelected(true);
        rmiSellCUP.setSelected(true);
        txtSellProductSellName.clear();
        txtSellProductSellStored.clear();
        txtSellProductSellQuantity.clear();
        txtSellProductSellPrice.clear();
        txtSellProductSellCategory.clear();
    }

    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String getSelectedBuyCurrency() {
        if (rmiBuyCUP.isSelected()) return "CUP";
        if (rmiBuyUSD.isSelected()) return "USD";
        if (rmiBuyEUR.isSelected()) return "EUR";
        if (rmiBuyMLC.isSelected()) return "MLC";
        throw new IllegalStateException("Debe seleccionar una moneda para la compra.");
    }

    private String getSelectedSellCurrency() {
        if (rmiSellCUP.isSelected()) return "CUP";
        if (rmiSellUSD.isSelected()) return "USD";
        if (rmiSellEUR.isSelected()) return "EUR";
        if (rmiSellMLC.isSelected()) return "MLC";
        throw new IllegalStateException("Debe seleccionar una moneda para la venta.");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadProductsAsync() {
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() {
                return productServiceImpl.getAllProductsByClient_IsClientActive(true);
            }
        };
        task.setOnSucceeded(event -> {
            List<Product> loadedProducts = task.getValue();
            products.clear();
            if (loadedProducts != null) {
                products.addAll(loadedProducts);
            }
            updateCounters();
        });
        new Thread(task).start();
    }

    private void updateCounters() {
        int warning = 0, alert = 0;
        for (Product product : products) {
            if (product.getQuantityInStorage() < 4) {
                alert++;
            } else if (product.getQuantityInStorage() < 10) {
                warning++;
            }
        }
        txtAlert.setText("Alertas: " + alert);
        txtWarning.setText("Advertencias: " + warning);
    }

    @FXML
    public void addPaneChanged(Event event) {
        addPane.setStyle("-fx-opacity: 0.6; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        sellPane.setStyle("-fx-opacity: 1; -fx-background-color: #6059ff; -fx-font-weight: 800;");
    }

    @FXML
    public void sellPaneChanged(Event event) {
        sellPane.setStyle("-fx-opacity: 0.6; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        addPane.setStyle("-fx-opacity: 1; -fx-background-color: #6059ff; -fx-font-weight: 800;");
    }


    @FXML
    public void addProductSellPrice(Event event) {
    }

    @FXML
    public void txtFilterNameChanged(Event event) {
    }


}
