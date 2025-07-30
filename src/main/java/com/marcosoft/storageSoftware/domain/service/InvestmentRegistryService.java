package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.InvestmentRegistry;

import java.util.List;

public interface InvestmentRegistryService {
    InvestmentRegistry save(InvestmentRegistry investmentRegistry);

    InvestmentRegistry getInventoryRegistryById(Long id);

    List<InvestmentRegistry> getAllInventoryRegistries();

    void deleteInventoryRegistryById(Long id);

    List<InvestmentRegistry> getAllInvestmentRegistryByClient(Client client);
}
