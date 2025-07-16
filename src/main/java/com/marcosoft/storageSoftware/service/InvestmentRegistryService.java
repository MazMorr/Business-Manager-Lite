package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.InvestmentRegistry;

import java.util.List;

public interface InvestmentRegistryService {
    InvestmentRegistry save(InvestmentRegistry investmentRegistry);

    InvestmentRegistry getInventoryRegistryById(Long id);

    List<InvestmentRegistry> getAllInventoryRegistries();

    void deleteInventoryRegistryById(Long id);
}
