package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Warehouse;
import org.springframework.data.repository.CrudRepository;

public interface WarehouseRepository extends CrudRepository<Warehouse, Long> {
}