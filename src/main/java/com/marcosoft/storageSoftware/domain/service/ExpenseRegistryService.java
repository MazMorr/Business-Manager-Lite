package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.ExpenseRegistry;

import java.util.List;

public interface ExpenseRegistryService {
    ExpenseRegistry save(ExpenseRegistry expenseRegistry);

    ExpenseRegistry getExpenseRegistryById(Long id);

    List<ExpenseRegistry> getAllExpenseRegistries();

    void deleteExpenseRegistryById(Long id);

    List<ExpenseRegistry> getAllExpenseRegistryByClient(Client client);
}
