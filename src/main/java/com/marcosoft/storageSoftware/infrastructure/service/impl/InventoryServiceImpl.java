package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.domain.repository.InventoryRepository;
import com.marcosoft.storageSoftware.domain.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
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
        return (List<Inventory>) inventoryRepository.findAllInventoriesByWarehouseAndClient(warehouse, client);
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
        return (List<Inventory>) inventoryRepository.findAllInventoriesByClient(client);
    }


}
