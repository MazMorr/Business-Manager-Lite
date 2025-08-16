package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.infrastructure.security.LicenseValidator;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.SellRegistryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private final UserLogged userLogged;
    private final InventoryServiceImpl inventoryService;
    private final LicenseValidator licenseValidator;
    private final SellRegistryServiceImpl sellRegistryService;


    /**
     * Instantiates a new Configuration view controller.
     *
     * @param clientService the client service
     * @param sceneSwitcher the scene switcher
     */
    public ConfigurationViewController(
            ClientServiceImpl clientService, SceneSwitcher sceneSwitcher, UserLogged userLogged, LicenseValidator licenseValidator,
            InventoryServiceImpl inventoryService, SellRegistryServiceImpl sellRegistryService
    ) {
        this.userLogged = userLogged;
        this.licenseValidator = licenseValidator;
        this.sellRegistryService = sellRegistryService;
        this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.sceneSwitcher = sceneSwitcher;
    }

    @FXML
    private Label lblSell, txtClientName, lblUser, lblProducts, lblCompany, lblDateLicense;


    /**
     * Close session.
     */
    @FXML
    void closeSession() {
        clientService.updateIsClientActiveByClientName(false, client.getClientName());
        //Aqui básicamente cerraría todas las ventanas y volvería a iniciar la aplicación
        Main.launch();
    }

    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        initAllLabels();
    }

    private void initAllLabels() {
        client = clientService.getClientByName(userLogged.getName());
        lblUser.setText(client.getClientName());
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
                productCounter += inv.getAmount();
            }
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }

        txtClientName.setText("Usuario: " + client.getClientName());
        lblCompany.setText("Compañía: " + clientService.getByIsClientActive(true).getClientCompany());
        lblProducts.setText("Productos: " + productCounter);
        lblSell.setText("Ventas: " + sellCounter);
        lblDateLicense.setText("Fecha Vencimiento Licencia: " + LocalDate.now().until(licenseValidator.getRemainingTime()).getDays() + " Días");
    }

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

    // ============================
    // MÉTODOS DE IMPORTACIÓN/EXPORTACIÓN
    // ============================

    @FXML
    public void ImportDatabase(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de respaldo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar importación");
            confirmAlert.setHeaderText("¿Está seguro de importar los datos?");
            confirmAlert.setContentText("Esta acción sobrescribirá los datos actuales");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    importDataFromCSV(file);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Importación exitosa");
                    successAlert.setHeaderText("Datos importados correctamente");
                    successAlert.showAndWait();

                    // Actualizar la interfaz después de importar
                    initAllLabels();
                } catch (Exception e) {
                    showErrorAlert("Error al importar datos: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void exportDatabase(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar respaldo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("backup_" + LocalDate.now() + ".csv");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportDataToCSV(file);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Exportación exitosa");
                successAlert.setHeaderText("Datos exportados correctamente");
                successAlert.setContentText("Ubicación: " + file.getAbsolutePath());
                successAlert.showAndWait();
            } catch (Exception e) {
                showErrorAlert("Error al exportar datos: " + e.getMessage());
            }
        }
    }

    private void exportDataToCSV(File file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            // Escribir encabezados
            writer.write("Tipo,ID,Nombre,Cantidad,Precio,Fecha");
            writer.newLine();

            // Exportar inventarios
            /*List<Inventory> inventories = inventoryService.getAllInventoriesByClient(client);
            for (Inventory inv : inventories) {
                writer.write(String.format("INVENTORY,%d,%s,%d,%.2f,%s",
                        inv.getId(),
                        escapeCsv(inv.getProduct().getProductName()),
                        inv.getAmount(),
                        inv.getWarehouse().getWarehouseName(),
                        inv.get()));
                writer.newLine();
            }
*/
            // Exportar otras entidades según sea necesario...
            // Ejemplo para ventas:
            /*
            List<SellRegistry> sales = sellRegistryService.getAllSellRegistriesByClient(client);
            for (SellRegistry sale : sales) {
                writer.write(String.format("SALE,%d,%s,%d,%.2f,%s",
                        sale.getId(),
                        escapeCsv(sale.getProductName()),
                        sale.getProductAmount(),
                        sale.getSellPrice(),
                        sale.getSellDate()));
                writer.newLine();
            }
            */
        }
    }

    private void importDataFromCSV(File file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Saltar encabezados
                }

                String[] values = parseCsvLine(line);
                if (values.length < 6) continue;

            /* String type = values[0];
                switch (type) {
                    case "INVENTORY":
                        Inventory inv = new Inventory();
                        inv.setId(Long.parseLong(values[1]));
                        inv.setProductName(unescapeCsv(values[2]));
                        inv.setAmount(Integer.parseInt(values[3]));
                        inv.setPrice(Double.parseDouble(values[4]));
                        inv.setDateAdded(LocalDate.parse(values[5]));
                        // inventoryService.save(inv);
                        break;

                    // Manejar otros tipos...
                }*/
            }
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        // Implementación simple de parser CSV
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
