package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
@Lazy
@Controller
public class ConfigurationViewController {

    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;

    /**
     * Instantiates a new Configuration view controller.
     *
     * @param clientService the client service
     * @param sceneSwitcher the scene switcher
     */
    @Lazy
    public ConfigurationViewController(ClientServiceImpl clientService, SceneSwitcher sceneSwitcher) {
        this.clientService = clientService;
        this.sceneSwitcher = sceneSwitcher;
    }

    @FXML
    private MenuButton mbAppTheme;
    @FXML
    private RadioMenuItem rdmiDarkTheme, rdmiLightTheme;
    @FXML
    private Label txtClientName, txtClientCompany;
    @FXML
    private ToggleGroup rdmiTheme;

    /**
     * Close session.
     */
    @FXML
    void closeSession() {
        Client client = clientService.getByIsClientActive(true);
        clientService.updateIsClientActiveByClientName(false, client.getClientName());
        //Aqui básicamente cerraría todas las ventanas y volvería a iniciar la aplicación
        Main.launch();
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {

        //Recoge los nombres y la compañia del usuario
        txtClientName.setText(clientService.getByIsClientActive(true).getClientName());
        txtClientCompany.setText(clientService.getByIsClientActive(true).getClientCompany());
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================

    /**
     * Switch to support.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    /**
     * Switch to warehouse.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    /**
     * Switch to registry.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    /**
     * Switch to balance.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    /**
     * Switch to investment.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/investmentView.fxml");
    }

    /**
     * Switch to sell.
     *
     * @param actionEvent the action event
     */
    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }

}
