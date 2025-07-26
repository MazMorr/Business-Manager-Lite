package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.InvestmentRegistry;
import com.marcosoft.storageSoftware.domain.repository.InvestmentRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.InvestmentRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class InvestmentRegistryServiceImpl implements InvestmentRegistryService {

    InvestmentRegistryRepository investmentRegistryRepository;

    @Lazy
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
