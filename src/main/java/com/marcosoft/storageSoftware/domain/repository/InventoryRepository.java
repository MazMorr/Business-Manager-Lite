package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {

    List<Inventory> findAllInventoriesByClient(Client client);

    List<Inventory> findAllInventoriesByWarehouseAndClient(Warehouse warehouse, Client client);

    Inventory findByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client);

    boolean existsByProductAndWarehouseAndClient(Product product, Warehouse warehouse, Client client);

    List<Inventory> findByProductAndClient(Product product, Client client);

    List<Inventory> findByBuyId(Long buyId);

    Inventory findByProduct_ProductNameAndWarehouseAndClient(String productName, Warehouse warehouse, Client client);

}