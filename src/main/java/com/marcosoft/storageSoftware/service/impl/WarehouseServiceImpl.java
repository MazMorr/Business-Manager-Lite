package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Warehouse;
import com.marcosoft.storageSoftware.repository.WarehouseRepository;
import com.marcosoft.storageSoftware.service.WarehouseRegistryService;
import com.marcosoft.storageSoftware.service.WarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {
    WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository){
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Override
    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id).orElse(null);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return (List<Warehouse>) warehouseRepository.findAll();
    }

    @Override
    public void deleteWarehouseById(Long id) {
        warehouseRepository.deleteById(id);
    }
}
