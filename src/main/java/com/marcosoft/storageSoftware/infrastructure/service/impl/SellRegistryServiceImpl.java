package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import com.marcosoft.storageSoftware.domain.repository.SellRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.SellRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellRegistryServiceImpl implements SellRegistryService {

    private final SellRegistryRepository sellRegistryRepository;

    public SellRegistryServiceImpl(SellRegistryRepository sellRegistryRepository){
        this.sellRegistryRepository = sellRegistryRepository;
    }

    @Override
    public SellRegistry save(SellRegistry sellRegistry) {
        return sellRegistryRepository.save(sellRegistry);
    }

    @Override
    public List<SellRegistry> getAllSellRegistriesByClient(Client client) {
        return (List<SellRegistry>) sellRegistryRepository.findAllSellRegistriesByClient(client);
    }
}
