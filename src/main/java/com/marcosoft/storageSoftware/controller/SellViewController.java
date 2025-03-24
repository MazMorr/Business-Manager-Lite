
package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.*;
import com.marcosoft.storageSoftware.service.impl.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static javafx.scene.paint.Color.*;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
public class SellViewController implements Initializable {

    @FXML
    Label txtDebugForm;
    @FXML
    ProgressIndicator progressSellProduct;
    @FXML
    TextField txtFieldName, txtFieldPrice, txtFieldAmount;
    @FXML
    DatePicker txtFieldDate;
    @FXML
    RadioMenuItem rmiCUP, rmiMLC, rmiEUR, rmiUSD;

    float txtDateProgress = 0, txtPriceProgress = 0, txtNameProgress = 0, txtAmountProgress = 0;
    boolean dateIsChanged = false, priceIsChanged = false, nameIsChanged = false, amountIsChanged = false;

    @Autowired
    ProductServiceImpl productServiceImpl;
    @Autowired
    TransactionServiceImpl transactionServiceImpl;
    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    TransactionTypeServiceImpl transactionTypeServiceImpl;
    @Autowired
    CurrencyServiceImpl currencyServiceImpl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtFieldDate.setValue(LocalDate.now());
        //Aqui falta el get Name Product del producto seleccionado
        txtDateProgress += 0.25F;
        txtNameProgress += 0.25F;
        nameIsChanged = true;
        dateIsChanged = true;
        progressSellProduct.setProgress(txtDateProgress + txtPriceProgress + txtNameProgress + txtAmountProgress);
    }

    @FXML
    public void sellProduct() {
        if (progressSellProduct.isIndeterminate()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error al intentar la operación");
            alert.setResizable(false);
            alert.setContentText("Debe rellenar todos los campos");
            alert.showAndWait();
        } else if (progressSellProduct.getProgress() == 1) {
            //eliminar producto de la existencia y poner la transaction de la venta
            BigDecimal price = BigDecimal.valueOf(Long.parseLong(txtFieldPrice.getText()));
            Integer amount = Integer.parseInt(txtFieldAmount.getText());
            String name = txtFieldName.getText();
            LocalDate date = txtFieldDate.getValue();
            Client client = clientServiceImpl.findByIsClientActive(true);
            Product product = productServiceImpl.findByProductName(name);
            TransactionType transactionType = transactionTypeServiceImpl.findByTransactionTypeName("venta");
            Currency currency = getSelectedCurrency();
            Transaction transaction = new Transaction(null, price, amount, date, currency, client, product, transactionType);
            //falta la lógica que trae el producto acá
            productServiceImpl.updateQuantityInStorageByProductName(amount, name);
            transactionServiceImpl.save(transaction);
        }
    }

    @FXML
    void changedTxtAmountProduct() {

    }

    @FXML
    public void setTextChangedAmount() {
        if (!txtFieldAmount.getText().matches("\\d*")) {
            txtDebugForm.setText("Solo se permiten números enteros");
            txtDebugForm.setTextFill(DARKORANGE);
            // Eliminar caracteres no numéricos
            txtFieldAmount.setText(txtFieldAmount.getText().replaceAll("[^\\d]", ""));
        } else if (txtAmountProgress == 0 && !amountIsChanged && txtFieldAmount.getLength() > 0) {
            amountIsChanged = true;
            txtAmountProgress += 0.25F;
            txtDebugForm.setText("Cantidad establecida");
            txtDebugForm.setTextFill(GREEN);
        } else if (txtAmountProgress == 0.25F && amountIsChanged && txtFieldAmount.getLength() < 1) {
            amountIsChanged = false;
            txtAmountProgress -= 0.25F;
            txtDebugForm.setText("Cantidad sin establecer");
            txtDebugForm.setTextFill(RED);
        }
        progressSellProduct.setProgress(txtDateProgress + txtPriceProgress + txtNameProgress + txtAmountProgress);
    }

    @FXML
    public void setTextChangedPrice() {
        if (!txtFieldPrice.getText().matches("\\d*(\\.\\d*)?")) {
            txtDebugForm.setText("Solo se permiten números decimales");
            // Eliminar caracteres no numéricos excepto el punto decimal
            txtFieldPrice.setText(txtFieldPrice.getText().replaceAll("[^\\d.]", ""));
        } else if (txtPriceProgress == 0 && !priceIsChanged && txtFieldPrice.getLength() > 0) {
            priceIsChanged = true;
            txtPriceProgress += 0.25F;
            txtDebugForm.setText("Precio establecida");
            txtDebugForm.setTextFill(GREEN);
        } else if (txtPriceProgress == 0.25F && priceIsChanged && txtFieldPrice.getLength() < 1) {
            priceIsChanged = false;
            txtPriceProgress -= 0.25F;
            txtDebugForm.setText("Precio sin establecer");
            txtDebugForm.setTextFill(RED);
        }
        progressSellProduct.setProgress(txtDateProgress + txtPriceProgress + txtNameProgress + txtAmountProgress);
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

}
