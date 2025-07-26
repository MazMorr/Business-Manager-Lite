package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WarehouseRepository extends CrudRepository<Warehouse, Long> {

    Warehouse findByWarehouseNameAndClient(String warehouseName, Client client);

    List<Warehouse> findAllByClient(Client client);

    boolean existsByWarehouseNameAndClient(String warehouseName, Client client);

    List<Warehouse> findAllWarehousesByClient(Client client);
}