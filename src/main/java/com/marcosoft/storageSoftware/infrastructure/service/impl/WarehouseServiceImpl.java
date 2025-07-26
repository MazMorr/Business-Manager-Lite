package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Warehouse;
import com.marcosoft.storageSoftware.domain.repository.WarehouseRepository;
import com.marcosoft.storageSoftware.domain.service.WarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class WarehouseServiceImpl implements WarehouseService {
    WarehouseRepository warehouseRepository;

    @Lazy
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

    @Override
    public Warehouse getWarehouseByWarehouseNameAndClient(String warehouseName, Client client) {
        return warehouseRepository.findByWarehouseNameAndClient(warehouseName,client);
    }

    @Override
    public boolean existsByWarehouseNameAndClient(String warehouseName, Client client) {
        return warehouseRepository.existsByWarehouseNameAndClient(warehouseName, client);
    }

    @Override
    public List<Warehouse> getWarehousesByClient(Client client) {
        return warehouseRepository.findAllByClient(client);
    }

    @Override
    public List<Warehouse> getAllWarehousesByClient(Client client) {
        return (List<Warehouse>) warehouseRepository.findAllWarehousesByClient(client);
    }
}
