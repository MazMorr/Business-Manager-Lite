package com.marcosoft.storageSoftware.application.controller;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import com.marcosoft.storageSoftware.infrastructure.util.CleanHelper;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import com.marcosoft.storageSoftware.infrastructure.util.SceneSwitcher;
import com.marcosoft.storageSoftware.infrastructure.util.UserLogged;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ProductionViewController {
    // Constants
    private static final String CUP_CURRENCY_NAME = "CUP";
    private static final String PRODUCTION_BUY_TYPE = "Materias Primas y Materiales";
    private static final int MIN_PRODUCT_AMOUNT = 1;

    // Services
    private final SceneSwitcher sceneSwitcher;
    private final DisplayAlerts displayAlerts;
    private final CleanHelper cleanHelper;
    private final WarehouseServiceImpl warehouseService;
    private final UserLogged userLogged;
    private final InventoryServiceImpl inventoryService;
    private final ProductServiceImpl productService;
    private final BuyServiceImpl buyService;
    private final CurrencyServiceImpl currencyService;
    private final WarehouseViewController warehouseViewController;

    @FXML
    private TextField tfWarehouse1, tfWarehouse2, tfWarehouse3, tfWarehouse4, tfNewProductDestinyWarehouse,
            tfProduct1, tfProduct2, tfProduct3, tfProduct4, tfNewProductName,
            tfAmount1, tfAmount2, tfAmount3, tfAmount4, tfNewProductAmount;
    @FXML
    private MenuButton mbWarehouse1, mbWarehouse2, mbWarehouse3, mbWarehouse4, mbNewProductDestinyWarehouse,
            mbProduct1, mbProduct2, mbProduct3, mbProduct4,
            mbAmount1, mbAmount2, mbAmount3, mbAmount4;

    @FXML
    private CheckBox cbReady1, cbReady2, cbReady3, cbReady4;

    // State
    private Client client;
    private Currency cupCurrency;

    @FXML
    private void initialize() {
        client = userLogged.getClient();
        cupCurrency = currencyService.getCurrencyByName(CUP_CURRENCY_NAME);

        Platform.runLater(() -> {
            setupTextFieldListeners();
            initializeWarehouseMenus();
            initializeAmountMenus();
        });
    }

    @FXML
    public void produce() {
        if (!validateNewProductFields()) {
            return;
        }

        List<ProductionIngredient> readyIngredients = getReadyIngredients();
        if (readyIngredients.isEmpty()) {
            displayAlerts.showAlert("Debe tener al menos un producto listo para producir");
            return;
        }

        if (!validateInventoryAvailability(readyIngredients)) {
            return;
        }

        try {
            createNewProductAndBuy(readyIngredients);
            warehouseViewController.initialize();
            displayAlerts.showAlert("Producción completada exitosamente");
            cleanAllFields();
        } catch (Exception e) {
            displayAlerts.showError("Error durante la producción: " + e.getMessage());
        }
    }

    private boolean validateNewProductFields() {
        String productName = tfNewProductName.getText().trim();
        String amountText = tfNewProductAmount.getText().trim();
        String warehouseName = tfNewProductDestinyWarehouse.getText().trim();

        if (productName.isEmpty()) {
            displayAlerts.showAlert("El nombre del nuevo producto es requerido");
            return false;
        }

        if (amountText.isEmpty()) {
            displayAlerts.showAlert("La cantidad del nuevo producto es requerida");
            return false;
        }

        try {
            int amount = Integer.parseInt(amountText);
            if (amount < MIN_PRODUCT_AMOUNT) {
                displayAlerts.showAlert("La cantidad debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }

        if (warehouseName.isEmpty()) {
            displayAlerts.showAlert("El almacén destino es requerido");
            return false;
        }

        return true;
    }

    private List<ProductionIngredient> getReadyIngredients() {
        List<ProductionIngredient> readyIngredients = new ArrayList<>();

        addIngredientIfReady(readyIngredients, tfWarehouse1, tfProduct1, tfAmount1, cbReady1);
        addIngredientIfReady(readyIngredients, tfWarehouse2, tfProduct2, tfAmount2, cbReady2);
        addIngredientIfReady(readyIngredients, tfWarehouse3, tfProduct3, tfAmount3, cbReady3);
        addIngredientIfReady(readyIngredients, tfWarehouse4, tfProduct4, tfAmount4, cbReady4);

        return readyIngredients;
    }

    private void addIngredientIfReady(List<ProductionIngredient> ingredients,
                                      TextField warehouse, TextField product,
                                      TextField amount, CheckBox ready) {
        if (ready.isSelected()) {
            ingredients.add(createIngredient(warehouse, product, amount));
        }
    }

    private ProductionIngredient createIngredient(TextField warehouse, TextField product, TextField amount) {
        ProductionIngredient ingredient = new ProductionIngredient();
        ingredient.setWarehouseName(warehouse.getText().trim());
        ingredient.setProductName(product.getText().trim());
        ingredient.setAmount(Integer.parseInt(amount.getText().trim()));
        return ingredient;
    }

    private boolean validateInventoryAvailability(List<ProductionIngredient> ingredients) {
        // Agrupar requerimientos por producto y almacén
        Map<String, Map<String, Integer>> requiredAmounts = new HashMap<>();

        for (ProductionIngredient ingredient : ingredients) {
            String productKey = ingredient.getProductName();
            String warehouseKey = ingredient.getWarehouseName();

            requiredAmounts
                    .computeIfAbsent(productKey, k -> new HashMap<>())
                    .merge(warehouseKey, ingredient.getAmount(), Integer::sum);
        }

        // Validar cada grupo acumulado
        for (Map.Entry<String, Map<String, Integer>> productEntry : requiredAmounts.entrySet()) {
            String productName = productEntry.getKey();

            for (Map.Entry<String, Integer> warehouseEntry : productEntry.getValue().entrySet()) {
                String warehouseName = warehouseEntry.getKey();
                int totalRequired = warehouseEntry.getValue();

                Product product = productService.getByProductNameAndClient(productName, client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);

                if (product == null || warehouse == null) {
                    displayAlerts.showAlert("Producto o almacén no encontrado: " + productName);
                    return false;
                }

                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
                if (inventory == null || inventory.getAmount() < totalRequired) {
                    displayAlerts.showAlert(
                            String.format("Stock insuficiente para: %s en almacén %s. Disponible: %d, Requerido total: %d",
                                    productName,
                                    warehouseName,
                                    inventory != null ? inventory.getAmount() : 0,
                                    totalRequired));
                    return false;
                }
            }
        }

        return true;
    }

    private void createNewProductAndBuy(List<ProductionIngredient> ingredients) {
        double totalCost = calculateProductionCostInCup(ingredients);
        int newProductAmount = Integer.parseInt(tfNewProductAmount.getText().trim());
        double unitCost = totalCost / newProductAmount;

        Warehouse destinyWarehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                tfNewProductDestinyWarehouse.getText().trim(), client);

        if (destinyWarehouse == null) {
            throw new RuntimeException("Almacén destino no encontrado");
        }

        Product newProduct = createOrUpdateNewProduct(unitCost);
        Buy productionBuy = createProductionBuy(newProduct, unitCost, totalCost, newProductAmount);
        updateInventories(ingredients, newProduct, destinyWarehouse, newProductAmount, productionBuy);
    }

    private Product createOrUpdateNewProduct(double unitCost) {
        String productName = tfNewProductName.getText().trim();
        Product newProduct = new Product();
        if (productService.existsByProductNameAndClient(productName, client)) {
            newProduct = productService.getByProductNameAndClient(productName, client);
        } else {
            newProduct.setProductName(productName);
            newProduct.setClient(client);
            newProduct.setSellPrice(unitCost);
            newProduct.setCurrency(cupCurrency);
        }
        return productService.save(newProduct);
    }

    private Buy createProductionBuy(Product newProduct, double unitCost, double totalCost, int amount) {
        Buy buy = new Buy();
        buy.setBuyName(newProduct.getProductName());
        buy.setBuyUnitaryPrice(unitCost);
        buy.setBuyTotalPrice(totalCost);
        buy.setAmount(amount);
        buy.setLeftAmount(0);
        buy.setReceivedDate(LocalDate.now());
        buy.setBuyType(PRODUCTION_BUY_TYPE);
        buy.setClient(client);
        buy.setCurrency(cupCurrency);
        return buyService.save(buy);
    }

    private double calculateProductionCostInCup(List<ProductionIngredient> ingredients) {
        return ingredients.stream()
                .mapToDouble(this::calculateIngredientCostInCup)
                .sum();
    }

    private double calculateIngredientCostInCup(ProductionIngredient ingredient) {
        Product product = productService.getByProductNameAndClient(ingredient.getProductName(), client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                ingredient.getWarehouseName(), client);

        Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
        if (inventory == null || inventory.getUnitPrice() == null) {
            throw new RuntimeException("No se pudo obtener el precio unitario para: " + ingredient.getProductName());
        }

        double ingredientCost = inventory.getUnitPrice() * ingredient.getAmount();
        return currencyService.convertToCUP(ingredientCost, inventory.getCurrency());
    }

    private void updateInventories(List<ProductionIngredient> ingredients, Product newProduct,
                                   Warehouse destinyWarehouse, int newProductAmount, Buy productionBuy) {
        // Reduce raw material inventories
        ingredients.forEach(this::reduceIngredientInventory);

        // Update finished product inventory
        updateFinishedProductInventory(newProduct, destinyWarehouse, newProductAmount, productionBuy);
    }

    private void reduceIngredientInventory(ProductionIngredient ingredient) {
        Product product = productService.getByProductNameAndClient(ingredient.getProductName(), client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(
                ingredient.getWarehouseName(), client);

        Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
        if (inventory.getAmount() < ingredient.getAmount()) {
            throw new RuntimeException("Stock insuficiente para: " + ingredient.getProductName());
        }

        int newAmount = inventory.getAmount() - ingredient.getAmount();

        if (newAmount == 0) {
            // Eliminar el registro de inventario si la cantidad llega a 0
            inventoryService.deleteInventoryById(inventory.getId());
        } else {
            // Actualizar la cantidad si todavía hay stock
            inventory.setAmount(newAmount);
            inventoryService.save(inventory);
        }
    }

    private void updateFinishedProductInventory(Product newProduct, Warehouse destinyWarehouse,
                                                int newProductAmount, Buy productionBuy) {
        Inventory existingInventory = inventoryService.getByProductAndWarehouseAndClient(
                newProduct, destinyWarehouse, client);

        if (existingInventory != null) {
            updateExistingInventory(existingInventory, newProductAmount, productionBuy);
        } else {
            createNewInventory(newProduct, destinyWarehouse, newProductAmount, productionBuy);
        }
        buyService.deleteByBuyId(productionBuy.getBuyId());
    }

    private void updateExistingInventory(Inventory inventory, int newAmount, Buy productionBuy) {
        double currentTotalValue = inventory.getUnitPrice() * inventory.getAmount();
        double newTotalValue = productionBuy.getBuyUnitaryPrice() * newAmount;
        int totalAmount = inventory.getAmount() + newAmount;

        double weightedAverageCost = (currentTotalValue + newTotalValue) / totalAmount;

        inventory.setAmount(totalAmount);
        inventory.setUnitPrice(weightedAverageCost);
        inventory.setCurrency(CUP_CURRENCY_NAME);
        inventory.setBuyId(productionBuy.getBuyId());

        inventoryService.save(inventory);
    }

    private void createNewInventory(Product product, Warehouse warehouse,
                                    int amount, Buy productionBuy) {
        Inventory newInventory = new Inventory();
        newInventory.setProduct(product);
        newInventory.setWarehouse(warehouse);
        newInventory.setClient(client);
        newInventory.setAmount(amount);
        newInventory.setUnitPrice(productionBuy.getBuyUnitaryPrice());
        newInventory.setCurrency(CUP_CURRENCY_NAME);
        newInventory.setBuyId(productionBuy.getBuyId());

        inventoryService.save(newInventory);
    }

    // UI Helper Methods
    public void assignAllProductAmount(TextField tfProduct, TextField tfWarehouse, TextField tfAmount) {
        String productName = tfProduct.getText().trim();
        String warehouseName = tfWarehouse.getText().trim();

        if (productName.isEmpty() || warehouseName.isEmpty()) {
            displayAlerts.showAlert("Debe asignar un almacén y un producto primero");
            return;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);

        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("No se encontró el respectivo producto dentro del almacén que proporcionó");
        } else {
            Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
            if (inventory.getAmount() == 0) {
                displayAlerts.showAlert("No hay stock disponible de este producto en el almacén seleccionado");
            } else {
                tfAmount.setText(String.valueOf(inventory.getAmount()));
            }
        }
    }

    private void initializeWarehouseMenus() {
        initWarehouseMenu(mbWarehouse1, tfWarehouse1, mbProduct1, tfProduct1);
        initWarehouseMenu(mbWarehouse2, tfWarehouse2, mbProduct2, tfProduct2);
        initWarehouseMenu(mbWarehouse3, tfWarehouse3, mbProduct3, tfProduct3);
        initWarehouseMenu(mbWarehouse4, tfWarehouse4, mbProduct4, tfProduct4);
        initDestinyWarehouseMenu();
    }

    private void initializeAmountMenus() {
        initAmountMenu(mbAmount1, tfProduct1, tfWarehouse1, tfAmount1);
        initAmountMenu(mbAmount2, tfProduct2, tfWarehouse2, tfAmount2);
        initAmountMenu(mbAmount3, tfProduct3, tfWarehouse3, tfAmount3);
        initAmountMenu(mbAmount4, tfProduct4, tfWarehouse4, tfAmount4);
    }

    private void initWarehouseMenu(MenuButton mbWarehouse, TextField tfWarehouse,
                                   MenuButton mbProduct, TextField tfProduct) {
        mbWarehouse.getItems().clear();
        warehouseService.getWarehousesByClient(client).forEach(warehouse -> {
            MenuItem item = new MenuItem(warehouse.getWarehouseName());
            item.setOnAction(e -> {
                tfWarehouse.setText(warehouse.getWarehouseName());
                initProductMenu(warehouse, mbProduct, tfProduct);
            });
            mbWarehouse.getItems().add(item);
        });
    }

    private void initDestinyWarehouseMenu() {
        mbNewProductDestinyWarehouse.getItems().clear();
        warehouseService.getWarehousesByClient(client).forEach(warehouse -> {
            MenuItem item = new MenuItem(warehouse.getWarehouseName());
            item.setOnAction(e -> tfNewProductDestinyWarehouse.setText(warehouse.getWarehouseName()));
            mbNewProductDestinyWarehouse.getItems().add(item);
        });
    }

    private void initProductMenu(Warehouse warehouse, MenuButton mbProduct, TextField tfProduct) {
        mbProduct.getItems().clear();
        if (warehouse == null) return;

        List<Inventory> inventories = inventoryService.getAllInventoriesByWarehouseAndClient(warehouse, client);

        if (inventories.isEmpty()) {
            addDisabledMenuItem(mbProduct, "No hay productos disponibles");
            return;
        }

        inventories.stream()
                .filter(this::isValidInventory)
                .forEach(inventory -> {
                    MenuItem item = new MenuItem(
                            String.format("%s (Stock: %d)",
                                    inventory.getProduct().getProductName(),
                                    inventory.getAmount())
                    );
                    item.setOnAction(e -> tfProduct.setText(inventory.getProduct().getProductName()));
                    mbProduct.getItems().add(item);
                });
    }

    private boolean isValidInventory(Inventory inventory) {
        return inventory.getProduct() != null &&
                inventory.getAmount() != null &&
                inventory.getAmount() > 0;
    }

    private void initAmountMenu(MenuButton mbAmount, TextField tfProduct,
                                TextField tfWarehouse, TextField tfAmount) {
        mbAmount.getItems().clear();
        MenuItem item = new MenuItem("Todos");
        item.setOnAction(e -> assignAllProductAmount(tfProduct, tfWarehouse, tfAmount));
        mbAmount.getItems().add(item);
    }

    private void addDisabledMenuItem(MenuButton menuButton, String text) {
        MenuItem item = new MenuItem(text);
        item.setDisable(true);
        menuButton.getItems().add(item);
    }

    private void setupTextFieldListeners() {
        setupIngredientListeners(tfWarehouse1, tfProduct1, tfAmount1, cbReady1);
        setupIngredientListeners(tfWarehouse2, tfProduct2, tfAmount2, cbReady2);
        setupIngredientListeners(tfWarehouse3, tfProduct3, tfAmount3, cbReady3);
        setupIngredientListeners(tfWarehouse4, tfProduct4, tfAmount4, cbReady4);
    }

    private void setupIngredientListeners(TextField warehouse, TextField product,
                                          TextField amount, CheckBox ready) {
        warehouse.textProperty().addListener((obs, oldVal, newVal) -> validateIngredient(warehouse, product, amount, ready));
        product.textProperty().addListener((obs, oldVal, newVal) -> validateIngredient(warehouse, product, amount, ready));
        amount.textProperty().addListener((obs, oldVal, newVal) -> validateIngredient(warehouse, product, amount, ready));
    }

    private void validateIngredient(TextField warehouse, TextField product,
                                    TextField amount, CheckBox ready) {
        boolean isValid = !warehouse.getText().trim().isEmpty() &&
                !product.getText().trim().isEmpty() &&
                !amount.getText().trim().isEmpty() &&
                isValidAmount(amount.getText().trim());

        ready.setSelected(isValid);
    }

    private boolean isValidAmount(String amountText) {
        try {
            Integer.parseInt(amountText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Navigation and Cleanup methods
    @FXML
    public void goOut() {
        sceneSwitcher.closeWindow(tfAmount1);
    }

    @FXML
    public void cleanProduct1() {
        cleanIngredient(tfWarehouse1, tfProduct1, tfAmount1, cbReady1);
    }

    @FXML
    public void cleanProduct2() {
        cleanIngredient(tfWarehouse2, tfProduct2, tfAmount2, cbReady2);
    }

    @FXML
    public void cleanProduct3() {
        cleanIngredient(tfWarehouse3, tfProduct3, tfAmount3, cbReady3);
    }

    @FXML
    public void cleanProduct4() {
        cleanIngredient(tfWarehouse4, tfProduct4, tfAmount4, cbReady4);
    }

    @FXML
    public void cleanNewProduct() {
        cleanHelper.cleanTextFields(List.of(tfNewProductName, tfNewProductAmount, tfNewProductDestinyWarehouse));
    }

    private void cleanAllFields() {
        cleanProduct1();
        cleanProduct2();
        cleanProduct3();
        cleanProduct4();
        cleanNewProduct();
    }

    private void cleanIngredient(TextField warehouse, TextField product,
                                 TextField amount, CheckBox ready) {
        cleanHelper.cleanTextFields(List.of(warehouse, product, amount));
        ready.setSelected(false);
    }

    @Setter
    @Getter
    private static class ProductionIngredient {
        private String warehouseName;
        private String productName;
        private int amount;
    }
}