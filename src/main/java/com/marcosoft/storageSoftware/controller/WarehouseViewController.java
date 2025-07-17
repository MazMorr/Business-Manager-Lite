package com.marcosoft.storageSoftware.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Controller;

@Controller
public class WarehouseViewController {
    @FXML
    private TableView tvInvestments;
    @FXML
    private Label txtClientName;
    @FXML
    private TreeTableColumn<WarehouseRow, String> ttcWarehouseName, ttcProductName;
    @FXML
    private TreeTableColumn<WarehouseRow, Integer> ttcIdWarehouse, ttcProductAmount;
    @FXML
    private TreeTableView<WarehouseRow> ttvWarehouse;

    @FXML
    private TableColumn tcProductDate, tcProductAmount, tcProductName, tcIdInvestment;

    @FXML
    public void switchToConfiguration(ActionEvent actionEvent) {
        // Cambia a la vista de configuración
    }

    @FXML
    public void switchToInvestment(ActionEvent actionEvent) {
        // Cambia a la vista de inversiones
    }

    @FXML
    public void switchToSupport(ActionEvent actionEvent) {
        // Cambia a la vista de soporte
    }

    @FXML
    public void switchToRegistry(ActionEvent actionEvent) {
        // Cambia a la vista de registros
    }

    @FXML
    public void initialize() {
        Platform.runLater(()->{
            // Ejemplo de cómo inyectar valores en el TreeTableView

            // Configura las columnas
            ttcWarehouseName.setCellValueFactory(new TreeItemPropertyValueFactory<>("warehouseName"));
            ttcIdWarehouse.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
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
        });
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

        public Integer getId() { return id; }
        public String getWarehouseName() { return warehouseName; }
        public String getProductName() { return productName; }
        public Integer getProductAmount() { return productAmount; }
    }
}
