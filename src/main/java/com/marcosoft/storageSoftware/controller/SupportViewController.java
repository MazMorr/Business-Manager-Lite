package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import org.springframework.stereotype.Controller;

@Controller
public class SupportViewController {

    @FXML
    private Label txtWelcome, versionLabel;
    public boolean configurationShowing;
    @Setter
    private ClientViewController accountController;
    private final WindowShowing windowShowing;
    @FXML
    private HBox vboxSlogan;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @Autowired
    private SceneSwitcher sceneSwitcher;

    public SupportViewController() {
        windowShowing = new WindowShowing();
    }

    public void setAccountController(ClientViewController clientViewController) {
        System.out.println("Controlador de cuenta configurado: " + clientViewController);
    }

    @FXML
    private void switchToRegistry(ActionEvent event) throws IOException {
        try{
            sceneSwitcher.setRoot(event, "/registryView.fxml");
            windowShowing.closeAllWindows();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    @FXML
    private void switchToExistency(ActionEvent event) throws IOException {
        try {
            sceneSwitcher.setRoot(event, "/stockView.fxml");
            windowShowing.closeAllWindows();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    private void displayConfigurationView(ActionEvent event) throws IOException {
        String errorMessage = "Ya hay una ventana de Configuración abierta";
        String fxmlPath = "/configurationView.fxml";
        int aux = 3;
        windowShowing.displayAssistance(windowShowing.isConfigurationShowing(), fxmlPath, errorMessage, aux);
    }

    @FXML
    private void licenseInformation(MouseEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Asistente de Ayuda");
        alert.setHeaderText("Información de la licencia");
        alert.setContentText("Pasado el tiempo disponible para renovar su licencia el programa se bloqueará"
                + " instantaneamente y no podrá ser usado, muy posiblemente pierda los datos"
                + " de la base de datos, por lo que es recomendable llamar al +53 5550 5961 antes de que eso pase para"
                + " que pueda revocar su licencia y continuar así con el consumo de este software.");
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        versionLabel.setText("Versión: Alfa");
        txtWelcome.setText("Este software le facilitará el control físico-financiero de su negocio " +
                "ya que tendrá a su disposición múltiples formas de manejar y almacenar todo tipo de productos, " +
                "permitiéndole tener un control total de las existencias en su almacén, sus costos " +
                "y ganancias de venta. Cualquier problema con el rendimiento de la aplicación por favor contactad " +
                "por cualquiera de las vías mostradas a la derecha. ¡Gracias por confiar en nosotros!"
        );
    }

    @FXML
    public void switchToWallet(ActionEvent actionEvent) {
    }

}
