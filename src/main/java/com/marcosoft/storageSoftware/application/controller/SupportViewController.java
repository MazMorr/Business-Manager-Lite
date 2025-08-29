package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller for the support view.
 * Handles welcome messages, navigation, and license information display.
 */
@Lazy
@Controller
public class SupportViewController {
    // Reference to the account controller for session management
    private Client client;
    private LoginViewController accountController;

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final LicenseValidator licenseValidator;
    private final DisplayAlerts displayAlerts;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;

    /**
     * Constructor for dependency injection.
     * @param currencyService the currency service
     * @param sceneSwitcher the scene switcher
     * @param userLogged the user logged
     * @param licenseValidator the license validator
     */
    public SupportViewController(
            CurrencyServiceImpl currencyService, SceneSwitcher sceneSwitcher,
            UserLogged userLogged, LicenseValidator licenseValidator, DisplayAlerts displayAlerts
    ) {
        this.userLogged = userLogged;
        this.licenseValidator = licenseValidator;
        this.currencyService = currencyService;
        this.sceneSwitcher = sceneSwitcher;
        this.displayAlerts = displayAlerts;
    }

    // FXML UI components
    @FXML
    private Label lblWelcome, versionLabel, lblLicenseDays, lblClientName, lblWelcomeTitle;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets welcome labels and displays the active client name.
     */
    @FXML
    private void initialize() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());

        Platform.runLater(() -> {
            initWelcomeLabels();
            initCurrencyDefaultValues();


        });
    }

    /**
     * Sets the account controller reference for session management.
     * @param loginViewController the client view controller
     */
    public void setAccountController(LoginViewController loginViewController) {
        this.accountController = loginViewController;
        System.out.println("Controlador de cuenta configurado: " + loginViewController);
    }

    /**
     * Initializes default currencies if they do not exist in the database.
     */
    private void initCurrencyDefaultValues() {
        List<String> defaultCurrenciesName = List.of("MLC", "CUP", "USD", "EUR");
        for (String currencyName : defaultCurrenciesName) {
            if (!currencyService.existsByCurrencyName(currencyName)) {
                Currency currency = new Currency(null, currencyName, 0.0);
                switch (currencyName) {
                    case "MLC" -> currency = new Currency(null, currencyName, 200.00);
                    case "CUP" -> currency = new Currency(null, currencyName, 1.00);
                    case "USD" -> currency = new Currency(null, currencyName, 120.00);
                    case "EUR" -> currency = new Currency(null, currencyName, 160.00);
                }

                currencyService.save(currency);
            }
        }
    }

    /**
     * Initializes welcome labels with version and welcome message.
     * The welcome message is shown in Spanish.
     */
    private void initWelcomeLabels() {
        lblLicenseDays.setText(licenseValidator.getDaysRemaining() + " Días");
        versionLabel.setText("0.9.9");
        lblWelcomeTitle.setText("Bienvenido, " + userLogged.getName());
        lblWelcome.setText(
                """
                        Este sistema ha sido diseñado para brindarle un control eficiente y seguro sobre los recursos de su negocio. \
                        Aquí podrá gestionar inventarios, gastos, ventas y mucho más de manera sencilla y centralizada.
                        
                        Recuerde que el uso de este software está protegido por una licencia. La distribución o comercialización fuera de los canales oficiales \
                        puede resultar en sanciones legales, multas o la cancelación permanente de la licencia.
                        
                        Para cualquier duda, sugerencia o reporte de errores, utilice la sección 'Soporte' ubicada a la derecha de este mensaje. \
                        Nuestro equipo estará siempre disponible para ayudarle y mejorar su experiencia.
                        
                        ¡Gracias por confiar en nosotros!"""
        );
    }

    /**
     * Navigates to the registry view.
     */
    @FXML
    private void switchToRegistry(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/registryView.fxml");
    }

    /**
     * Navigates to the investment view.
     */
    @FXML
    private void switchToExpense(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/expenseView.fxml");
    }

    /**
     * Navigates to the configuration view.
     */
    @FXML
    private void switchToConfiguration(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/configurationView.fxml");
    }

    /**
     * Navigates to the warehouse view.
     * @param event the event
     */
    @FXML
    public void switchToWarehouse(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/warehouseView.fxml");
    }

    /**
     * Navigates to the balance view.
     * @param event the event
     */
    @FXML
    public void switchToBalance(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/balanceView.fxml");
    }

    @FXML
    public void switchToInventory(ActionEvent event) {
        sceneSwitcher.switchView(event, "/views/sellView.fxml");
    }

    @FXML
    private void licenseInformation() {
        displayAlerts.showAlert("Pasado el tiempo disponible para renovar su licencia, el programa se bloqueará " +
                "instantáneamente y no podrá ser usado. Por favor, llame al +53 5550 5961 antes de que eso ocurra" +
                " para renovar su licencia y continuar usando la aplicación sin interrupciones.");
    }
}
