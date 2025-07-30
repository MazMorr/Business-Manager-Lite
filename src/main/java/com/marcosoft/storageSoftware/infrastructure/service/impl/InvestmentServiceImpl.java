package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.repository.InvestmentRepository;
import com.marcosoft.storageSoftware.domain.service.InvestmentService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class InvestmentServiceImpl implements InvestmentService {
    InvestmentRepository investmentRepository;

    @Lazy
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

    @Override
    public boolean existsByInvestmentId(Long investmentId) {
        return investmentRepository.existsByInvestmentId(investmentId);
    }

    @Override
    public List<Investment> getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType) {
        return investmentRepository.findAllInvestmentsByClientAndAmountGreaterThanAndInvestmentType(client, 0, investmentType);
    }

    @Override
    public List<Investment> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client) {
        return investmentRepository.findByLeftAmountGreaterThanAndClient(leftAmount, client);
    }

    public List<Investment> getNonZeroInvestmentsByClient(Client client) {
        return investmentRepository.findByLeftAmountGreaterThanAndClient(0, client);
    }

}
