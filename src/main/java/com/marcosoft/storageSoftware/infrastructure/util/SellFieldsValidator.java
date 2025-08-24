package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.infrastructure.service.impl.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Lazy
@Component
public class SellFieldsValidator {

    private final DisplayAlerts displayAlerts;
    private final ProductServiceImpl productService;
    private final InventoryServiceImpl inventoryService;
    private final CurrencyServiceImpl currencyService;
    private final WarehouseServiceImpl warehouseService;
    private final SellRegistryServiceImpl sellRegistryService;

    public SellFieldsValidator(
            DisplayAlerts displayAlerts, ProductServiceImpl productService, InventoryServiceImpl inventoryService,
            CurrencyServiceImpl currencyService, WarehouseServiceImpl warehouseService, SellRegistryServiceImpl sellRegistryService
    ) {
        this.displayAlerts = displayAlerts;
        this.warehouseService = warehouseService;
        this.currencyService = currencyService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.sellRegistryService = sellRegistryService;
    }

    public boolean validateAllSellFieldsForUpdate(
            String productName, String warehouseName, String amountText, LocalDate selectedDate, String currencyName,
            String priceText, Client client) {
        return isNotBlank(productName, "Debe seleccionar un producto")
                && isNotBlank(warehouseName, "Debe seleccionar un almacén")
                && existsProduct(productName, client)
                && existsWarehouse(warehouseName, client)
                && existsInventory(productName, warehouseName, client)
                && isNotBlank(amountText, "La cantidad no puede estar vacía")
                && isValidAmountFormat(amountText)  // Solo valida formato, no existencias
                && isNotBlank(priceText, "El precio no puede estar vacío")
                && isValidPriceFormat(priceText, false)
                && isNotBlank(currencyName, "Debe seleccionar una moneda")
                && existsCurrency(currencyName)
                && isValidDate(selectedDate);
    }

    private boolean isValidAmountFormat(String amountText) {
        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                displayAlerts.showAlert("La cantidad debe ser mayor que cero");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }
    }

    public boolean validateAllAssignPriceFields(String productName, String currencyName, String priceText, Client client) {
        return isNotBlank(productName, "Debe asignar un producto")
                && existsProduct(productName, client)
                && isNotBlank(priceText, "El precio no debe estar vacío")
                && isValidPriceFormat(priceText, true)
                && isNotBlank(currencyName, "Debe seleccionar una moneda")
                && isAlpha(currencyName, "La moneda solo puede contener letras");
    }

    public boolean validateAllSellFields(
            String productName, String warehouseName, String amountText, LocalDate selectedDate, String currencyName,
            String priceText, Client client
    ) {
        return isNotBlank(productName, "Debe seleccionar un producto")
                && isNotBlank(warehouseName, "Debe seleccionar un almacén")
                && existsProduct(productName, client)
                && existsWarehouse(warehouseName, client)
                && existsInventory(productName, warehouseName, client)
                && isNotBlank(amountText, "La cantidad no puede estar vacía")
                && isValidAmount(amountText, productName, warehouseName, client)
                && isNotBlank(priceText, "El precio no puede estar vacío")
                && isValidPriceFormat(priceText, false)
                && isNotBlank(currencyName, "Debe seleccionar una moneda")
                && existsCurrency(currencyName)
                && isValidDate(selectedDate);
    }

    public boolean validateId(Long id, Client client) {
        if (id == null) {
            displayAlerts.showAlert("Debe seleccionar un id para modificar");
            return false;
        }
        List<SellRegistry> sellRegistryList = sellRegistryService.getAllSellRegistriesByClient(client);
        boolean exists = sellRegistryList.stream().anyMatch(sell -> Objects.equals(sell.getId(), id));
        if (!exists) {
            displayAlerts.showAlert("El id seleccionado no existe en los registros de venta");
        }
        return exists;
    }

    // --- Métodos privados de validación reutilizables ---

    private boolean isNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            displayAlerts.showAlert(message);
            return false;
        }
        return true;
    }

    private boolean isAlpha(String value, String message) {
        if (!value.matches("[a-zA-Z]+")) {
            displayAlerts.showAlert(message);
            return false;
        }
        return true;
    }

    private boolean existsProduct(String productName, Client client) {
        Product product = productService.getByProductNameAndClient(productName, client);
        if (product == null) {
            displayAlerts.showAlert("El producto no existe en la base de datos");
            return false;
        }
        return true;
    }

    private boolean existsWarehouse(String warehouseName, Client client) {
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (warehouse == null) {
            displayAlerts.showAlert("El almacén no existe");
            return false;
        }
        return true;
    }

    private boolean existsInventory(String productName, String warehouseName, Client client) {
        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        if (product == null || warehouse == null) return false;
        if (!inventoryService.existsByProductAndWarehouseAndClient(product, warehouse, client)) {
            displayAlerts.showAlert("El producto no está disponible en el almacén seleccionado");
            return false;
        }
        return true;
    }

    private boolean existsCurrency(String currencyName) {
        if (!currencyService.existsByCurrencyName(currencyName)) {
            displayAlerts.showAlert("La moneda seleccionada no existe en la base de datos");
            return false;
        }
        return true;
    }

    private boolean isValidPriceFormat(String priceText, boolean checkDecimals) {
        try {
            String normalized = priceText.replace(",", ".");
            double price = Double.parseDouble(normalized);
            if (price <= 0) {
                displayAlerts.showAlert("El precio debe ser mayor que 0");
                return false;
            }
            if (checkDecimals) {
                String[] parts = normalized.split("\\.");
                if (parts.length > 1 && parts[1].length() > 2) {
                    displayAlerts.showAlert("El precio solo puede tener 2 decimales");
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("El precio debe ser un número válido");
            return false;
        }
    }

    private boolean isValidAmount(String amountText, String productName, String warehouseName, Client client) {
        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                displayAlerts.showAlert("La cantidad debe ser mayor que cero");
                return false;
            }
            Product product = productService.getByProductNameAndClient(productName, client);
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
            if (product != null && warehouse != null) {
                Inventory inventory = inventoryService.getByProductAndWarehouseAndClient(product, warehouse, client);
                if (inventory != null && inventory.getAmount() < amount) {
                    displayAlerts.showAlert("No hay suficientes existencias disponibles");
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            displayAlerts.showAlert("La cantidad debe ser un número válido");
            return false;
        }
    }

    private boolean isValidDate(LocalDate selectedDate) {
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
}
