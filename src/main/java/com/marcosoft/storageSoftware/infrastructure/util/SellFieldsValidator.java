package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.infrastructure.service.impl.CurrencyServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.InventoryServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ProductServiceImpl;
import com.marcosoft.storageSoftware.infrastructure.service.impl.WarehouseServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Lazy
@Component
public class SellFieldsValidator {

    private final DisplayAlerts displayAlerts;
    private final ProductServiceImpl productService;
    private final InventoryServiceImpl inventoryService;
    private final CurrencyServiceImpl currencyService;
    private final WarehouseServiceImpl warehouseService;


    public SellFieldsValidator(
            DisplayAlerts displayAlerts, ProductServiceImpl productService, InventoryServiceImpl inventoryService,
            CurrencyServiceImpl currencyService, WarehouseServiceImpl warehouseService
    ) {
        this.displayAlerts = displayAlerts;
        this.warehouseService = warehouseService;
        this.currencyService = currencyService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    public boolean validateAllAssignPriceFields(String productName, String currencyName, String priceText, Client client) {
        return validateTfAssignProduct(productName, client) && validateTfPriceAssign(priceText) && validateTfAssignCurrency(currencyName);
    }

    public boolean validateTfAssignCurrency(String currencyName) {

        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }

        if (!currencyName.matches("[a-zA-Z]+")) {
            displayAlerts.showAlert("La moneda solo puede contener letras");
            return false;
        }

        return true;
    }

    public boolean validateTfAssignProduct(String productName, Client client) {

        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Debe asignar un producto");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto no existe en la base de datos");
            return false;
        }

        return true;
    }

    public boolean validateTfPriceAssign(String priceText) {


        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("El precio no debe estar vacío");
            return false;
        }

        try {
            // Handle locale-specific decimal separators
            priceText = priceText.replace(",", ".");
            double price = Double.parseDouble(priceText);

            if (price <= 0) {
                displayAlerts.showAlert("El precio debe ser mayor que 0");
                return false;
            }

            // Check decimal places safely
            String[] parts = priceText.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                displayAlerts.showAlert("El precio solo puede tener 2 decimales");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El precio debe ser un número válido");
            return false;
        }
    }

    public boolean validateAllSellFields(
            String productName, String warehouseName, String amountText, LocalDate selectedDate, String currencyName,
            String priceText, Client client
    ) {
        return validateTfSellProduct(productName, warehouseName, client) && validateTfSellAmount(
                amountText, productName, warehouseName, client)
                && validateSellPrice(priceText) && validateSellCurrency(currencyName) && validateDatePicker(selectedDate);
    }

    private boolean validateSellCurrency(String currencyName) {

        if (currencyName == null || currencyName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar una moneda");
            return false;
        }

        if (!currencyService.existsByCurrencyName(currencyName)) {
            displayAlerts.showAlert("La moneda seleccionada no existe en la base de datos");
            return false;
        }

        return true;
    }

    private boolean validateSellPrice(String priceText) {

        if (priceText == null || priceText.isEmpty()) {
            displayAlerts.showAlert("El precio no puede estar vacío");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);

            if (price <= 0) {
                displayAlerts.showAlert("El precio debe ser mayor que cero");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El precio debe ser un número válido");
            return false;
        }
    }

    private boolean validateDatePicker(LocalDate selectedDate) {

        if (selectedDate == null) {
            displayAlerts.showAlert("Por favor selecciona una fecha válida");
            return false;
        }

        if (selectedDate.isAfter(LocalDate.now())) {
            displayAlerts.showAlert("La fecha de venta no puede ser en el futuro");
            return false;
        }

        return true;
    }

    private boolean validateTfSellAmount(String amountText, String productName, String warehouseName, Client client) {

        if (amountText == null || amountText.isEmpty()) {
            displayAlerts.showAlert("La cantidad no pueda estar vacía");
            return false;
        }

        try {
            int amount = Integer.parseInt(amountText);

            if (amount <= 0) {
                displayAlerts.showAlert("La cantidad debe ser mayor que cero");
                return false;
            }

            if (validateTfSellProduct(productName, warehouseName, client) && !warehouseName.isEmpty()) {
                Product product = productService.getByProductNameAndClient(productName, client);
                Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);

                if (inventory.getAmount() < amount) {
                    displayAlerts.showAlert("No hay suficiente existencias disponibles");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }
    }

    private boolean validateTfSellProduct(String productName, String warehouseName, Client client) {

        if (productName == null || productName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un producto");
            return false;
        }

        if (warehouseName == null || warehouseName.isEmpty()) {
            displayAlerts.showAlert("Debe seleccionar un almacén");
            return false;
        }

        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto no existe");
            return false;
        }

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("El almacén no existe");
            return false;
        }

        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("El producto no está disponible en el almacén seleccionado");
            return false;
        }

        return true;
    }

}
