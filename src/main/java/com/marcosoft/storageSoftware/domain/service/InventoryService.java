package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;

import java.util.List;

public interface InventoryService {
    Inventory save(Inventory inventory);

    Inventory getInventoryById(Long id);

    List<Inventory> getAllInventories();

    void deleteInventoryById(Long id);

    List<Inventory> getAllInventoriesByWarehouseAndClient(Warehouse warehouse, Client client);

    Inventory getByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client);

    boolean existsByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client);

    List<Inventory> getAllInventoriesByClient(Client client);
}
