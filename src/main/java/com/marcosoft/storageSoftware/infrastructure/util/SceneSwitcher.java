package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SceneSwitcher {

    private final SpringFXMLLoader springFXMLLoader;

    @Autowired
    public SceneSwitcher(SpringFXMLLoader springFXMLLoader) {
        this.springFXMLLoader = springFXMLLoader;
    }

    /**
     * Cambia la raíz de la escena usando un ActionEvent.
     */
    public void setRootWithEvent(ActionEvent event, String fxmlFile) {
        setRoot((Node) event.getSource(), fxmlFile, null);
    }

    /**
     * Cambia la raíz de la escena usando cualquier Node.
     */
    public void setRoot(Node node, String fxmlFile) {
        setRoot(node, fxmlFile, null);
    }

    /**
     * Cambia la raíz de la escena y opcionalmente el título de la ventana.
     */
    public void setRoot(Node node, String fxmlFile, String windowTitle) {
        try {
            Parent root = (Parent) springFXMLLoader.load(fxmlFile);
            Scene scene = node.getScene();
            scene.setRoot(root);

            if (windowTitle != null) {
                Stage stage = (Stage) scene.getWindow();
                stage.setTitle(windowTitle);
            }
        } catch (IOException e) {
            showError("No se pudo cargar la vista: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje de error simple.
     */
    private void showError(String message) {
        System.err.println(message);
        // Aquí puedes agregar un Alert si quieres mostrarlo en la interfaz gráfica.
    }
}
