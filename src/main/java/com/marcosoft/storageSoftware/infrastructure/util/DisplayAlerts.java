package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;

@Lazy
@Component
public class DisplayAlerts {

    private void setAlertIconAndCSS(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

        // Cargar icono usando URL
        URL iconUrl = getClass().getResource("/images/lc_logo.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toString()));
        }

        stage.initStyle(StageStyle.TRANSPARENT);
        DialogPane dialogPane = alert.getDialogPane();

        // Cargar CSS usando URL - FORMA CORRECTA para JAR
        URL cssUrl = getClass().getResource("/Styles.css");
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("No se pudo encontrar el archivo CSS en el JAR");
        }

        dialogPane.getStyleClass().add("alert");
    }

    public void showAlert( String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setAlertIconAndCSS(alert);
        alert.setHeaderText("Mensaje");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertIconAndCSS(alert);
        alert.setHeaderText("¿Está seguro?");
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("confirmation");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setAlertIconAndCSS(alert);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
