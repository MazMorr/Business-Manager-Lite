package com.marcosoft.storageSoftware.controller;

import com.marcosoft.storageSoftware.util.ParseDataTypes;
import com.marcosoft.storageSoftware.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class WarehouseViewController {
    private static final Logger logger = LoggerFactory.getLogger(WarehouseViewController.class);

    @Autowired
    private SceneSwitcher sceneSwitcher;
    @Autowired
    private ParseDataTypes parseDataTypes;

    @FXML
    private TableView tvInvestments;
    @FXML
    private Label txtClientName;
    @FXML
    private TreeTableColumn<WarehouseRow, String> ttcWarehouseName, ttcProductName;
    @FXML
    private TreeTableColumn<WarehouseRow, Integer> ttcProductAmount;
    @FXML
    private TreeTableView<WarehouseRow> ttvWarehouse;

    @FXML
    private TableColumn tcProductDate, tcProductAmount, tcProductName, tcIdInvestment;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initTreeTable();
            initTableLabels();
        });
    }

    private void initTableLabels() {

    }

    private void initTreeTable() {
        // Ejemplo de cómo inyectar valores en el TreeTableView

        // Configura las columnas
        ttcWarehouseName.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
        ttcProductName.setCellValueFactory(new TreeItemPropertyValueFactory<>("productName"));
        ttcProductAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("productAmount"));

        // Crea datos de ejemplo
        WarehouseRow warehouse1 = new WarehouseRow(1, "Almacén Central", "Producto A", 50);
        WarehouseRow warehouse2 = new WarehouseRow(2, "Almacén Secundario", "Producto B", 30);

        TreeItem<WarehouseRow> root = new TreeItem<>(new WarehouseRow(0, "Root", "", 0));
        TreeItem<WarehouseRow> item1 = new TreeItem<>(warehouse1);
        TreeItem<WarehouseRow> item2 = new TreeItem<>(warehouse2);

        root.getChildren().addAll(item1, item2);
        ttvWarehouse.setRoot(root);
        ttvWarehouse.setShowRoot(false);
    }

    @FXML
    public void reassignProduct(ActionEvent actionEvent) {
    }

    @FXML
    public void addWarehouse(ActionEvent actionEvent) {
    }

    @FXML
    public void deleteWarehouse(ActionEvent actionEvent) {
    }

    @FXML
    public void assignInvestment(ActionEvent actionEvent) {
    }

    // Modelo para las filas del TreeTableView
    public static class WarehouseRow {
        private Integer id;
        private String warehouseName;
        private String productName;
        private Integer productAmount;

        public WarehouseRow(Integer id, String warehouseName, String productName, Integer productAmount) {
            this.id = id;
            this.warehouseName = warehouseName;
            this.productName = productName;
            this.productAmount = productAmount;
        }

        public Integer getId() {
            return id;
        }

        public String getWarehouseName() {
            return warehouseName;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getProductAmount() {
            return productAmount;
        }
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
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/warehouseView.fxml");
    }

    @FXML
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/balanceView.fxml");
    }

    @FXML
    public void switchToInventory(ActionEvent actionEvent) {
        switchView(actionEvent, "/sellView.fxml");
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        switchView(actionEvent, "/investmentView.fxml");
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
    // UTILIDADES
    // ============================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
