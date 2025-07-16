package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.WarehouseRegistry;
import com.marcosoft.storageSoftware.repository.WarehouseRegistryRepository;
import com.marcosoft.storageSoftware.service.WarehouseRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseRegistryServiceImpl implements WarehouseRegistryService {

    WarehouseRegistryRepository warehouseRegistryRepository;

    public  WarehouseRegistryServiceImpl(WarehouseRegistryRepository warehouseRegistryRepository){
        this.warehouseRegistryRepository = warehouseRegistryRepository;
    }

    @Override
    public WarehouseRegistry save(WarehouseRegistry warehouseRegistry) {
        return warehouseRegistryRepository.save(warehouseRegistry);
    }

    @Override
    public WarehouseRegistry getWarehouseRegistryById(Long id) {
        return warehouseRegistryRepository.findById(id).orElse(null);
    }

    @Override
    public List<WarehouseRegistry> getAllWarehouseRegistries() {
        return (List<WarehouseRegistry>) warehouseRegistryRepository.findAll();
    }

    @Override
    public void deleteWarehouseRegistryById(Long id) {
        warehouseRegistryRepository.deleteById(id);
    }
}
