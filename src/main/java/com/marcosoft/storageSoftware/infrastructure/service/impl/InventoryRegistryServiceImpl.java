package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.InventoryRegistry;
import com.marcosoft.storageSoftware.domain.repository.InventoryRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.InventoryRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class InventoryRegistryServiceImpl implements InventoryRegistryService {

    InventoryRegistryRepository inventoryRegistryRepository;

    @Lazy
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
