package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.logic.WindowShowing;
import com.marcosoft.storageSoftware.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ClientController {

    private final WindowShowing windowShowing;
    @FXML private TextField txtFieldPassword, txtFieldName;

    public ClientController(){
        windowShowing= new WindowShowing();
    }

    @FXML
    private void enterApplication(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/supportView.fxml"));
            Parent root = loader.load();
            SupportViewController primaryController = loader.getController(); // Declare and initialize correctly
            primaryController.setAccountController(this); // Pass the reference to the primary controller
            
            //Obetener la referencia de la ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            //Crear la nueva ventana
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
            stage.setTitle("Almacen");
            stage.centerOnScreen();
            stage.setResizable(false);
            
            //Handle the close request
            stage.setOnCloseRequest(e -> {
                windowShowing.closeAllWindows();
                // Show the alert and check the response
                if(showAlert()){
                    stage.close(); // Close the window if the user confirms
                } else{
                    e.consume(); // Consume the event to prevent closing
                }
                
            });
            
            //Show the new window
            stage.show();
            
            //Close the actual window
            currentStage.close();
            
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    
    private boolean showAlert() {
        // Create an alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Alert");
        alert.setHeaderText("¿Seguro que quiere salir?");
        alert.setContentText("Asegurese de tener todo en orden antes de cerrar la aplicación por favor");

        // Show the alert and wait for the user to respond
        Optional<ButtonType> result = alert.showAndWait();
    
        // Return true if the user clicked OK, false if they clicked Cancel
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    @FXML
    private void switchToCreateAccount() throws IOException {
        Main.setRoot("createAccountView");
    }
    
    @FXML
    public void initialize() {
        // TODO
    }    
    
}
