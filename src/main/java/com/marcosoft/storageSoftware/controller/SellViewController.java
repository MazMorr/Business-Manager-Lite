
package com.marcosoft.storageSoftware.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

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
    
    @FXML Label txtDebugForm;
    @FXML ProgressIndicator progressSellProduct;
    @FXML TextField txtFieldName, txtFieldPrice, txtFieldAmount;
    @FXML DatePicker txtFieldDate;

    float txtDateProgress=0, txtPriceProgress=0, txtNameProgress=0, txtAmountProgress=0;
    boolean dateIsChanged=false, priceIsChanged=false, nameIsChanged=false,amountIsChanged=false;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtFieldDate.setValue(LocalDate.now());
        //Aqui falta el get Name Product del producto seleccionado
        txtDateProgress+= 0.25F;
        txtNameProgress+= 0.25F;
        nameIsChanged=true;
        dateIsChanged=true;
        progressSellProduct.setProgress(txtDateProgress+txtPriceProgress+txtNameProgress+txtAmountProgress);
    }  
    @FXML
    public void sellProduct(){
        if(progressSellProduct.isIndeterminate()){
            Alert alert= new Alert(AlertType.ERROR);
            alert.setTitle("Error al intentar la operación");
            alert.setResizable(false);
            alert.setContentText("Debe rellenar todos los campos");
            alert.showAndWait();
        } else if(progressSellProduct.getProgress()==1) {
            //eliminar producto de la existencia y poner el registro de la venta
        }
    }
    @FXML void changedTxtAmountProduct(){

    }
    
    @FXML public void setTextChangedAmount(){
        if(!txtFieldAmount.getText().matches("\\d*")) {
            txtDebugForm.setText("Solo se permiten números enteros");
            txtDebugForm.setTextFill(DARKORANGE);
            // Eliminar caracteres no numéricos
            txtFieldAmount.setText(txtFieldAmount.getText().replaceAll("[^\\d]", ""));
        } else if(txtAmountProgress==0 && !amountIsChanged && txtFieldAmount.getLength()>0){
            amountIsChanged=true;
            txtAmountProgress+= 0.25F;
            txtDebugForm.setText("Cantidad establecida");
            txtDebugForm.setTextFill(GREEN);
        }else if(txtAmountProgress==0.25F && amountIsChanged && txtFieldAmount.getLength()<1){
            amountIsChanged=false;
            txtAmountProgress-= 0.25F;
            txtDebugForm.setText("Cantidad sin establecer");
            txtDebugForm.setTextFill(RED);
        }
        progressSellProduct.setProgress(txtDateProgress+txtPriceProgress+txtNameProgress+txtAmountProgress);
    }
    
    @FXML public void setTextChangedPrice(){
        if (!txtFieldPrice.getText().matches("\\d*(\\.\\d*)?")) {
            txtDebugForm.setText("Solo se permiten números decimales");
            // Eliminar caracteres no numéricos excepto el punto decimal
            txtFieldPrice.setText(txtFieldPrice.getText().replaceAll("[^\\d.]", ""));
        } else if(txtPriceProgress==0 && !priceIsChanged && txtFieldPrice.getLength()>0){
            priceIsChanged=true;
            txtPriceProgress+= 0.25F;
            txtDebugForm.setText("Precio establecida");
            txtDebugForm.setTextFill(GREEN);
        }else if(txtPriceProgress==0.25F && priceIsChanged && txtFieldPrice.getLength()<1){
            priceIsChanged=false;
            txtPriceProgress-= 0.25F;
            txtDebugForm.setText("Precio sin establecer");
            txtDebugForm.setTextFill(RED);
        }
        progressSellProduct.setProgress(txtDateProgress+txtPriceProgress+txtNameProgress+txtAmountProgress);
    }

    //Don't delete this function, its helpful when you need to close a window

    /*@FXML
    private void closeWindow(ActionEvent event){
        // Get the source of the event (the button that was clicked)
        Node source = (Node) event.getSource();

        // Get the stage from the source
        Stage stage = (Stage) source.getScene().getWindow();

        // Close the stage
        stage.close();
    }*/
    
}
