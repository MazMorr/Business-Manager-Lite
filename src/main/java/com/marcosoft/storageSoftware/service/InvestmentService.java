package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Investment;

import java.util.List;

public interface InvestmentService {
    Investment save(Investment investment);

    Investment getInvestmentById(Long id);

    List<Investment> getAllInvestments();

    void deleteInvestmentById(Long id);
}
