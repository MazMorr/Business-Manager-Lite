package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Investment;
import com.marcosoft.storageSoftware.repository.InvestmentRepository;
import com.marcosoft.storageSoftware.service.InvestmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {
    InvestmentRepository investmentRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    public Investment save(Investment investment) {
        return investmentRepository.save(investment);
    }

    @Override
    public Investment getInvestmentById(Long id) {
        return investmentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Investment> getAllInvestments() {
        return (List<Investment>) investmentRepository.findAll();
    }

    @Override
    public void deleteInvestmentById(Long id) {
        investmentRepository.deleteById(id);
    }
}
