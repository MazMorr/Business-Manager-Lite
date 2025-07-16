package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Client;
import com.marcosoft.storageSoftware.domain.Warehouse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WarehouseRepository extends CrudRepository<Warehouse, Long> {

    Warehouse findByWarehouseNameAndClient(String warehouseName, Client client);

    List<Warehouse> findAllByClient(Client client);
}