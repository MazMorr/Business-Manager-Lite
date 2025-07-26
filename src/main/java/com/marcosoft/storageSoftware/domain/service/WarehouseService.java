package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Warehouse;

import java.util.List;

public interface WarehouseService {
    Warehouse save(Warehouse warehouse);

    Warehouse getWarehouseById(Long id);

    List<Warehouse> getAllWarehouses();

    void deleteWarehouseById(Long id);

    Warehouse getWarehouseByWarehouseNameAndClient(String warehouseName, Client client);

    boolean existsByWarehouseNameAndClient(String warehouseName, Client client);

    List<Warehouse> getWarehousesByClient(Client client);

    List<Warehouse> getAllWarehousesByClient(Client client);
}
