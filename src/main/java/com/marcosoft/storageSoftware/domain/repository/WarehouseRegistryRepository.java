package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WarehouseRegistryRepository extends CrudRepository<WarehouseRegistry, Long> {
    List<WarehouseRegistry> findAllWarehouseRegistriesByClient(Client client);
}