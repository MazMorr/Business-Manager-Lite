package com.marcosoft.storageSoftware.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
public class ConfigurationViewController implements Initializable {
    
    @FXML private Button btnAdjustAdjust;
    @FXML private Button btnExistencyAdjust;
    @FXML private Button btnRegistryAdjust;
    @FXML private Button btnSupportAdjust;
    @FXML private MenuButton mbAppTheme;
    @FXML private MenuButton mbUsers;
    @FXML private MenuItem miChangeUser;
    @FXML private MenuButton mbLanguage;
    @FXML private MenuButton mbResolution;
    @FXML private RadioMenuItem rdmiDarkTheme;
    @FXML private RadioMenuItem rdmiLightTheme;
    @FXML private Label txtTitleAdjust;
    
    
    @FXML
    void switchLanguageToEnglish(ActionEvent event) {

    }

    @FXML
    void switchLanguageToSpanish(ActionEvent event) {

    }
    
    private void setRoot(ActionEvent event, String fxmlFile) throws IOException {
        // Load the new FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Get the current scene and set the new root
        Scene currentScene = ((Node) event.getSource()).getScene();
        currentScene.setRoot(root);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
