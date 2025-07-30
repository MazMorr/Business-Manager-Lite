package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.InvestmentRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvestmentRegistryRepository extends CrudRepository<InvestmentRegistry, Long> {
    List<InvestmentRegistry> findAllInvestmentRegistryByClient(Client client);
}