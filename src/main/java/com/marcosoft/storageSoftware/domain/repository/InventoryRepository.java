package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {

    List<Inventory> findAllInventoriesByClient(Client client);

    List<Inventory> findAllInventoriesByWarehouseAndClient(Warehouse warehouse, Client client);
}