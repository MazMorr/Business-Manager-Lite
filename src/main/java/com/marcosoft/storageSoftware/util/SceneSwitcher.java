package com.marcosoft.storageSoftware.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SceneSwitcher {

    public SceneSwitcher() {
    }

    public void setRoot(ActionEvent event, String fxmlFile) throws IOException {
        // Load the new FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Get the current scene and set the new root
        Scene currentScene = ((Node) event.getSource()).getScene();
        currentScene.setRoot(root);
    }
}
