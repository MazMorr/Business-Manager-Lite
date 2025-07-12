package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Inventory;

import java.util.List;

public interface InventoryService {
    Inventory save(Inventory inventory);
    Inventory getInventoryById(Long id);
    List<Inventory> getAllInventories();
    void deleteInventoryById(Long id);
}
