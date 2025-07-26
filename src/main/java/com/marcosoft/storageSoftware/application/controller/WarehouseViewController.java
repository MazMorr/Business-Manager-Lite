package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.Main;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.application.dto.InvestmentWarehouseDataTable;
import com.marcosoft.storageSoftware.application.dto.UserLogged;
import com.marcosoft.storageSoftware.application.dto.WarehouseDataTable;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Lazy
@Controller
public class WarehouseViewController {
    private static final Logger logger = LoggerFactory.getLogger(WarehouseViewController.class);

    private final WarehouseServiceImpl warehouseService;
    private final UserLogged userLogged;
    private final SceneSwitcher sceneSwitcher;
    private final ParseDataTypes parseDataTypes;
    private final InventoryServiceImpl inventoryService;
    private final ClientServiceImpl clientService;

    @Lazy
    public WarehouseViewController(WarehouseServiceImpl warehouseService, UserLogged userLogged, SceneSwitcher sceneSwitcher, ParseDataTypes parseDataTypes, InventoryServiceImpl inventoryService, ClientServiceImpl clientService) {
        this.clientService = clientService;
        this.inventoryService = inventoryService;
        this.parseDataTypes = parseDataTypes;
        this.sceneSwitcher = sceneSwitcher;
        this.userLogged = userLogged;
        this.warehouseService = warehouseService;
    }

    @FXML
    private TableView<InvestmentWarehouseDataTable> tvInvestments;
    @FXML
    private Label txtClientName;
    @FXML
    private TreeTableView<WarehouseDataTable> ttvWarehouse;
    @FXML
    private TreeTableColumn<WarehouseDataTable, String> ttcWarehouseName, ttcProductName;
    @FXML
    private TreeTableColumn<WarehouseDataTable, Integer> ttcProductAmount;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, Long> tcIdInvestment;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, String> tcProductName;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, Integer> tcProductAmount;
    @FXML
    private TableColumn<InvestmentWarehouseDataTable, LocalDate> tcProductDate;

    @FXML
    public void initialize() {
        txtClientName.setText(userLogged.getName());
        Platform.runLater(() -> {
            initTreeTable();
            initTableLabels();
        });
    }

    private void initTableLabels() {
        // Placeholder para TreeTableView (ttvWarehouse)
        ttvWarehouse.setPlaceholder(new Label("""
                📦 ¡Vaya! No hay almacenes registrados
                Añade tu primer almacén con el botón\s
                '(+) Agregar Almacén'"""));

        // Placeholder para TableView (tvInvestments)
        tvInvestments.setPlaceholder(new Label("""
                💼 Aquí aparecerán tus inversiones
                Cuando registres inversiones de tipo producto
                y estas no hayan sido asignadas, se mostrarán aquí"""));

        // Estilo común para ambos placeholders
        String commonStyle = "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 14px; " +
                "-fx-text-alignment: center; " +
                "-fx-text-fill: #64748b; " +
                "-fx-alignment: center; " +
                "-fx-padding: 10px; ";

        ttvWarehouse.getPlaceholder().setStyle(commonStyle);
        tvInvestments.getPlaceholder().setStyle(commonStyle);
    }

    private void initTreeTable() {
        // Configuración de columnas
        ttcWarehouseName.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));

        List<Inventory> inventory = inventoryService.getAllInventories();

        // Agrupar por almacén
        Map<Warehouse, List<Inventory>> inventoriesByWarehouse = inventory.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouse));

        TreeItem<WarehouseDataTable> root = new TreeItem<>();

        inventoriesByWarehouse.forEach((warehouse, inventories) -> {
            int total = inventories.stream().mapToInt(Inventory::getAmount).sum();
            WarehouseDataTable warehouseNode = new WarehouseDataTable(
                    warehouse.getWarehouseName() + " (Total: " + total + ")",
                    "",
                    total
            );

            TreeItem<WarehouseDataTable> warehouseItem = new TreeItem<>(warehouseNode);

            // Hijos (productos)
            inventories.forEach(inv -> {
                WarehouseDataTable productNode = new WarehouseDataTable(
                        "",
                        inv.getProduct().getProductName(),
                        inv.getAmount()
                );
                warehouseItem.getChildren().add(new TreeItem<>(productNode));
            });

            root.getChildren().add(warehouseItem);
        });

        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false); // Así el placeholder funciona correctamente
    }

    @FXML
    public void reassignProduct(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/reassignProductView.fxml"),
                "Sistema de cuentas",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.show();
    }

    @FXML
    public void addWarehouse(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/addWarehouseView.fxml"),
                "Añadir Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    private Stage createStage(Parent root, String title, String iconPath) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResource(iconPath).toString()));
        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }

    @FXML
    public void deleteWarehouse(ActionEvent actionEvent) {
        WarehouseDataTable w = ttvWarehouse.getSelectionModel().getSelectedItem().getValue();
        Client c = clientService.getClientByName(userLogged.getName());
        if (showConfirmationAlert("Está seguro de querer eliminar el almacén seleccionado:\n" +
                w.getWarehouseName() + " junto a todos los productos almacenados en él?")) {
            warehouseService.deleteWarehouseById(warehouseService.getWarehouseByWarehouseNameAndClient(w.getWarehouseName(), c).getId());
            ttvWarehouse.getSelectionModel().clearSelection();
        } else {
            ttvWarehouse.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void assignInvestment(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/assignInvestmentView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    @FXML
    public void checkInvestment(ActionEvent actionEvent) {
        showAlert("Próximamente");
    }

    @FXML
    public void updateWarehouse(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/updateWarehouseView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    @FXML
    public void changeProductName(ActionEvent actionEvent) throws IOException {
        ConfigurableApplicationContext context = Main.getContext();
        Stage stage = createStage(
                context.getBean(SpringFXMLLoader.class).load("/changeProductNameView.fxml"),
                "Asignar Inversión a un Almacén",
                "/images/RTS_logo.png"
        );
        stage.setOnCloseRequest(event -> {
        });
        stage.showAndWait();
    }

    // ============================
    // MÉTODOS DE NAVEGACIÓN
    // ============================
    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        switchView(actionEvent, "/configurationView.fxml");
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/supportView.fxml");
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        switchView(actionEvent, "/registryView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        switchView(actionEvent, "/investmentView.fxml");
    }

    @FXML
    public void switchToSell(ActionEvent actionEvent) {
        switchView(actionEvent, "/sellView.fxml");
    }

    private void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            sceneSwitcher.setRootWithEvent(actionEvent, fxmlPath);
        } catch (Exception e) {
            logger.error("Error al cambiar de vista", e);
            showAlert("Error al cambiar de vista: " + e.getMessage());
        }
    }

    // ============================
    // UTILITIES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("¿Está seguro?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


}
