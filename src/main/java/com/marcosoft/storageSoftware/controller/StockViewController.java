package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.Product;
import com.marcosoft.storageSoftware.model.Transaction;
import com.marcosoft.storageSoftware.model.TransactionType;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.service.impl.TransactionServiceImpl;
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
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Controller
public class StockViewController {

    private static final Logger logger = LoggerFactory.getLogger(StockViewController.class);

    //FXML objects initialization
    @FXML
    private TableView<Product> tblStock;
    @FXML
    private TableColumn nameColumn, amountColumn, currencySellColumn, currencyBuyColumn, storedInColumn, sellColumn, buyColumn,
            categoryColumn;
    @FXML
    private TextField txtAddProductCategory, txtAddProductName, txtAddProductStoredIn, txtAddProductQuantity,
            txtAddProductBuyPrice, txtAddProductSellPrice, txtSellProductSellCategory, txtFilterName,
            txtSellProductSellName, txtSellProductSellStored, txtSellProductSellQuantity, txtSellProductSellPrice;
    @FXML
    private DatePicker txtSellProductDatePicker;
    @FXML
    private RadioMenuItem rmiSellCUP, rmiSellMLC, rmiSellUSD, rmiSellEUR, rmiBuyMLC, rmiBuyCUP, rmiBuyEUR, rmiBuyUSD,
            radioSellUSD, radioSellEUR, radioSellCUP, radioSellMLC;
    @FXML
    private ToggleGroup sellCurrency, buyCurrency, sellCurrencyInSell;
    @FXML
    private Label txtSellDebugForm, txtAddDebugForm, txtClientName, txtWarning, txtAlert;
    @FXML
    private Tab sellPane, addPane;

    //The table products and WindowShowing instantiation
    private final WindowShowing windowShowing;
    TransactionType transactionType;
    private ObservableList<Product> products;

    //Things that are auto administrated by Spring framework
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
        sceneSwitcher.setRoot(event, "/supportView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToRegistry(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/registryView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/configurationView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToWallet(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/walletView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    public void initialize() {
        products = FXCollections.observableArrayList();
        tblStock.setItems(products);

        // Configuración de columnas
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("quantityInStorage"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        buyColumn.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        currencyBuyColumn.setCellValueFactory(new PropertyValueFactory<>("currencyBuyName"));
        sellColumn.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        currencySellColumn.setCellValueFactory(new PropertyValueFactory<>("currencySellName"));
        storedInColumn.setCellValueFactory(new PropertyValueFactory<>("storedIn"));

        // Placeholder para tabla vacía
        Label placeholder = new Label("No hay productos registrados");
        placeholder.setPadding(new Insets(20));
        tblStock.setPlaceholder(placeholder);

        // Configurar colores dinámicos para las filas
        tblStock.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (product == null || empty) {
                    setStyle(""); // Restart style for empty lines
                } else {
                    if (product.getQuantityInStorage() < 4) {
                        setStyle("-fx-background-color: #ffcccc;"); // Red for more than 4
                    } else if (product.getQuantityInStorage() < 10) {
                        setStyle("-fx-background-color: #ffaa00;"); // Orange for less than 10
                    } else {
                        setStyle(""); // Without style for more quantities than 10
                    }
                }
            }
        });

        // Cargar productos existentes y actualizar contadores
        loadProductsAsync();
        updateCounters();

        txtClientName.setText(clientServiceImpl.getByIsClientActive(true).getClientName());
        addPane.setStyle("-fx-opacity: 0.6; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        sellPane.setStyle("-fx-opacity: 1; -fx-background-color: #6059ff; -fx-font-weight: 800;");
        txtSellProductDatePicker.setValue(LocalDate.now());
    }

    @FXML
    public void selectProduct(MouseEvent event) {
        Product p = this.tblStock.getSelectionModel().getSelectedItem();

        if (p != null) {
            this.txtAddProductName.setText(p.getProductName());
            this.txtAddProductQuantity.setText(String.valueOf(p.getQuantityInStorage()));
            this.txtAddProductBuyPrice.setText(String.valueOf(p.getBuyPrice()));
            this.txtAddProductSellPrice.setText(String.valueOf(p.getSellPrice()));
            this.txtAddProductCategory.setText(p.getCategoryName());
            this.txtAddProductStoredIn.setText(p.getStoredIn());
            switch (p.getCurrencyBuyName()) {
                case "CUP" -> this.rmiBuyCUP.setSelected(true);
                case "USD" -> this.rmiBuyUSD.setSelected(true);
                case "EUR" -> this.rmiBuyEUR.setSelected(true);
                case "MLC" -> this.rmiBuyMLC.setSelected(true);
            }
            switch (p.getCurrencySellName()) {
                case "CUP" -> this.rmiSellCUP.setSelected(true);
                case "USD" -> this.rmiSellUSD.setSelected(true);
                case "EUR" -> this.rmiSellEUR.setSelected(true);
                case "MLC" -> this.rmiSellMLC.setSelected(true);
            }

            this.txtSellProductSellCategory.setText(p.getCategoryName());
            this.txtSellProductSellName.setText(p.getProductName());
            this.txtSellProductSellPrice.setText(String.valueOf(p.getSellPrice()));
            this.txtSellProductSellStored.setText(p.getStoredIn());
            this.txtSellProductSellQuantity.setText("0");
            switch (p.getCurrencySellName()) {
                case "CUP" -> this.radioSellCUP.setSelected(true);
                case "MLC" -> this.radioSellMLC.setSelected(true);
                case "EUR" -> this.radioSellEUR.setSelected(true);
                case "USD" -> this.radioSellUSD.setSelected(true);
            }
        }
    }

    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        try {
            String name = txtAddProductName.getText();
            Integer quantity = Integer.parseInt(txtAddProductQuantity.getText());
            BigDecimal buyPrice = new BigDecimal(txtAddProductBuyPrice.getText());
            BigDecimal sellPrice = new BigDecimal(txtAddProductSellPrice.getText());
            String category = txtAddProductCategory.getText();
            String buyCurrency = getSelectedBuyCurrency();
            String sellCurrency = getSelectedSellCurrency();
            String storedIn = txtAddProductStoredIn.getText();

            // Crear producto auxiliar
            Product product = new Product(name, category, quantity, buyPrice, buyCurrency, sellPrice, sellCurrency,
                    storedIn, clientServiceImpl.getByIsClientActive(true));

            // Validar el producto
            productServiceImpl.validateProductInputs(product);

            // Verificar si el producto ya existe
            boolean isNewProduct = !productServiceImpl.productExists(name);

            // Usar el servicio para crear o actualizar el producto
            Product updatedProduct = productServiceImpl.createOrUpdateProduct(product);

            // Registrar la transacción
            Transaction transaction = new Transaction();
            transaction.setTransactionPrice(buyPrice.multiply(new BigDecimal(quantity)));
            transaction.setTransactionStock(quantity);
            transaction.setTransactionDate(txtSellProductDatePicker.getValue());
            transaction.setCurrencyName(buyCurrency);
            transaction.setClientId(clientServiceImpl.getByIsClientActive(true));
            transaction.setProductName(name);
            transaction.setCategoryName(category);
            transaction.setTransactionType(isNewProduct ? TransactionType.ADD : TransactionType.UPDATE); // Enum para tipo de transacción
            transaction.setTransactionStorage(txtAddProductStoredIn.getText());
            transactionServiceImpl.save(transaction);

            // Actualizar la lista de productos en la tabla
            if (isNewProduct) {
                products.add(updatedProduct);
            } else {
                int index = products.indexOf(updatedProduct);
                products.set(index, updatedProduct);
            }

            // Refrescar la tabla
            tblStock.refresh();
            showAlert("Éxito", isNewProduct ? "Producto agregado correctamente." : "Producto actualizado correctamente.", Alert.AlertType.INFORMATION);

        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Error al procesar la acción: {}", e.getMessage(), e);
            showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void removeProduct(ActionEvent actionEvent) {
        Product selectedProduct = this.tblStock.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert("Error", "Por favor, selecciona un producto de la tabla para eliminarlo.", Alert.AlertType.ERROR);
            return;
        }

        // Mostrar confirmación
        boolean confirmed = showConfirmationDialog(
                "Confirmación de eliminación",
                "¿Estás seguro de que deseas eliminar el producto?",
                "Producto: " + selectedProduct.getProductName() +
                        "\nCategoría: " + selectedProduct.getCategoryName() +
                        "\nCantidad: " + selectedProduct.getQuantityInStorage() +
                        "\nPrecio de compra: " + selectedProduct.getBuyPrice() +
                        "\nTipo de moneda para compra: " + selectedProduct.getCurrencyBuyName() +
                        "\nPrecio de venta: " + selectedProduct.getSellPrice() +
                        "\nTipo de moneda para la venta: " + selectedProduct.getCurrencySellName() +
                        "\nAlmacenamiento: " + selectedProduct.getStoredIn()
        );

        if (confirmed) {
            try {
                // Usar el servicio para eliminar el producto
                Transaction transaction = new Transaction();
                transaction.setTransactionStorage(selectedProduct.getStoredIn());
                transaction.setTransactionDate(LocalDate.now());
                transaction.setTransactionStock(selectedProduct.getQuantityInStorage());
                transaction.setTransactionPrice(selectedProduct.getBuyPrice());
                transaction.setTransactionType(TransactionType.DELETE);
                transaction.setCategoryName(selectedProduct.getCategoryName());
                transaction.setClientId(clientServiceImpl.getByIsClientActive(true));
                transaction.setProductName(selectedProduct.getProductName());
                transaction.setCurrencyName(selectedProduct.getCurrencyBuyName());
                transactionServiceImpl.save(transaction);

                productServiceImpl.deleteByProductName(selectedProduct.getProductName());
                products.remove(selectedProduct);
                this.tblStock.refresh();
                updateCounters();
                showAlert("Éxito", "El producto ha sido eliminado correctamente.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                logger.error("Error al eliminar el producto", e);
                showAlert("Error", "Ocurrió un error al intentar eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void addProductQuantityChanged(Event event) {
        handleFieldChange(txtAddProductQuantity, "\\d*", "Solo se permiten números enteros en la cantidad.");
    }

    @FXML
    public void addProductBuyPrice(Event event) {
        handleFieldChange(txtAddProductBuyPrice, "\\d*(\\.\\d*)?",
                "Solo se permiten números decimales en el precio.");

    }

    @FXML
    public void addProductSellPrice() {
        handleFieldChange(txtAddProductSellPrice, "\\d*(\\.\\d*)?",
                "Solo se permiten números decimales en el precio.");
    }

    @FXML
    public void addProductNameChanged(Event event) {
        boolean isValid = !txtAddProductName.getText().isEmpty();
    }

    @FXML
    public void addProductCategoryChanged(Event event) {
        boolean isValid = !txtAddProductCategory.getText().isEmpty();
    }

    @FXML
    public void addProductStoredInChanged(Event event) {
        boolean isValid = !txtAddProductStoredIn.getText().isEmpty();
    }

    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        this.txtAddProductName.setText("");
        this.txtAddProductQuantity.setText("");
        this.txtAddProductBuyPrice.setText("");
        this.txtAddProductSellPrice.setText("");
        this.txtAddProductCategory.setText("");
        this.txtAddProductStoredIn.setText("");
        this.rmiBuyCUP.setSelected(true);
        this.rmiSellCUP.setSelected(true);

        this.txtSellProductSellName.setText("");
        this.txtSellProductSellStored.setText("");
        this.txtSellProductSellQuantity.setText("");
        this.txtSellProductSellPrice.setText("");
        this.txtSellProductSellCategory.setText("");
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

    private void handleFieldChange(TextField field, String regex, String errorMessage) {
        String text = field.getText();
        boolean isValid = text.matches(regex);

        if (!isValid) {
            showAlert("Error", errorMessage, Alert.AlertType.ERROR);
            field.setText(text.replaceAll("[^\\d.]", "")); // Ajustar según el regex
        }

        isValid = !field.getText().isEmpty();
    }

    private void loadProductsAsync() {
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() {
                // Obtener productos del servicio
                return productServiceImpl.getAllProductsByClient_IsClientActive(true);
            }
        };

        task.setOnSucceeded(event -> {
            List<Product> loadedProducts = task.getValue();
            if (loadedProducts == null || loadedProducts.isEmpty()) {
                logger.info("No se encontraron productos para el cliente activo.");
            } else {
                // Limpia la lista observable antes de agregar nuevos productos
                products.clear();
                products.addAll(loadedProducts);
                tblStock.refresh();
                updateCounters();
            }
        });

        new Thread(task).start();
    }

    private void updateCounters() {
        //Integer variables
        int warning = 0;
        int alert = 0;

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
    void txtFilterNameChanged(InputMethodEvent event) {
        String filterText = txtFilterName.getText();

        if (filterText == null || filterText.isEmpty()) {
            // Si el campo de texto está vacío, muestra todos los productos
            tblStock.setItems(products);
        } else {
            // Filtra los productos por el nombre
            ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
            for (Product product : products) {
                if (product.getProductName().toLowerCase().contains(filterText.toLowerCase())) {
                    filteredProducts.add(product);
                }
            }
            tblStock.setItems(filteredProducts);
        }
    }

    @FXML
    public void sellProduct(ActionEvent actionEvent) {
        try {

            // Obtener los valores ingresados por el usuario
            String productName = txtSellProductSellName.getText();
            Integer sellQuantity = Integer.parseInt(txtSellProductSellQuantity.getText());
            BigDecimal sellPrice = new BigDecimal(txtSellProductSellPrice.getText());
            String storedIn = txtSellProductSellStored.getText();
            LocalDate date = txtSellProductDatePicker.getValue();

            // Validar que los campos no estén vacíos
            if (productName.isEmpty() || storedIn.isEmpty()) {
                throw new IllegalArgumentException("Todos los campos son obligatorios.");
            }

            // Buscar el producto en la lista
            Product product = productServiceImpl.getByProductName(productName);
            if (product == null) {
                throw new IllegalArgumentException("El producto no existe.");
            }

            // Validar que la cantidad a vender no exceda la cantidad almacenada
            if (sellQuantity > product.getQuantityInStorage()) {
                throw new IllegalArgumentException("La cantidad a vender excede la cantidad almacenada.");
            }
            if (sellQuantity < 1) {
                // Actualizar la cantidad en almacenamiento
                int newQuantity = product.getQuantityInStorage() - sellQuantity;
                product.setQuantityInStorage(newQuantity);
                productServiceImpl.createOrUpdateProduct(product);
                tblStock.refresh();

                // Registrar la transacción
                Transaction transaction = new Transaction();
                transaction.setTransactionPrice(sellPrice.multiply(new BigDecimal(sellQuantity)));
                transaction.setTransactionStock(sellQuantity);
                transaction.setTransactionDate(date);
                transaction.setCurrencyName(product.getCurrencySellName());
                transaction.setClientId(clientServiceImpl.getByIsClientActive(true));
                transaction.setProductName(product.getProductName());
                transaction.setTransactionStorage(product.getStoredIn());
                transaction.setCategoryName(product.getCategoryName());
                transaction.setTransactionType(TransactionType.SALE); // Enum para tipo de transacción
                transactionServiceImpl.save(transaction);

                // Refresh the table and show succeed
                tblStock.refresh();
                showAlert("Éxito", "La venta se ha registrado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "No puede vender 0 unidades de un producto, o usar valores negativos" +
                        " por favor introduzca correctamente los datos", Alert.AlertType.ERROR);
            }

        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Error al procesar la venta: {}", e.getMessage(), e);
            showAlert("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
