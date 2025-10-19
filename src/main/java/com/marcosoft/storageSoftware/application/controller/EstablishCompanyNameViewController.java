package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class EstablishCompanyNameViewController {
    private Client client;

    private final UserLogged userLogged;
    private final ClientServiceImpl clientService;
    private final DisplayAlerts displayAlerts;
    private final ConfigurationViewController configurationViewController;
    private final SceneSwitcher sceneSwitcher;

    @FXML
    private TextField tfCompanyName;

    @FXML
    private void initialize() {
        client = userLogged.getClient();
        if (client.getClientCompany() != null) {
            tfCompanyName.setText(client.getClientCompany());
        }
    }

    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfCompanyName);
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
