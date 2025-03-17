package com.marcosoft.storageSoftware.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
public class FilterExistencyViewController implements Initializable {

        
    @FXML
    private void closeWindow(ActionEvent event){
        // Get the source of the event (the button that was clicked)
        Node source = (Node) event.getSource();
        
        // Get the stage from the source
        Stage stage = (Stage) source.getScene().getWindow();
        
        // Close the stage
        stage.close();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
