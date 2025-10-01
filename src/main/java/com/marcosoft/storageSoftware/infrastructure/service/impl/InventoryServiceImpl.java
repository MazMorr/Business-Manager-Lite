package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.domain.repository.InventoryRepository;
import com.marcosoft.storageSoftware.domain.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;

    private final WarehouseServiceImpl warehouseService;
    private final ProductServiceImpl productService;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository, WarehouseServiceImpl warehouseService, ProductServiceImpl productService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Inventory> getAllInventories() {
        return (List<Inventory>) inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteInventoryById(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public List<Inventory> getAllInventoriesByWarehouseAndClient(Warehouse warehouse, Client client) {
        return inventoryRepository.findAllInventoriesByWarehouseAndClient(warehouse, client);
    }

    @Override
    public Inventory getByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client) {
        return inventoryRepository.findByProductAndWarehouseAndClient(product, warehouse, client);
    }

    @Override
    public boolean existsByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client) {
        return inventoryRepository.existsByProductAndWarehouseAndClient(product, warehouse, client);
    }

    @Override
    public List<Inventory> getAllInventoriesByClient(Client client) {
        return inventoryRepository.findAllInventoriesByClient(client);
    }

    @Override
    public List<Inventory> getAllInventoriesByProductAndClient(Product product, Client client) {
        return inventoryRepository.findByProductAndClient(product, client);
    }

    public Inventory getAndValidateInventory(String warehouseName, String productName, int productAmount, Client client) {
        Product product = productService.getByProductNameAndClient(productName, client);
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseNameAndClient(warehouseName, client);
        Inventory inventory = getByProductAndWarehouseAndClient(product, warehouse, client);

        if (inventory == null) {
            throw new IllegalStateException("No existe inventario para este producto y almacén");
        }

        if (inventory.getAmount() < productAmount) {
            throw new IllegalStateException("No hay suficiente stock. Stock actual: " + inventory.getAmount());
        }

        return inventory;
    }

    public List<Inventory> getInventories(Client client) {
        List<Inventory> inventories = getAllInventoriesByClient(client);
        inventories.removeIf(inv -> inv.getAmount() == null || inv.getAmount() == 0);
        return inventories;
    }

    public boolean shouldShowAlert(Inventory inventory) {
        try {
            return inventory != null &&
                    inventory.getAmountAlert() != null &&
                    inventory.getAmount() <= inventory.getAmountAlert();
        } catch (Exception e) {
            log.error("Error checking alert condition", e);
            return false;
        }
    }

    public boolean shouldShowWarning(Inventory inventory) {
        try {
            return inventory != null &&
                    inventory.getAmountWarning() != null &&
                    inventory.getAmount() <= inventory.getAmountWarning() &&
                    inventory.getAmount() > (inventory.getAmountAlert() != null ? inventory.getAmountAlert() : Integer.MIN_VALUE);
        } catch (Exception e) {
            log.error("Error checking warning condition", e);
            return false;
        }
    }

    public List<Inventory> getInventoriesByBuyId(Long buyId) {
        try {
            return inventoryRepository.findByBuyId(buyId);
        } catch (Exception e) {
            log.error("Error getting inventories by buyId: {}", buyId, e);
            return new ArrayList<>();
        }
    }
}
