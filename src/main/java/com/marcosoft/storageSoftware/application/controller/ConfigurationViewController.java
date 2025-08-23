package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.infrastructure.config.DatabaseManager;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.marcosoft.storageSoftware.Main.primaryStage;
import static com.marcosoft.storageSoftware.Main.springFXMLLoader;

/**
 * FXML Controller class
 *
 * @author MazMorr
 */
@Lazy
@Controller
public class ConfigurationViewController {
    private Client client;

    private final ClientServiceImpl clientService;
    private final SceneSwitcher sceneSwitcher;
    private final DisplayAlerts displayAlerts;
    private final UserLogged userLogged;
    private final InventoryServiceImpl inventoryService;
    private final LicenseValidator licenseValidator;
    private final SellRegistryServiceImpl sellRegistryService;
    private final DatabaseManager databaseManager;

    /**
     * Instantiates a new Configuration view controller.
     *
     * @param clientService the client service
     * @param sceneSwitcher the scene switcher
     */
    public ConfigurationViewController(
            ClientServiceImpl clientService, SceneSwitcher sceneSwitcher, DisplayAlerts displayAlerts,
            UserLogged userLogged, LicenseValidator licenseValidator,
            InventoryServiceImpl inventoryService, SellRegistryServiceImpl sellRegistryService, DatabaseManager databaseManager) {
        this.displayAlerts = displayAlerts;
        this.userLogged = userLogged;
        this.licenseValidator = licenseValidator;
        this.sellRegistryService = sellRegistryService;
        this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
        this.databaseManager = databaseManager;
    }

    @FXML
    private Label lblSell, lblClientName, lblUser, lblProducts, lblCompany, lblDateLicense;

    /**
     * Close session.
     */
    @FXML
    void closeSession() {
        try {
            // Marcar usuario como inactivo
            clientService.updateIsClientActiveByClientName(false, client.getClientName());

            // Cargar la pantalla de login
            Parent root = springFXMLLoader.load("/views/clientView.fxml");

            // Preparar nueva ventana
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/RTS_logo.png")).toString()));
            loginStage.setTitle("Iniciar Sesión");
            loginStage.centerOnScreen();
            loginStage.setResizable(false);

            // Mostrar la ventana de login y cerrar la actual
            Stage currentStage = (Stage) lblUser.getScene().getWindow();

            //TODO: hay que cerrar también todas las demás ventanas si están, hay q ver como se puede hacer eso
            loginStage.show();
            currentStage.close();

        } catch (IOException e) {
            displayAlerts.showError("Error al cargar la pantalla de inicio de sesión: " + e.getMessage());
        } catch (Exception e) {
            displayAlerts.showError("Error inesperado: " + e.getMessage());
        }
    }


    private void closeAllWindows() {
        Platform.runLater(() -> {
            // Crear una copia de la lista para evitar modificaciones concurrentes
            List<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Stage stage) {
                    if (primaryStage != null && !stage.equals(primaryStage) && stage.isShowing()) {
                        stage.close();
                    }
                }
            }
        });
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        initAllLabels();
    }

    private void initAllLabels() {
        client = userLogged.getClient();
        lblClientName.setText(client.getClientName());
        int productCounter = 0;
        int sellCounter;
        try {
            try {
                sellCounter = sellRegistryService.getAllSellRegistriesByClient(client).size();
            } catch (NullPointerException e) {
                throw new RuntimeException(e);
            }

            List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
            for (Inventory inv : inventories) {
                if (inv.getAmount() != null) {
                    productCounter += inv.getAmount();
                }
            }
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }

        lblUser.setText("Usuario: " + client.getClientName());
        lblCompany.setText("Compañía: " + client.getClientCompany());
        lblProducts.setText("Productos: " + productCounter);
        lblSell.setText("Ventas: " + sellCounter);
        lblDateLicense.setText("Fecha Vencimiento Licencia: "
                + LocalDate.now().until(licenseValidator.getRemainingTime()).getDays() + " Días");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/supportView.fxml");
    }

    @FXML
    public void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/warehouseView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/registryView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/balanceView.fxml");
    }

    @FXML
    public void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/expenseView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchView(actionEvent, "/views/sellView.fxml");
    }

    @FXML
    public void ImportDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de respaldo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar importación");
            confirmAlert.setHeaderText("¿Está seguro de importar los datos?");
            confirmAlert.setContentText("Esta acción sobrescribirá los datos actuales");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Importación exitosa");
                    successAlert.setHeaderText("Datos importados correctamente");
                    successAlert.showAndWait();

                    // Actualizar la interfaz después de importar
                    initAllLabels();
                } catch (Exception e) {
                    displayAlerts.showError("Error al importar datos: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void exportDatabase() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para guardar la copia de seguridad");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            try {
                String destinationPath = selectedDirectory.getAbsolutePath() +
                        "\\Database_backup_" +
                        LocalDate.now() +
                        ".zip"; // H2 crea un zip por defecto

                databaseManager.exportDB(destinationPath);
                displayAlerts.showAlert("Base de datos exportada correctamente");

            } catch (Exception e) {
                displayAlerts.showError("Error al exportar la base de datos: " + e.getMessage());
            }
        }
    }


}