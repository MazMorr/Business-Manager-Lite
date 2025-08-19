package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.ExpenseRegistry;
import com.marcosoft.storageSoftware.domain.repository.ExpenseRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.ExpenseRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class ExpenseRegistryServiceImpl implements ExpenseRegistryService {

    ExpenseRegistryRepository expenseRegistryRepository;

    @Lazy
    public ExpenseRegistryServiceImpl(ExpenseRegistryRepository expenseRegistryRepository){
        this.expenseRegistryRepository = expenseRegistryRepository;
    }

    @Override
    public ExpenseRegistry save(ExpenseRegistry expenseRegistry) {
        return expenseRegistryRepository.save(expenseRegistry);
    }

    @Override
    public ExpenseRegistry getInventoryRegistryById(Long id) {
        return expenseRegistryRepository.findById(id).orElse(null);
    }

    @Override
    public List<ExpenseRegistry> getAllInventoryRegistries() {
        return (List<ExpenseRegistry>) expenseRegistryRepository.findAll();
    }

    @Override
    public void deleteInventoryRegistryById(Long id) {
        expenseRegistryRepository.deleteById(id);
    }

    @Override
    public List<ExpenseRegistry> getAllInvestmentRegistryByClient(Client client) {
        return expenseRegistryRepository.findAllInvestmentRegistryByClient(client);
    }
}
