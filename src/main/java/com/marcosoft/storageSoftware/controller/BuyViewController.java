package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.*;
import com.marcosoft.storageSoftware.service.CategoryService;
import com.marcosoft.storageSoftware.service.ProductService;
import com.marcosoft.storageSoftware.service.TransactionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller class for handling the Buy View.
 */
@Controller
public class BuyViewController implements Initializable {

    //ID from the fxml objects
    @FXML
    private Label txtDebugForm;
    @FXML
    private MenuItem miPersonal, miClean, miMilk, miCereal, miMeat, miDrink;
    @FXML
    private TextField txtFieldSubCategory, txtFieldPrize, txtFieldAmount, txtFieldName;
    @FXML
    private DatePicker txtFieldDate;
    @FXML
    private RadioMenuItem rmiCUP, rmiUSD, rmiEUR, rmiMLC;
    @FXML
    private ProgressIndicator percentageBar;

    //Services
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TransactionService transactionService;

    //Variables for the logic
    private double percentageDate = 0, percentageName = 0, percentageSubCategory = 0,
            percentagePrize = 0, percentageAmount = 0;
    private boolean dateIsSet = false, nameIsSet = false, subCategoryIsSet = false,
            priceIsSet = false, amountIsSet = false;

    /**
     * Handles changes in the product name text field.
     */
    @FXML
    private void setTextChangedName() {
        updateProgress(txtFieldName.getLength() != 0, "Nombre de su producto.",
                percentageName, nameIsSet);
        nameIsSet = txtFieldName.getLength() != 0;
    }

    /**
     * Handles changes in the subcategory text field.
     */
    @FXML
    private void setTextChangedSubCategory() {
        updateProgress(txtFieldSubCategory.getLength() != 0, "Categoría del producto.",
                percentageSubCategory, subCategoryIsSet);
        subCategoryIsSet = txtFieldSubCategory.getLength() != 0;
    }

    /**
     * Handles changes in the price text field.
     */
    @FXML
    private void setTextChangedPrice() {
        if (!txtFieldPrize.getText().matches("\\d*(\\.\\d*)?")) {
            txtDebugForm.setText("Solo se permiten números decimales.");
            txtFieldPrize.setText(txtFieldPrize.getText().replaceAll("[^\\d.]", ""));
        }
        updateProgress(txtFieldPrize.getLength() != 0,
                "Recuerde seleccionar el tipo de moneda en el botón: Moneda.", percentagePrize, priceIsSet);
        priceIsSet = txtFieldPrize.getLength() != 0;
    }

    /**
     * Handles changes in the amount text field.
     */
    @FXML
    private void setTextChangedAmount() {
        if (!txtFieldAmount.getText().matches("\\d*")) {
            txtDebugForm.setText("Solo se permiten números enteros.");
            txtFieldAmount.setText(txtFieldAmount.getText().replaceAll("[^\\d]", ""));
        }
        updateProgress(txtFieldAmount.getLength() != 0, "Cantidad de ese mismo producto.",
                percentageAmount, amountIsSet);
        amountIsSet = txtFieldAmount.getLength() != 0;
    }

    /**
     * Handles changes in the date picker.
     */
    @FXML
    private void setTextClickedDate() {
        updateProgress(txtFieldDate.getValue() != null, "Seleccione la fecha en el botón de la derecha.", percentageDate, dateIsSet);
        dateIsSet = txtFieldDate.getValue() != null;
    }

    /**
     * Updates the progress indicator based on the provided conditions.
     *
     * @param condition  the condition to check
     * @param debugText  the debug text to display
     * @param percentage the percentage to update
     * @param isSetted   the flag indicating if the condition is set
     */
    private void updateProgress(boolean condition, String debugText, double percentage, boolean isSetted) {
        txtDebugForm.setText(debugText);
        if (condition && !isSetted) {
            percentage += 0.2;
        } else if (!condition && percentage == 0.2) {
            percentage -= 0.2;
        }
        percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
    }

    /**
     * Clears all input fields and resets the progress indicator.
     */
    @FXML
    private void clean() {
        txtDebugForm.setText("");
        txtFieldSubCategory.setText("");
        txtFieldPrize.setText("");
        txtFieldAmount.setText("");
        txtFieldName.setText("");
        txtFieldDate.setValue(LocalDate.now());
        resetProgress();
    }

    /**
     * Resets the progress indicator and related flags.
     */
    private void resetProgress() {
        percentageName = 0;
        percentageSubCategory = 0;
        percentagePrize = 0;
        percentageAmount = 0;
        nameIsSet = false;
        subCategoryIsSet = false;
        priceIsSet = false;
        amountIsSet = false;
        percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
    }

    /**
     * Adds a new product based on the input fields.
     */
    @FXML
    private void addProduct() {
        try {
            if (areFieldsEmpty(txtFieldSubCategory.getText(), txtFieldPrize.getText(), txtFieldAmount.getText(), txtFieldName.getText()) || percentageBar.getProgress() != 1.0) {
                txtDebugForm.setText("Por favor rellene todos los campos");
                return;
            }

            if (!isCurrencySelected()) {
                txtDebugForm.setText("Por favor seleccione el tipo de Moneda");
                return;
            }

            if (txtFieldDate.getValue() == null) {
                txtDebugForm.setText("Por favor seleccione la fecha.");
                return;
            }

            String productName = txtFieldName.getText();
            String categoryName = txtFieldSubCategory.getText();
            BigDecimal price = new BigDecimal(txtFieldPrize.getText());
            int quantity = Integer.parseInt(txtFieldAmount.getText());
            LocalDate date = txtFieldDate.getValue();

            Category category = findOrCreateCategory(categoryName);
            Product product = new Product(null, productName, category);
            Transaction transaction = createTransaction(product, price, quantity, date);

            categoryService.saveCategory(category);
            productService.saveProduct(product);
            transactionService.saveTransaction(transaction);

            clean();
            txtDebugForm.setText("Producto añadido exitosamente");

        } catch (NumberFormatException e) {
            txtDebugForm.setText("Error en el formato de números");
        } catch (Exception e) {
            e.printStackTrace();
            txtDebugForm.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Finds or creates a new category based on the provided category name.
     *
     * @param categoryName the category name
     * @return the found or created category
     */
    private Category findOrCreateCategory(String categoryName) {
        Category existingCategory = categoryService.getCategoryByName(categoryName);
        if (existingCategory != null) {
            return existingCategory;
        }
        Category newCategory = new Category(null, categoryName);
        categoryService.saveCategory(newCategory);
        return newCategory;
    }

    /**
     * Creates a new transaction based on the provided product, price, quantity, and date.
     *
     * @param product  the product
     * @param price    the price
     * @param quantity the quantity
     * @param date     the date
     * @return the created transaction
     */
    private Transaction createTransaction(Product product, BigDecimal price, int quantity, LocalDate date) {
        Currency currency = getSelectedCurrency();
        Transaction transaction = new Transaction();
        transaction.setIdProduct(product);
        transaction.setPrice(price);
        transaction.setStock(quantity);
        transaction.setDate(date);
        transaction.setCurrency(currency);

        TransactionType transactionType = new TransactionType();
        transactionType.setTransactionName("Compra");
        transaction.setTransactionType(transactionType);

        Client client = new Client();
        client.setClientName("Cuenta Predeterminada");
        client.setClientPassword("password");
        transaction.setIdClient(client);

        return transaction;
    }

    /**
     * Gets the selected currency based on the selected radio menu item.
     *
     * @return the selected currency
     * @throws IllegalStateException if no currency is selected
     */
    private Currency getSelectedCurrency() {
        if (rmiCUP.isSelected()) {
            return new Currency("CUP", "Peso Cubano");
        } else if (rmiUSD.isSelected()) {
            return new Currency("USD", "Dólar Estadounidense");
        } else if (rmiEUR.isSelected()) {
            return new Currency("EUR", "Euro");
        } else if (rmiMLC.isSelected()) {
            return new Currency("MLC", "Moneda Libremente Convertible");
        }
        throw new IllegalStateException("No se seleccionó ninguna moneda");
    }

    /**
     * Checks if any of the provided fields are empty.
     *
     * @param fields the fields to check
     * @return true if any field is empty, false otherwise
     */
    private boolean areFieldsEmpty(String... fields) {
        for (String field : fields) {
            if (field.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any currency is selected.
     *
     * @return true if any currency is selected, false otherwise
     */
    private boolean isCurrencySelected() {
        return rmiCUP.isSelected() || rmiUSD.isSelected() || rmiEUR.isSelected() || rmiMLC.isSelected();
    }

    /**
     * Hides the provisions menu items and shows the personal and clean menu items.
     */
    @FXML
    private void hideProvisions() {
        toggleVisibility(false, miMilk, miCereal, miMeat, miDrink);
        toggleVisibility(true, miPersonal, miClean);
    }

    /**
     * Hides the personal and clean menu items and shows the provisions menu items.
     */
    @FXML
    private void hideCleanStuff() {
        toggleVisibility(false, miPersonal, miClean);
        toggleVisibility(true, miMilk, miCereal, miMeat, miDrink);
    }

    /**
     * Toggles the visibility of the provided menu items.
     *
     * @param visible the visibility state to set
     * @param items   the menu items to toggle
     */
    private void toggleVisibility(boolean visible, MenuItem... items) {
        for (MenuItem item : items) {
            item.setVisible(visible);
        }
    }

    /**
     * Sets the subcategory text field and updates the progress indicator.
     *
     * @param subcategory the subcategory to set
     */
    public void setSubcategory(String subcategory) {
        txtFieldSubCategory.setText(subcategory);
        if (!subCategoryIsSet) {
            subCategoryIsSet = true;
            percentageSubCategory += 0.2;
            percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
        }
    }

    /**
     * Sets the subcategory to "Drink".
     */
    @FXML
    private void setDrinkText() {
        setSubcategory("Bebida");
    }

    /**
     * Sets the subcategory to "Milk".
     */
    @FXML
    private void setMilkText() {
        setSubcategory("Lácteo");
    }

    /**
     * Sets the subcategory to "Personal Hygiene".
     */
    @FXML
    private void setPersonalText() {
        setSubcategory("Aseo Personal");
    }

    /**
     * Sets the subcategory to "Cleaning".
     */
    @FXML
    private void setCleanText() {
        setSubcategory("Limpieza");
    }

    /**
     * Sets the subcategory to "Cereal".
     */
    @FXML
    private void setCerealText() {
        setSubcategory("Cereal");
    }

    /**
     * Sets the subcategory to "Meat".
     */
    @FXML
    private void setMeatText() {
        setSubcategory("Cárnico");
    }

    /**
     * Initializes the controller class.
     *
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known
     * @param rb  the resources used to localize the root object, or null if the root object was not localized
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtFieldDate.setValue(LocalDate.now());
        percentageDate += 0.2;
        percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
        dateIsSet = true;
    }

    /**
     * Closes the current window.
     *
     * @param event the action event
     */
    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
