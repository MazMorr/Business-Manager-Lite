package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Investment;

import java.util.List;

public interface InvestmentService {
    Investment save(Investment investment);

    Investment getInvestmentById(Long id);

    List<Investment> getAllInvestments();

    void deleteInvestmentById(Long id);

    boolean existsByInvestmentId(Long investmentId);

    List<Investment> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);
}
