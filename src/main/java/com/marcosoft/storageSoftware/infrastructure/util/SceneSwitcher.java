package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.Main;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class SceneSwitcher {

    private final SpringFXMLLoader springFXMLLoader;
    private final DisplayAlerts displayAlerts;

    public SceneSwitcher(DisplayAlerts displayAlerts, SpringFXMLLoader springFXMLLoader) {
        this.springFXMLLoader = springFXMLLoader;
        this.displayAlerts = displayAlerts;
    }

    /**
     * Cambia la raíz de la escena usando un ActionEvent.
     */
    public void setRootWithEvent(ActionEvent event, String fxmlFile) {
        setRoot((Node) event.getSource(), fxmlFile, null);
    }

    /**
     * Cambia la raíz de la escena y opcionalmente el título de la ventana.
     */
    public void setRoot(Node node, String fxmlFile, String windowTitle) {
        try {
            Parent root = springFXMLLoader.load(fxmlFile);
            Scene scene = node.getScene();
            scene.setRoot(root);

            if (windowTitle != null) {
                Stage stage = (Stage) scene.getWindow();
                stage.setTitle(windowTitle);
            }
        } catch (IOException e) {
            displayAlerts.showAlert("No se pudo cargar la vista: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            setRootWithEvent(actionEvent, fxmlPath);
        } catch (Exception e) {
            displayAlerts.showAlert("Error al cambiar de vista: " + e.getMessage());
        }
    }

    public void displayWindow(String title, String logoPath, String fxmlPath){
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage;

        try {
            stage = createStage(
                    context.getBean(SpringFXMLLoader.class).load(fxmlPath),
                    title,
                    logoPath
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        stage.setOnCloseRequest(event -> {
        });
        stage.show();
    }

    /**
     * Utility method to create and configure a new stage.
     */
    private Stage createStage(Parent root, String title, String iconPath) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString()));
        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }
}
