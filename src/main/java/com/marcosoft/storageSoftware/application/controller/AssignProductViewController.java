package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.service.CurrencyTransaction;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the assign investment view.
 * Handles logic for assigning investments to warehouses and products.
 */
@RequiredArgsConstructor
@Controller
@Slf4j
public class AssignProductViewController {
    private Client client;

    // Service and utility dependencies
    private final InventoryServiceImpl inventoryService;
    private final UserLogged userLogged;
    private final WarehouseServiceImpl warehouseService;
    private final ProductServiceImpl productService;
    private final DisplayAlerts displayAlerts;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final WarehouseRegistryServiceImpl warehouseRegistryService;
    private final WarehouseViewController warehouseViewController;
    private final BuyServiceImpl buyService;
    private final SceneSwitcher sceneSwitcher;
    private final CurrencyServiceImpl currencyService;

    // FXML UI components
    @FXML
    private MenuButton mbWarehouse, mbBuy;
    @FXML
    private TextField tfWarehouse, tfProduct, tfInvestment, tfAmount;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads warehouse and investment menus.
     */
    @FXML
    public void initialize() {
        client = userLogged.getClient();
        Platform.runLater(() -> {
            initTfInvestmentListener();
            initMbWarehouse();
            initMbBuy();
        });
    }

    private void initTfInvestmentListener() {
        tfInvestment.textProperty().addListener((obs, oldVal, newVal) -> searchProduct());
    }

    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfAmount);
    }

    private void clearFields() {
        tfInvestment.clear();
        tfProduct.clear();
        tfWarehouse.clear();
        tfAmount.clear();
    }


    /**
     * Checks if the warehouse exists for the current client.
     */
    private boolean warehouseExistsForClient() {
        return warehouseService.existsByWarehouseNameAndClient(
                tfWarehouse.getText(),
                client
        );
    }

    private void searchProduct() {
        Long buyId = parseDataTypes.parseLong(tfInvestment.getText());
        if (buyId != null && buyService.existsByBuyId(buyId)) {
            Buy buy = buyService.getBuyById(buyId);
            tfProduct.setText(buy.getBuyName());
        } else {
            clearFields();
        }
    }

    @FXML
    public void assignAllProductAmount() {
        if (tfInvestment.getText().isEmpty()) {
            displayAlerts.showAlert("Debe asignar una compra primero");
        } else {
            Long buyId = parseDataTypes.parseLong(tfInvestment.getText());
            if (!buyService.existsByBuyId(buyId)) {
                displayAlerts.showAlert("No se encontró el identificador de la compra en la base de datos");
            } else {
                Buy buy = buyService.getBuyById(buyId);
                if (buy.getLeftAmount() == 0) {
                    displayAlerts.showAlert("Esta compra ha sido completamente asignada, debe seleccionar otra o reasignar los productos");
                } else {
                    tfAmount.setText(String.valueOf(buy.getLeftAmount()));
                }
            }
        }
    }

    private void initMbBuy() {
        mbBuy.getItems().clear();
        // Obtener compras de tipo "Materias Primas y Materiales" con leftAmount > 0
        List<Buy> buys = buyService.getAllBuysGreaterThanZeroByClient(client).stream()
                .filter(buy -> "Materias Primas y Materiales".equals(buy.getBuyType()))
                .toList();

        for (Buy buy : buys) {
            MenuItem item = new MenuItem(String.valueOf(buy.getBuyId()));
            item.setOnAction(e -> {
                tfInvestment.setText(item.getText());
                tfProduct.setText(buy.getBuyName());
            });
            mbBuy.getItems().add(item);
        }
    }

    @FXML
    public void assignProduct() {
        try {
            if (tfInvestment.getText().isEmpty() || tfWarehouse.getText().isEmpty() || tfAmount.getText().isEmpty()) {
                displayAlerts.showAlert("Todos los campos son obligatorios");
                return;
            }

            long buyId = Long.parseLong(tfInvestment.getText());
            int amountToAssign = Integer.parseInt(tfAmount.getText());

            Buy buy = buyService.getBuyById(buyId);

            if (buy == null) {
                displayAlerts.showAlert("No se encontró la compra en la base de datos");
            } else if (buy.getLeftAmount() == 0) {
                displayAlerts.showAlert("Esta compra ha sido completamente asignada");
            } else if (amountToAssign > buy.getLeftAmount()) {
                displayAlerts.showAlert("La cantidad excede el monto disponible de la compra");
            } else if (!warehouseExistsForClient()) {
                displayAlerts.showAlert("No se encuentra el Almacén especificado");
            } else {
                Product product = productService.getByProductNameAndClient(tfProduct.getText(), client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(tfWarehouse.getText(), client);

                // CALCULAR PRECIO PROMEDIO PONDERADO ANTES DE ASIGNAR
                Double newWeightedAveragePrice = calculateWeightedAveragePriceForProduct(product, buy, amountToAssign);
                String currency = buy.getCurrency().getCurrencyName();

                Inventory inventory;
                if (inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
                    inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                    // RECALCULAR PRECIO PROMEDIO PONDERADO CONSIDERANDO EXISTENCIAS + NUEVAS
                    Double currentTotalValue = inventory.getUnitPrice() * inventory.getAmount();
                    Double newTotalValue = newWeightedAveragePrice * amountToAssign;
                    int totalAmount = inventory.getAmount() + amountToAssign;

                    Double weightedAverage = (currentTotalValue + newTotalValue) / totalAmount;

                    inventory.setAmount(totalAmount);
                    inventory.setUnitPrice(weightedAverage);
                    // Mantener la moneda (o convertir si es necesario)
                    inventory.setCurrency(currency);

                } else {
                    // NUEVO INVENTARIO CON PRECIO PROMEDIO
                    inventory = new Inventory(
                            null,
                            product,
                            client,
                            warehouse,
                            amountToAssign,
                            newWeightedAveragePrice,  // ← PRECIO PROMEDIO
                            currency,
                            buy.getBuyId(),
                            null,
                            null
                    );
                }

                inventoryService.save(inventory);

                // ACTUALIZAR leftAmount DE LA COMPRA
                int newLeftAmount = buy.getLeftAmount() - amountToAssign;
                buy.setLeftAmount(newLeftAmount);
                buyService.save(buy);

                LocalDateTime registryMoment = LocalDateTime.now();
                GeneralRegistry generalRegistry = new GeneralRegistry(
                        null, client, "Almacenes", "Asignación de Productos desde Compra", registryMoment
                );
                generalRegistryService.save(generalRegistry);

                WarehouseRegistry warehouseRegistry = new WarehouseRegistry(
                        null, client, "Asignación", registryMoment,
                        warehouse.getWarehouseName(), product.getProductName(), amountToAssign
                );
                warehouseRegistryService.save(warehouseRegistry);


                warehouseViewController.initializeTreeTable();
                warehouseViewController.initializeTableValues();
                clearFields();
                initMbBuy();
                displayAlerts.showAlert("Producto asignado correctamente al almacén");
            }
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("Los campos numéricos deben contener valores válidos");
        } catch (Exception e) {
            displayAlerts.showAlert("Ocurrió un error inesperado: " + e.getMessage());
        }
    }


    private Double calculateWeightedAveragePriceForProduct(Product product, Buy newBuy, int newAmount) {
        try {
            List<Buy> existingBuys = buyService.getAllBuysByClient(client).stream()
                    .filter(b -> b.getBuyName().equals(product.getProductName())).toList();

            List<CurrencyTransaction> transactions = existingBuys.stream()
                    .filter(buy -> buy.getLeftAmount() > 0)
                    .map(buy -> new CurrencyTransaction(
                            buy.getBuyUnitaryPrice(),
                            buy.getCurrency().getCurrencyName(),
                            buy.getLeftAmount().doubleValue()
                    ))
                    .collect(Collectors.toList());

            // Agregar la nueva compra
            transactions.add(new CurrencyTransaction(
                    newBuy.getBuyUnitaryPrice(),
                    newBuy.getCurrency().getCurrencyName(),
                    (double) newAmount
            ));

            return currencyService.calculateWeightedAverage(transactions, "CUP");

        } catch (Exception e) {
            log.error("Error calculando promedio ponderado: {}", e.getMessage());
            return currencyService.convertToCUP(newBuy.getBuyUnitaryPrice(), newBuy.getCurrency().getCurrencyName());
        }
    }

    /**
     * Initializes the warehouse menu with all warehouses for the current client.
     */
    private void initMbWarehouse() {
        mbWarehouse.getItems().clear();
        List<Warehouse> warehouses = warehouseService.getWarehousesByClient(client);

        for (Warehouse w : warehouses) {
            MenuItem item = new MenuItem(w.getWarehouseName());
            item.setOnAction(e -> tfWarehouse.setText(item.getText()));
            mbWarehouse.getItems().add(item);
        }
    }
}
