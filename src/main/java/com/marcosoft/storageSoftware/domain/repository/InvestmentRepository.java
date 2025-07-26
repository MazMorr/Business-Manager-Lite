package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Investment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvestmentRepository extends CrudRepository<Investment, Long> {

    boolean existsByInvestmentId(Long investmentId);

    List<Investment> findByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);
}