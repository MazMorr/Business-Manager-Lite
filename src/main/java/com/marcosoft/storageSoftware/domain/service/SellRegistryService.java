package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;

import java.util.List;

public interface SellRegistryService {
    SellRegistry save(SellRegistry sellRegistry);

    boolean existsByIdAndClient(Long id, Client client);

    List<SellRegistry> getAllSellRegistriesByClient(Client client);

    SellRegistry getByIdAndClient(Long id, Client client);
}
