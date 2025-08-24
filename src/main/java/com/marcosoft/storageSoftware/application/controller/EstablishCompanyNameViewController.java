package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;


@Controller
public class EstablishCompanyNameViewController {
    private Client client;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final DisplayAlerts displayAlerts;
    private final ConfigurationViewController configurationViewController;

    @FXML
    private TextField tfCompanyName;

    public EstablishCompanyNameViewController(UserLogged userLogged, ClientServiceImpl clientService, DisplayAlerts displayAlerts, ConfigurationViewController configurationViewController) {
        this.userLogged = userLogged;
        this.clientService = clientService;
        this.displayAlerts = displayAlerts;
        this.configurationViewController = configurationViewController;
    }

    @FXML
    private void initialize() {
        client = userLogged.getClient();
        if (client.getClientCompany() != null) {
            tfCompanyName.setText(client.getClientCompany());
        }
    }

    @FXML
    public void goOut() {
        Stage stage = (Stage) tfCompanyName.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void updateCompanyName() {
        if (!tfCompanyName.getText().isEmpty() && tfCompanyName.getText().length() < 16) {
            client.setClientCompany(tfCompanyName.getText());
            clientService.save(client);
            displayAlerts.showAlert("Nombre de compañía asignado correctamente");
            configurationViewController.initialize();
            goOut();
        } else if (tfCompanyName.getText().length() > 16) {
            displayAlerts.showAlert("El nombre de la compañía es mayor a 16 carácteres");
        } else if (tfCompanyName.getText().isEmpty()) {
            displayAlerts.showAlert("El campo está vacío");
        } else if (tfCompanyName.getText().equals(client.getClientCompany())) {
            displayAlerts.showAlert("El nombre de la compañía es el mismo que ya había sido asignado");
        }
    }
}
