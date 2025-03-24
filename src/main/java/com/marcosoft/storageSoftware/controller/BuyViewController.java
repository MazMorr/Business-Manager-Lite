package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.*;
import com.marcosoft.storageSoftware.repository.*;
import com.marcosoft.storageSoftware.service.impl.*;
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

    @FXML
    private Label txtDebugForm;
    @FXML
    private MenuItem miPersonal, miClean, miMilk, miCereal, miMeat, miDrink;
    @FXML
    private TextField txtFieldSubCategory, txtFieldPrice, txtFieldAmount, txtFieldName;
    @FXML
    private DatePicker txtFieldDate;
    @FXML
    private RadioMenuItem rmiCUP, rmiUSD, rmiEUR, rmiMLC;
    @FXML
    private ProgressIndicator percentageBar;

    private double percentageDate = 0, percentageName = 0, percentageSubCategory = 0,
            percentagePrize = 0, percentageAmount = 0;
    private boolean dateIsSet = false, nameIsSet = false, subCategoryIsSet = false,
            priceIsSet = false, amountIsSet = false;

    @Autowired
    ProductServiceImpl productServiceImpl;
    @Autowired
    CategoryServiceImpl categoryServiceImpl;
    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    TransactionTypeServiceImpl transactionTypeServiceImpl;
    @Autowired
    TransactionServiceImpl transactionServiceImpl;

    @FXML
    private void setTextChangedName() {
        updateProgress(txtFieldName.getLength() != 0, "Nombre de su producto.",
                percentageName, nameIsSet);
        nameIsSet = txtFieldName.getLength() != 0;
    }

    @FXML
    private void setTextChangedSubCategory() {
        updateProgress(txtFieldSubCategory.getLength() != 0, "Categoría del producto.",
                percentageSubCategory, subCategoryIsSet);
        subCategoryIsSet = txtFieldSubCategory.getLength() != 0;
    }

    @FXML
    private void setTextChangedPrice() {
        if (!txtFieldPrice.getText().matches("\\d*(\\.\\d*)?")) {
            txtDebugForm.setText("Solo se permiten números decimales.");
            txtFieldPrice.setText(txtFieldPrice.getText().replaceAll("[^\\d.]", ""));
        }
        updateProgress(txtFieldPrice.getLength() != 0,
                "Recuerde seleccionar el tipo de moneda en el botón: Moneda.", percentagePrize, priceIsSet);
        priceIsSet = txtFieldPrice.getLength() != 0;
    }

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

    @FXML
    private void setTextClickedDate() {
        updateProgress(txtFieldDate.getValue() != null, "Seleccione la fecha en el botón de la derecha.", percentageDate, dateIsSet);
        dateIsSet = txtFieldDate.getValue() != null;
    }

    private void updateProgress(boolean condition, String debugText, double percentage, boolean isSetted) {
        txtDebugForm.setText(debugText);
        if (condition && !isSetted) {
            percentage += 0.2;
        } else if (!condition && percentage == 0.2) {
            percentage -= 0.2;
        }
        percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
    }

    @FXML
    private void clean() {
        txtDebugForm.setText("");
        txtFieldSubCategory.setText("");
        txtFieldPrice.setText("");
        txtFieldAmount.setText("");
        txtFieldName.setText("");
        txtFieldDate.setValue(LocalDate.now());
        resetProgress();
    }

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

    @FXML
    private void addProduct() {
        try {
            if (areFieldsEmpty(txtFieldSubCategory.getText(), txtFieldPrice.getText(), txtFieldAmount.getText(), txtFieldName.getText()) || percentageBar.getProgress() != 1.0) {
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

            //Assignations of attributes to the registry
            String productName = txtFieldName.getText();
            String categoryName = txtFieldSubCategory.getText();
            BigDecimal price = new BigDecimal(txtFieldPrice.getText());
            Integer stock = Integer.parseInt(txtFieldAmount.getText());
            LocalDate date = txtFieldDate.getValue();
            Product product = productServiceImpl.findByProductName(productName);
            TransactionType transactionType = transactionTypeServiceImpl.findByTransactionTypeName("compra");
            Client client = clientServiceImpl.findByIsClientActive(true);

            if (product != null) {
                stock += product.getQuantityInStorage();
                productServiceImpl.updateQuantityInStorageByProductName(stock, productName);
                Transaction transaction = new Transaction(null, price, stock, date, getSelectedCurrency(),
                        client, product, transactionType);
                transactionServiceImpl.save(transaction);
            } else {
                Product newProduct = new Product(null, productName,
                        categoryServiceImpl.findByCategoryName(categoryName), stock);
                Transaction transaction = new Transaction(null, price, stock, date, getSelectedCurrency(),
                        client, newProduct, transactionType);
                productServiceImpl.save(newProduct);
                transactionServiceImpl.save(transaction);
            }

            clean();
            txtDebugForm.setText("Producto añadido exitosamente");

        } catch (NumberFormatException e) {
            txtDebugForm.setText("Error en el formato de números");
        } catch (Exception e) {
            e.printStackTrace();
            txtDebugForm.setText("Error: " + e.getMessage());
        }
    }

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

    private boolean areFieldsEmpty(String... fields) {
        for (String field : fields) {
            if (field.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrencySelected() {
        return rmiCUP.isSelected() || rmiUSD.isSelected() || rmiEUR.isSelected() || rmiMLC.isSelected();
    }

    @FXML
    private void hideProvisions() {
        toggleVisibility(false, miMilk, miCereal, miMeat, miDrink);
        toggleVisibility(true, miPersonal, miClean);
    }

    @FXML
    private void hideCleanStuff() {
        toggleVisibility(false, miPersonal, miClean);
        toggleVisibility(true, miMilk, miCereal, miMeat, miDrink);
    }

    private void toggleVisibility(boolean visible, MenuItem... items) {
        for (MenuItem item : items) {
            item.setVisible(visible);
        }
    }

    public void setSubcategory(String subcategory) {
        txtFieldSubCategory.setText(subcategory);
        if (!subCategoryIsSet) {
            subCategoryIsSet = true;
            percentageSubCategory += 0.2;
            percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
        }
    }

    @FXML
    private void setDrinkText() {
        setSubcategory("Bebida");
    }

    @FXML
    private void setMilkText() {
        setSubcategory("Lácteo");
    }

    @FXML
    private void setPersonalText() {
        setSubcategory("Aseo Personal");
    }

    @FXML
    private void setCleanText() {
        setSubcategory("Limpieza");
    }

    @FXML
    private void setCerealText() {
        setSubcategory("Cereal");
    }

    @FXML
    private void setMeatText() {
        setSubcategory("Cárnico");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtFieldDate.setValue(LocalDate.now());
        percentageDate += 0.2;
        percentageBar.setProgress(percentageName + percentageDate + percentageSubCategory + percentageAmount + percentagePrize);
        dateIsSet = true;
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
