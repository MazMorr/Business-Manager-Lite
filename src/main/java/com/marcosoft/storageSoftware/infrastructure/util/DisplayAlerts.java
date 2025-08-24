package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Lazy
@Component
public class DisplayAlerts {

    private void setAlertIconAndCSS(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/images/lc_logo.png"));
        stage.initStyle(StageStyle.TRANSPARENT);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
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
        alert.setHeaderText("Confirmación");
        alert.setHeaderText("¿Está seguro?");
        alert.setContentText(message);

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
