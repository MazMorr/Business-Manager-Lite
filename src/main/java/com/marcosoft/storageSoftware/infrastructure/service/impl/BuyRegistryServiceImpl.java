package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.BuyRegistry;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.repository.BuyRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.BuyRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuyRegistryServiceImpl implements BuyRegistryService {

    private final BuyRegistryRepository buyRegistryRepository;

    public BuyRegistryServiceImpl(BuyRegistryRepository buyRegistryRepository) {
        this.buyRegistryRepository = buyRegistryRepository;
    }

    @Override
    public BuyRegistry save(BuyRegistry buyRegistry) {
        return buyRegistryRepository.save(buyRegistry);
    }

    @Override
    public List<BuyRegistry> getAllBuyRegistriesByClient(Client client) {
        return buyRegistryRepository.findListByClient(client);
    }
}
