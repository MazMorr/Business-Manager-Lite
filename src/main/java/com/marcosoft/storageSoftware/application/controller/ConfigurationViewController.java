package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.infrastructure.config.DatabaseManager;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.marcosoft.storageSoftware.Main.springFXMLLoader;

@RequiredArgsConstructor
@Controller
public class ConfigurationViewController {

    private final SceneSwitcher sceneSwitcher;
    private final DisplayAlerts displayAlerts;
    private final UserLogged userLogged;
    private final InventoryServiceImpl inventoryService;
    private final LicenseValidator licenseValidator;
    private final SellRegistryServiceImpl sellRegistryService;
    private final DatabaseManager databaseManager;

    @FXML
    private Label lblSell, lblClientName, lblUser, lblProducts, lblCompany, lblDateLicense;

    @FXML
    public void initialize() {
        initAllLabels();
    }

    @FXML
    void closeSession() {
        try {
            // Cerrar todas las ventanas de la aplicación
            closeAllWindows();

            // Cargar la pantalla de login después de cerrar todas las ventanas
            Platform.runLater(() -> {
                try {
                    Parent root = springFXMLLoader.load("/views/loginView.fxml");

                    // Preparar nueva ventana
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(root));
                    loginStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/lc_logo.png")).toString()));
                    loginStage.setTitle("Iniciar Sesión");
                    loginStage.centerOnScreen();
                    loginStage.setResizable(false);

                    // Mostrar la ventana de login
                    loginStage.show();
                } catch (IOException e) {
                    displayAlerts.showError("Error al cargar la pantalla de inicio de sesión: " + e.getMessage());
                } catch (Exception e) {
                    displayAlerts.showError("Error inesperado: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            displayAlerts.showError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    private void closeAllWindows() {
        // Obtener todas las ventanas y cerrarlas
        List<Window> windows = new ArrayList<>(Window.getWindows());
        for (Window window : windows) {
            if (window instanceof Stage stage) {
                // Cerrar la ventana de manera forzada
                stage.close();
            }
        }

        // Forzar la finalización de cualquier diálogo o ventana modal
        Platform.runLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage) {
                    ((Stage) window).close();
                }
            }
        });
    }

    private void initAllLabels() {
        Client client = userLogged.getClient();
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
        String company = client.getClientCompany() == null || client.getClientCompany().isEmpty() ? "Sin Compañía Asociada" : client.getClientCompany();
        lblCompany.setText("Compañía: " + company);
        lblProducts.setText("Productos: " + productCounter);
        lblSell.setText("Ventas: " + sellCounter);
        lblDateLicense.setText("Fecha Vencimiento Licencia: "
                + LocalDate.now().until(licenseValidator.getRemainingTime()).getDays() + " Días");
    }

    @FXML
    private void switchToSupport(ActionEvent actionEvent) {
        sceneSwitcher.switchToSupport(actionEvent);
    }

    @FXML
    private void switchToWarehouse(ActionEvent actionEvent) {
        sceneSwitcher.switchToWarehouse(actionEvent);
    }

    @FXML
    private void switchToRegistry(ActionEvent actionEvent) {
        sceneSwitcher.switchToRegistry(actionEvent);
    }

    @FXML
    private void switchToBalance(ActionEvent actionEvent) {
        sceneSwitcher.switchToBalance(actionEvent);
    }

    @FXML
    private void switchToExpense(ActionEvent actionEvent) {
        sceneSwitcher.switchToExpense(actionEvent);
    }

    @FXML
    private void switchToSell(ActionEvent actionEvent) {
        sceneSwitcher.switchToSell(actionEvent);
    }

    @FXML
    public void switchToBuy(ActionEvent actionEvent) {
        sceneSwitcher.switchToBuy(actionEvent);
    }

    @FXML
    public void ImportDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de base de datos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("H2 Database Files", "*.mv.db")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar importación");
            confirmAlert.setHeaderText("¿Está seguro de importar la base de datos?");
            confirmAlert.setContentText("Esta acción sobrescribirá los datos actuales y deberá reiniciar la aplicación.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    databaseManager.importDB(file.getAbsolutePath());
                    displayAlerts.showAlert("Base de datos importada correctamente. Vuelva a iniciar la aplicación");

                    // Reiniciar la aplicación
                    restartApplication();
                } catch (Exception e) {
                    displayAlerts.showError("Error al importar la base de datos: " + e.getMessage());
                }
            }
        }
    }

    private void restartApplication() {
        try {
            // Cerrar la conexión actual
            DatabaseManager.closeAllConnections();

            // Reiniciar la aplicación
            Platform.exit();
            Runtime.getRuntime().exec("java -jar " + System.getProperty("java.class.path"));
            System.exit(0);
        } catch (Exception e) {
            displayAlerts.showError("Error al reiniciar: " + e.getMessage());
        }
    }

    @FXML
    public void exportDatabase() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para guardar la copia de seguridad");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            try {
                // Usar timestamp para hacer el nombre único
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String destinationPath = selectedDirectory.getAbsolutePath()
                        + File.separator + "Database_backup_" + timestamp + ".zip";

                databaseManager.exportDB(destinationPath);
                displayAlerts.showAlert("Base de datos exportada correctamente");

            } catch (Exception e) {
                displayAlerts.showError("Error al exportar la base de datos: " + e.getMessage());
            }
        }
    }

    @FXML
    public void displayEstablishCompanyName() throws SceneSwitcher.WindowLoadException {
        sceneSwitcher.displayWindow(
                "Establecer Nombre Compañía", "/images/lc_logo.png", "/views/establishCompanyNameView.fxml"
        );
    }
}