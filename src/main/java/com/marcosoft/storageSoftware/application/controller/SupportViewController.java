package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
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
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller for the support view.
 * Handles welcome messages, navigation, and license information display.
 */
@RequiredArgsConstructor
@Controller
public class SupportViewController {

    // Service and utility dependencies
    private final UserLogged userLogged;
    private final LicenseValidator licenseValidator;
    private final DisplayAlerts displayAlerts;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;
    private final Environment env;

    // FXML UI components
    @FXML
    private Label lblWelcome, versionLabel, lblLicenseDays, lblClientName, lblWelcomeTitle;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets welcome labels and displays the active client name.
     */
    @FXML
    private void initialize() {
        // Reference to the account controller for session management
        Client client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        initWelcomeLabels();

        Platform.runLater(this::initCurrencyDefaultValues);
    }

    /**
     * Sets the account controller reference for session management.
     *
     * @param loginViewController the client view controller
     */
    public void setAccountController(LoginViewController loginViewController) {
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
                    case "MLC" -> currency = new Currency(null, currencyName, 195.00);
                    case "CUP" -> currency = new Currency(null, currencyName, 1.00);
                    case "USD" -> currency = new Currency(null, currencyName, 410.00);
                    case "EUR" -> currency = new Currency(null, currencyName, 460.00);
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
        long daysRemaining = licenseValidator.getDaysRemaining();
        if (daysRemaining <= 10) {
            lblLicenseDays.setStyle("-fx-text-fill: #ff9b9b;");
        }
        lblLicenseDays.setText(daysRemaining + " Días");
        versionLabel.setText(env.getProperty("app.version"));
        lblWelcomeTitle.setText("Bienvenid@, " + userLogged.getName());
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

    @FXML
    private void switchToRegistry(ActionEvent event) {
        sceneSwitcher.switchToRegistry(event);
    }

    @FXML
    private void switchToExpense(ActionEvent event) {
        sceneSwitcher.switchToExpense(event);
    }

    @FXML
    private void switchToConfiguration(ActionEvent event) {
        sceneSwitcher.switchToConfiguration(event);
    }

    @FXML
    public void switchToWarehouse(ActionEvent event) {
        sceneSwitcher.switchToWarehouse(event);
    }

    @FXML
    public void switchToBalance(ActionEvent event) {
        sceneSwitcher.switchToBalance(event);
    }

    @FXML
    public void switchToSell(ActionEvent event) {
        sceneSwitcher.switchToSell(event);
    }

    @FXML
    public void switchToBuy(ActionEvent actionEvent) {
        sceneSwitcher.switchToBuy(actionEvent);
    }

    @FXML
    private void licenseInformation() {
        displayAlerts.showAlert("Pasado el tiempo disponible para renovar su licencia, el programa se bloqueará " +
                "instantáneamente y no podrá ser usado. Por favor, llame al +53 5550 5961 antes de que eso ocurra" +
                " para renovar su licencia y continuar usando la aplicación sin interrupciones.");
    }
}
