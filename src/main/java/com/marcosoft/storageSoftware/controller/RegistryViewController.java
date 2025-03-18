package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.logic.SceneSwitcher;
import com.marcosoft.storageSoftware.logic.WindowShowing;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistryViewController implements Initializable {
    private final WindowShowing windowShowing;
    private final SceneSwitcher sceneSwitcher;
    
    public RegistryViewController(){
        windowShowing= new WindowShowing();
        sceneSwitcher= new SceneSwitcher();
    }

    @FXML 
    private void switchToSupport(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/supportView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    private void switchToExistency(ActionEvent event) throws IOException {
        sceneSwitcher.setRoot(event, "/stockView.fxml");
        windowShowing.closeAllWindows();
    }
    
   @FXML
    private void displayConfigurationView(ActionEvent event) throws IOException {
        String errorMessage= "Ya hay una ventana de Configuración abierta";
        String fxmlPath="/configurationView.fxml";
        int aux=3;
        windowShowing.displayAssistance(windowShowing.isConfigurationShowing(), fxmlPath, errorMessage, aux);
    } 
    
    @FXML
    private void displayRegistryfilterView(ActionEvent event) throws IOException{
        String errorMessage= "Ya hay una ventana de filtros para registros abierta";
        String fxmlPath="/registryFilterView.fxml";
        int aux=4;
        windowShowing.displayAssistance(windowShowing.isRegistryFilterViewShowing(), fxmlPath, errorMessage, aux);
    }
    
    @FXML
    private void selected(MouseEvent event){
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
