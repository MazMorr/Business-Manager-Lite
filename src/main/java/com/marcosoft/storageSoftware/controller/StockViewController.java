package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.Product;
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
import java.util.List;

@Controller
public class StockViewController {

    private static final Logger logger = LoggerFactory.getLogger(StockViewController.class);

    @FXML
    private TableView<Product> tblStock;
    private ObservableList<Product> products;
    @FXML
    private TableColumn nameTable, amountTable, productTypeTable, pricePerUnitTable, currencyTable, storedInTable;
    @FXML
    private TextField txtAddProductCategory, txtAddProductName, txtAddProductStoredIn, txtAddProductQuantity,
            txtAddProductPricePerUnit;
    @FXML
    private ProgressBar progressBar;
    private double progressName = 0, progressCategory = 0, progressPrice = 0, progressQuantity = 0, progressStoredIn = 0;
    private boolean nameIsSet = false, categoryIsSet = false, priceIsSet = false, quantityIsSet = false, storedInIsSet = false;
    @FXML
    private Label txtSellDebugForm, txtAddDebugForm, txtClientName;
    @FXML
    private RadioMenuItem rmiEUR, rmiMLC, rmiUSD, rmiCUP;
    @FXML
    private ToggleGroup currency;

    private final WindowShowing windowShowing;

    @Autowired
    ProductServiceImpl productServiceImpl;
    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    ClientServiceImpl clientServiceImpl;

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
    private void displayBuyView() throws IOException {
        String errorMessage = "Ya hay una ventana de Compras abierta";
        String fxmlPath = "/buyView.fxml";
        int aux = 1;
        windowShowing.displayAssistance(windowShowing.isBuyViewShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void displayConfigurationView(ActionEvent event) throws IOException {
        String errorMessage = "Ya hay una ventana de Configuración abierta";
        String fxmlPath = "/configurationView.fxml";
        int aux = 3;
        windowShowing.displayAssistance(windowShowing.isConfigurationShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void switchToWallet() {
        // Implementación pendiente
    }

    @FXML
    public void initialize() {
        products = FXCollections.observableArrayList();
        tblStock.setItems(products);

        // Configurar columnas
        nameTable.setCellValueFactory(new PropertyValueFactory<>("productName"));
        amountTable.setCellValueFactory(new PropertyValueFactory<>("quantityInStorage"));
        productTypeTable.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        pricePerUnitTable.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        currencyTable.setCellValueFactory(new PropertyValueFactory<>("currencyName"));
        storedInTable.setCellValueFactory(new PropertyValueFactory<>("storedIn"));

        // Placeholder para tabla vacía
        Label placeholder = new Label("No hay productos registrados");
        placeholder.setPadding(new Insets(10));
        tblStock.setPlaceholder(placeholder);

        // Cargar datos de forma diferida
        loadProductsAsync();

        // Personalizar filas de la tabla
        customizeTableRows();
        txtClientName.setText(clientServiceImpl.findByIsClientActive(true).getClientName());
    }

    @FXML
    public void selectProduct(MouseEvent event) {
        Product p = this.tblStock.getSelectionModel().getSelectedItem();

        if (p != null) {
            this.txtAddProductName.setText(p.getProductName());
            this.txtAddProductQuantity.setText(String.valueOf(p.getQuantityInStorage()));
            this.txtAddProductPricePerUnit.setText(String.valueOf(p.getPricePerUnit()));
            this.txtAddProductCategory.setText(p.getCategoryName());
            this.txtAddProductStoredIn.setText(p.getStoredIn());
            switch (p.getCurrencyName()) {
                case "CUP" -> this.rmiCUP.setSelected(true);
                case "USD" -> this.rmiUSD.setSelected(true);
                case "EUR" -> this.rmiEUR.setSelected(true);
                case "MLC" -> this.rmiMLC.setSelected(true);
            }
        }
    }

    @FXML
    public void addOrUpgradeProduct(ActionEvent actionEvent) {
        try {
        /*  if (progressBar.getProgress() != 1.0) {
                showAlert("Error", "Por favor, complete todos los campos antes de guardar el producto.", Alert.AlertType.ERROR);
                return;
            }*/

            // Validar entradas
            validateProductInputs();
            String name = txtAddProductName.getText();
            Integer quantity = Integer.parseInt(txtAddProductQuantity.getText());
            BigDecimal pricePerUnit = new BigDecimal(txtAddProductPricePerUnit.getText());
            String category = txtAddProductCategory.getText();
            String currency = getSelectedCurrency();
            String storedIn = txtAddProductStoredIn.getText();

            // Crear producto auxiliar
            Product product = new Product(name, category, quantity, pricePerUnit, currency, storedIn);

            // Usar el servicio para crear o actualizar el producto
            Product updatedProduct = productServiceImpl.createOrUpdateProduct(product);

            // Actualizar la lista de productos en la tabla
            int index = products.indexOf(updatedProduct);
            if (index >= 0) {
                // Reemplazar el producto existente
                products.set(index, updatedProduct);
            } else {
                // Agregar el nuevo producto si no existe
                products.add(updatedProduct);
            }

            // Refrescar la tabla
            tblStock.refresh();

            resetProgress();
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
                        "\nPrecio por unidad: " + selectedProduct.getPricePerUnit() +
                        "\nTipo de moneda: " + selectedProduct.getCurrencyName() +
                        "\nAlmacenamiento: " + selectedProduct.getStoredIn()
        );

        if (confirmed) {
            try {
                // Usar el servicio para eliminar el producto
                productServiceImpl.deleteByProductName(selectedProduct.getProductName());
                products.remove(selectedProduct);
                this.tblStock.refresh();
                showAlert("Éxito", "El producto ha sido eliminado correctamente.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                logger.error("Error al eliminar el producto", e);
                showAlert("Error", "Ocurrió un error al intentar eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void addProductQuantityChanged(Event event) {
        handleFieldChange(txtAddProductQuantity, "\\d*", "Solo se permiten números enteros en la cantidad.", progressQuantity, quantityIsSet);
        quantityIsSet = !txtAddProductQuantity.getText().isEmpty();
    }

    @FXML
    public void addProductPricePerUnitChanged(Event event) {
        handleFieldChange(txtAddProductPricePerUnit, "\\d*(\\.\\d*)?", "Solo se permiten números decimales en el precio.", progressPrice, priceIsSet);
        priceIsSet = !txtAddProductPricePerUnit.getText().isEmpty();
    }

    @FXML
    public void addProductNameChanged(Event event) {
        boolean isValid = !txtAddProductName.getText().isEmpty();
        updateProgress(isValid, progressName, nameIsSet);
        nameIsSet = isValid;
    }

    @FXML
    public void addProductCategoryChanged(Event event) {
        boolean isValid = !txtAddProductCategory.getText().isEmpty();
        updateProgress(isValid, progressCategory, categoryIsSet);
        categoryIsSet = isValid;
    }

    @FXML
    public void addProductStoredInChanged(Event event) {
        boolean isValid = !txtAddProductStoredIn.getText().isEmpty();
        updateProgress(isValid, progressStoredIn, storedInIsSet);
        storedInIsSet = isValid;
    }

    @FXML
    public void cleanForm(ActionEvent actionEvent) {
        this.txtAddProductName.setText("");
        this.txtAddProductQuantity.setText("");
        this.txtAddProductPricePerUnit.setText("");
        this.txtAddProductCategory.setText("");
        this.txtAddProductStoredIn.setText("");
        this.rmiCUP.setSelected(true);
    }

    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String getSelectedCurrency() {
        if (rmiCUP.isSelected()) return "CUP";
        if (rmiUSD.isSelected()) return "USD";
        if (rmiEUR.isSelected()) return "EUR";
        if (rmiMLC.isSelected()) return "MLC";
        throw new IllegalStateException("No se seleccionó ninguna moneda");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String validateTextField(TextField field, String errorMessage) {
        String text = field.getText();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return text;
    }

    private Integer validateIntegerField(TextField field, String errorMessage) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private BigDecimal validateBigDecimalField(TextField field, String errorMessage) {
        try {
            return new BigDecimal(field.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateProductInputs() {
        validateTextField(txtAddProductName, "El nombre del producto no puede estar vacío");
        validateIntegerField(txtAddProductQuantity, "La cantidad debe ser un número entero válido");
        validateBigDecimalField(txtAddProductPricePerUnit, "El precio debe ser un número válido");
        validateTextField(txtAddProductCategory, "La categoría no puede estar vacía");
        validateTextField(txtAddProductStoredIn, "El lugar de almacenamiento no puede estar vacío");
    }

    private void updateProgress(boolean condition, double progress, boolean isSet) {
        progress += condition && !isSet ? 0.2 : (!condition && isSet ? -0.2 : 0);
        progressBar.setProgress(progressName + progressCategory + progressPrice + progressQuantity + progressStoredIn);
    }

    private void resetProgress() {
        progressName = 0;
        progressCategory = 0;
        progressPrice = 0;
        progressQuantity = 0;
        progressStoredIn = 0;

        nameIsSet = false;
        categoryIsSet = false;
        priceIsSet = false;
        quantityIsSet = false;
        storedInIsSet = false;

        progressBar.setProgress(0);
    }

    private void handleFieldChange(TextField field, String regex, String errorMessage, double progress, boolean isSetFlag) {
        String text = field.getText();
        boolean isValid = text.matches(regex);

        if (!isValid) {
            showAlert("Error", errorMessage, Alert.AlertType.ERROR);
            field.setText(text.replaceAll("[^\\d.]", "")); // Ajustar según el regex
        }

        isValid = !field.getText().isEmpty();
        updateProgress(isValid, progress, isSetFlag);
    }

    private void customizeTableRows() {
        tblStock.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    if (newItem.getQuantityInStorage() < 3) {
                        // Menos de 3 unidades: rojo
                        row.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;"); // Rojo claro
                    } else if (newItem.getQuantityInStorage() < 10) {
                        // Menos de 10 unidades: amarillo
                        row.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;"); // Amarillo claro
                    } else {
                        // Restablecer estilo si no cumple ninguna condición
                        row.setStyle("");
                    }
                }
            });
            return row;
        });
    }

    private void loadProductsAsync() {
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() {
                return productServiceImpl.getAllProducts(); // Carga los productos desde la base de datos
            }
        };

        task.setOnSucceeded(event -> {
            products.addAll(task.getValue());
            tblStock.refresh();
        });

        new Thread(task).start();
    }
}
