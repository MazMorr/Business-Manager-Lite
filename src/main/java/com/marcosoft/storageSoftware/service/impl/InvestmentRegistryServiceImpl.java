package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.InventoryRegistry;
import com.marcosoft.storageSoftware.domain.InvestmentRegistry;
import com.marcosoft.storageSoftware.repository.InvestmentRegistryRepository;
import com.marcosoft.storageSoftware.service.InventoryRegistryService;
import com.marcosoft.storageSoftware.service.InvestmentRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentRegistryServiceImpl implements InvestmentRegistryService {

    InvestmentRegistryRepository investmentRegistryRepository;

    public InvestmentRegistryServiceImpl(InvestmentRegistryRepository investmentRegistryRepository){
        this.investmentRegistryRepository = investmentRegistryRepository;
    }

    @Override
    public InvestmentRegistry save(InvestmentRegistry investmentRegistry) {
        return investmentRegistryRepository.save(investmentRegistry);
    }

    @Override
    public InvestmentRegistry getInventoryRegistryById(Long id) {
        return investmentRegistryRepository.findById(id).orElse(null);
    }

    @Override
    public List<InvestmentRegistry> getAllInventoryRegistries() {
        return (List<InvestmentRegistry>) investmentRegistryRepository.findAll();
    }

    @Override
    public void deleteInventoryRegistryById(Long id) {
        investmentRegistryRepository.deleteById(id);
    }
}
