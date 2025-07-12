package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Inventory;
import com.marcosoft.storageSoftware.repository.InventoryRepository;
import com.marcosoft.storageSoftware.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository){
        this.inventoryRepository=inventoryRepository;
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Override
    public List<Inventory> getAllInventories() {
        return (List<Inventory>) inventoryRepository.findAll();
    }

    @Override
    public void deleteInventoryById(Long id) {
        inventoryRepository.deleteById(id);
    }
}
