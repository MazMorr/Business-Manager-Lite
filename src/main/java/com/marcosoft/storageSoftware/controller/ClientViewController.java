package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.model.UserLogged;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import com.marcosoft.storageSoftware.util.WindowShowing;
import com.marcosoft.storageSoftware.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.paint.Color.RED;

@Controller
public class ClientViewController {

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    private final WindowShowing windowShowing;

    @FXML
    private TextField txtFieldName;
    @FXML
    private PasswordField txtFieldPassword;
    @FXML
    private Label txtDebugForm;

    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    UserLogged userLogged;

    @Autowired
    public ClientViewController(WindowShowing windowShowing) {
        this.windowShowing = windowShowing;
    }

    @FXML
    private void enterApplication(ActionEvent event) {
        String username = txtFieldName.getText();
        String password = txtFieldPassword.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Por favor, ingrese usuario y contraseña.");
            return;
        }

        if (clientServiceImpl.existsByClientNameAndClientPassword(username, password)) {
            try {
                clientServiceImpl.updateIsClientActiveByClientName(true, username);
                userLogged.setName(username);

                // Usar SpringFXMLLoader para cargar el archivo FXML
                Parent root = (Parent) springFXMLLoader.load("/supportView.fxml");

                // Obtener el controlador del archivo FXML cargado
                SupportViewController primaryController = springFXMLLoader.getController(SupportViewController.class);
                primaryController.setAccountController(this);

                // Obtener la ventana actual
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Crear una nueva ventana para la vista de soporte
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
                stage.setTitle("Almacenamiento");
                stage.centerOnScreen();
                stage.setMinWidth(1100);
                stage.setMinHeight(650);

                // Manejar el cierre de la ventana
                stage.setOnCloseRequest(e -> {
                    windowShowing.closeAllWindows();
                    if (showAlert()) {
                        clientServiceImpl.updateIsClientActiveByClientName(false, username);
                        stage.close();
                    } else {
                        e.consume();
                    }
                });

                // Mostrar la nueva ventana y cerrar la actual
                stage.show();
                currentStage.close();

            } catch (IOException e) {
                showError("Ha ocurrido un error al cargar la aplicación.");
                e.printStackTrace();
            }
        } else {
            showError("Usuario o contraseña incorrecta.");
        }
    }

    private void showError(String message) {
        txtDebugForm.setText(message);
        txtDebugForm.setTextFill(RED);
    }

    private boolean showAlert() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("¿Seguro que quiere salir?");
        alert.setContentText("Asegúrese de tener todo en orden antes de cerrar la aplicación, por favor.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void switchToCreateClient() {
        clearFields();
        Main.setRoot("createClientView");
    }

    private void clearFields() {
        txtFieldName.clear();
        txtFieldPassword.clear();
        txtDebugForm.setText("");
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Cierra cualquier sesión activa previa
            if (clientServiceImpl.existsByIsClientActive(true)) {
                clientServiceImpl.updateIsClientActiveByClientName(false,
                        clientServiceImpl.getByIsClientActive(true).getClientName());
            }
            clearFields();
        });

    }
}
