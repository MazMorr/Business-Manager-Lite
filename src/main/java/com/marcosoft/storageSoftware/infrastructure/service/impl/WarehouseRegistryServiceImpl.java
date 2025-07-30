package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.WarehouseRegistry;
import com.marcosoft.storageSoftware.domain.repository.WarehouseRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.WarehouseRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class WarehouseRegistryServiceImpl implements WarehouseRegistryService {

    WarehouseRegistryRepository warehouseRegistryRepository;

    @Lazy
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
    public List<WarehouseRegistry> getAllWarehouseRegistriesByClient(Client client) {
        return (List<WarehouseRegistry>) warehouseRegistryRepository.findAllWarehouseRegistriesByClient(client);
    }

    @Override
    public void deleteWarehouseRegistryById(Long id) {
        warehouseRegistryRepository.deleteById(id);
    }
}
