package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Inventory;
import org.springframework.data.repository.CrudRepository;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
}