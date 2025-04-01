package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import com.marcosoft.storageSoftware.util.WindowShowing;
import com.marcosoft.storageSoftware.Main;
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
    private Label txtDebugForm;

    @Autowired
    ClientServiceImpl clientServiceImpl;
    @FXML
    private PasswordField txtFieldPassword;

    @Autowired
    public ClientViewController(WindowShowing windowShowing) {
        this.windowShowing = windowShowing;
    }

    @FXML
    private void enterApplication(ActionEvent event) {
        if (clientServiceImpl.existsByClientNameAndClientPassword(txtFieldName.getText(), txtFieldPassword.getText())) {
            try {
                System.out.println("Usuario autenticado. Cargando la vista de soporte...");
                clientServiceImpl.updateIsClientActiveByClientName(true, txtFieldName.getText());

                // Usar SpringFXMLLoader para cargar el archivo FXML
                Parent root = (Parent) springFXMLLoader.load("/supportView.fxml");
                System.out.println("Vista de soporte cargada correctamente.");

                // Obtener el controlador del archivo FXML cargado
                SupportViewController primaryController = springFXMLLoader.getController(SupportViewController.class);
                primaryController.setAccountController(this);
                System.out.println("Controlador configurado correctamente.");

                // Obtener la ventana actual
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Crear una nueva ventana para la vista de soporte
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
                stage.setTitle("Almacenamiento");
                stage.centerOnScreen();

                // Establecer el tamaño mínimo de la ventana
                stage.setMinWidth(999);
                stage.setMinHeight(699);


                // Manejar el cierre de la ventana
                stage.setOnCloseRequest(e -> {
                    windowShowing.closeAllWindows();
                    if (showAlert()) {
                        clientServiceImpl.updateIsClientActiveByClientName(false,
                                clientServiceImpl.findByIsClientActive(true).getClientName());
                        stage.close();
                    } else {
                        e.consume();
                    }
                });

                // Mostrar la nueva ventana y cerrar la actual
                stage.show();
                currentStage.close();
                System.out.println("Nueva ventana mostrada correctamente.");

            } catch (IOException e) {
                txtDebugForm.setText("Ha ocurrido un error desconocido");
                txtDebugForm.setTextFill(RED);
                e.printStackTrace();
            }
        } else {
            txtDebugForm.setText("Usuario o contraseña incorrecta");
            txtDebugForm.setTextFill(RED);
        }
    }

    private boolean showAlert() {
        // Create an alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Alert");
        alert.setHeaderText("¿Seguro que quiere salir?");
        alert.setContentText("Asegurese de tener todo en orden antes de cerrar la aplicación por favor");

        // Show the alert and wait for the user to respond
        Optional<ButtonType> result = alert.showAndWait();

        // Return true if the user clicked OK, false if they clicked Cancel
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void switchToCreateClient() {
        Main.setRoot("createClientView");
    }

    @FXML
    public void initialize() {
        if (clientServiceImpl.existsByIsClientActive(true)) {
            clientServiceImpl.updateIsClientActiveByClientName(false, clientServiceImpl.
                    getByClientNameAndClientPassword(txtFieldName.getText(), txtFieldPassword.getText())
                    .getClientName());
        }
    }
}
