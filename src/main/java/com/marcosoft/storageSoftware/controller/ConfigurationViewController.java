package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.service.impl.ClientServiceImpl;
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
    private Button btnAdjustAdjust, btnStockAdjust, btnRegistryAdjust, btnSupportAdjust;
    @FXML
    private MenuButton mbAppTheme, mbUsers, mbLanguage, mbResolution;
    @FXML
    private MenuItem miChangeUser;
    @FXML
    private RadioMenuItem rdmiDarkTheme, rdmiLightTheme;
    @FXML
    private Label txtTitleAdjust, txtUser, txtCompany;

    @Autowired
    ClientServiceImpl clientServiceImpl;

    @FXML
    void switchLanguageToEnglish(ActionEvent event) {

    }

    @FXML
    void switchLanguageToSpanish(ActionEvent event) {

    }

    @FXML
    void closeSession() {
        Client client = clientServiceImpl.findByIsClientActive(true);
        clientServiceImpl.updateIsClientActiveByClientName(false, client.getClientName());
        //Aqui básicamente cerraría todas las ventanas y volvería a iniciar la aplicación
        Main.launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
