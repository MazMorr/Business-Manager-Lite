package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;

import java.util.List;

public interface WarehouseRegistryService {
    WarehouseRegistry save(WarehouseRegistry warehouseRegistry);

    WarehouseRegistry getWarehouseRegistryById(Long id);

    List<WarehouseRegistry> getAllWarehouseRegistries();

    void deleteWarehouseRegistryById(Long id);
}
