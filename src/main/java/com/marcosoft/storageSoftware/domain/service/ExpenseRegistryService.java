package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.ExpenseRegistry;

import java.util.List;

public interface ExpenseRegistryService {
    ExpenseRegistry save(ExpenseRegistry expenseRegistry);

    ExpenseRegistry getInventoryRegistryById(Long id);

    List<ExpenseRegistry> getAllInventoryRegistries();

    void deleteInventoryRegistryById(Long id);

    List<ExpenseRegistry> getAllInvestmentRegistryByClient(Client client);
}
