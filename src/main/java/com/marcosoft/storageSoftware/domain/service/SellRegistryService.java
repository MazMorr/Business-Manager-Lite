package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;

import java.util.List;

public interface SellRegistryService {
    SellRegistry save(SellRegistry sellRegistry);
    List<SellRegistry> getAllSellRegistriesByClient(Client client);
}
