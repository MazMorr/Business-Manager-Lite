package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.ExpenseRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExpenseRegistryRepository extends CrudRepository<ExpenseRegistry, Long> {
    List<ExpenseRegistry> findAllInvestmentRegistryByClient(Client client);
}