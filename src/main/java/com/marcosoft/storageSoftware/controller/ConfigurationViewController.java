package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.Client;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import com.marcosoft.storageSoftware.util.WindowShowing;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
@Controller
public class ConfigurationViewController implements Initializable {

    @FXML
    private MenuButton mbAppTheme, mbLanguage, mbResolution;
    @FXML
    private RadioMenuItem rdmiDarkTheme, rdmiLightTheme;
    @FXML
    private Label txtClientName, txtClientCompany;
    @FXML
    private ToggleGroup rdmiTheme, rdmiLanguage, rdmiResolution;

    @Autowired
    ClientServiceImpl clientServiceImpl;
    @Autowired
    SceneSwitcher sceneSwitcher;
    @Autowired
    WindowShowing windowShowing;

    @FXML
    void switchLanguageToEnglish(ActionEvent event) {

    }

    @FXML
    void switchLanguageToSpanish(ActionEvent event) {

    }

    @FXML
    void closeSession() {
        Client client = clientServiceImpl.getByIsClientActive(true);
        clientServiceImpl.updateIsClientActiveByClientName(false, client.getClientName());
        //Aqui básicamente cerraría todas las ventanas y volvería a iniciar la aplicación
        Main.launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Recoge los nombres y la compañia del usuario
        txtClientName.setText(clientServiceImpl.getByIsClientActive(true).getClientName());
        txtClientCompany.setText(clientServiceImpl.getByIsClientActive(true).getClientCompany());
    }

    @FXML
    public void switchToStock(ActionEvent event) {
        sceneSwitcher.setRootWithEvent(event, "/investmentView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    public void switchToWallet(ActionEvent event) {
        sceneSwitcher.setRootWithEvent(event, "/warehouseView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    public void switchToSupport(ActionEvent event) {
        sceneSwitcher.setRootWithEvent(event, "/supportView.fxml");
        windowShowing.closeAllWindows();
    }

    @FXML
    public void switchToRegistry(ActionEvent event) {
        sceneSwitcher.setRootWithEvent(event, "/registryView.fxml");
        windowShowing.closeAllWindows();
    }
}
