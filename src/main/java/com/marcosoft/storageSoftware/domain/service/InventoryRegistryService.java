package com.marcosoft.storageSoftware.domain.service;


import com.marcosoft.storageSoftware.domain.model.InventoryRegistry;

import java.util.List;

public interface InventoryRegistryService {
    InventoryRegistry save(InventoryRegistry inventoryRegistry);

    InventoryRegistry getInventoryById(Long id);

    List<InventoryRegistry> getAllInventories();

    void deleteInventoryById(Long id);
}
