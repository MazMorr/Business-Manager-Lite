package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Lazy
@Component
public class DisplayAlerts {

    private void setAlertIcon(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/lc_logo.png"));
    }

    public void showAlert( String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setAlertIcon(alert);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertIcon(alert);
        alert.setTitle("Confirmación");
        alert.setHeaderText("¿Está seguro?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setAlertIcon(alert);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
