package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.logic.WindowShowing;
import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.paint.Color.RED;

@Controller
public class ClientViewController {

    private final WindowShowing windowShowing;
    @FXML
    private TextField txtFieldPassword, txtFieldName;
    @FXML
    private Label txtDebugForm;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    public ClientViewController(WindowShowing windowShowing) {
        this.windowShowing = windowShowing;
    }

    //Intentar crear servicios y dentro de esos servicios hacer la logica donde se introduce el getText y toeso que el probblema es javafx
    @FXML
    private void enterApplication(ActionEvent event) {
        if (clientRepository.existsByClientNameAndClientPassword(txtFieldName.getText(), txtFieldPassword.getText())) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/supportView.fxml"));
                Parent root = loader.load();
                SupportViewController primaryController = loader.getController();
                primaryController.setAccountController(this);

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
                stage.setTitle("Almacen");
                stage.centerOnScreen();
                stage.setResizable(false);

                stage.setOnCloseRequest(e -> {
                    windowShowing.closeAllWindows();
                    if (showAlert()) {
                        stage.close();
                    } else {
                        e.consume();
                    }
                });

                stage.show();
                currentStage.close();

            } catch (IOException e) {
                txtDebugForm.setText("Ha ocurrido un error desconocido");
                txtDebugForm.setTextFill(RED);
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
        // TODO
    }

}
