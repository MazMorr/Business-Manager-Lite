package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.BuyRegistry;
import com.marcosoft.storageSoftware.domain.model.Client;

import java.util.List;

public interface BuyRegistryService {
    BuyRegistry save(BuyRegistry buyRegistry);
    List<BuyRegistry> getAllBuyRegistriesByClient(Client client);
}
