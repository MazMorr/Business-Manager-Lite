
package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.logic.SceneSwitcher;
import com.marcosoft.storageSoftware.logic.WindowShowing;
import com.marcosoft.storageSoftware.model.Client;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.io.IOException;

public class ExistencyViewController  {
    
    @FXML Button btnFilter;
    @FXML TableView<Client> tblExistency;
    private ObservableList<Client> product;
    
    private final WindowShowing windowShowing;
    private final SceneSwitcher sceneSwitcher;

    public ExistencyViewController() {
        windowShowing= new WindowShowing();
        sceneSwitcher= new SceneSwitcher();
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
    @FXML void changeProduct(ActionEvent event) throws IOException {
        //La lógica sería que al tocar el botón primero q todo debías haber seleccionado
        //un producto y entonces desplegaría un buyView donde ya vendría con todos los datos
        //Del producto seleccionado y el decide que cambiar
    }
    
    @FXML
    private void displaySellView(ActionEvent event) throws IOException { 
        String errorMessage= "Ya hay una ventana de Ventas abierta";
        String fxmlPath="/sellView.fxml";
        int aux=0;
        windowShowing.displayAssistance(windowShowing.isSellViewShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void displayBuyView() throws IOException {
        String errorMessage= "Ya hay una ventana de Compras  abierta";
        String fxmlPath="/buyView.fxml";
        int aux=1;
        windowShowing.displayAssistance(windowShowing.isBuyViewShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void displayFilterView(ActionEvent event) throws IOException {
        String errorMessage= "Ya hay una ventana de filtros para existencias abierta";
        String fxmlPath="/filterExistencyView.fxml";
        int aux=2;
        windowShowing.displayAssistance(windowShowing.isFilterViewShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void displayConfigurationView(ActionEvent event) throws IOException {
        String errorMessage= "Ya hay una ventana de Configuración abierta";
        String fxmlPath="/configurationView.fxml";
        int aux=3;
        windowShowing.displayAssistance(windowShowing.isConfigurationShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    public void initialize() {
        // TODO
    }    
    
}
