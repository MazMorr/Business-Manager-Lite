package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.InventoryRegistry;
import com.marcosoft.storageSoftware.repository.InventoryRegistryRepository;
import com.marcosoft.storageSoftware.service.InventoryRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryRegistryServiceImpl implements InventoryRegistryService {

    InventoryRegistryRepository inventoryRegistryRepository;

    public InventoryRegistryServiceImpl(InventoryRegistryRepository inventoryRegistryRepository){
        this.inventoryRegistryRepository = inventoryRegistryRepository;
    }

    @Override
    public InventoryRegistry save(InventoryRegistry inventoryRegistry) {
        return inventoryRegistryRepository.save(inventoryRegistry);
    }

    @Override
    public InventoryRegistry getInventoryById(Long id) {
        return inventoryRegistryRepository.findById(id).orElse(null);
    }

    @Override
    public List<InventoryRegistry> getAllInventories() {
        return (List<InventoryRegistry>) inventoryRegistryRepository.findAll();
    }

    @Override
    public void deleteInventoryById(Long id) {
        inventoryRegistryRepository.deleteById(id);
    }
}
