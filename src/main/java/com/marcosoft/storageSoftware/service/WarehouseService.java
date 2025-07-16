package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Client;
import com.marcosoft.storageSoftware.domain.Warehouse;

import java.util.List;

public interface WarehouseService {
    Warehouse save(Warehouse warehouse);

    Warehouse getWarehouseById(Long id);

    List<Warehouse> getAllWarehouses();

    void deleteWarehouseById(Long id);

    Warehouse getWarehouseByWarehouseNameAndClient(String warehouseName, Client client);

    List<Warehouse> getWarehousesByClient(Client client);
}
