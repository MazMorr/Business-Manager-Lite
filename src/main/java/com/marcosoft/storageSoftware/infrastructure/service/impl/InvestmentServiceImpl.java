package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.repository.InvestmentRepository;
import com.marcosoft.storageSoftware.domain.service.InvestmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {
    InvestmentRepository investmentRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @Override
    @Transactional
    public Investment save(Investment investment) {
        return investmentRepository.save(investment);
    }

    @Override
    @Transactional(readOnly = true)
    public Investment getInvestmentById(Long id) {
        return investmentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Investment> getAllInvestments() {
        return (List<Investment>) investmentRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteInvestmentById(Long id) {
        investmentRepository.deleteById(id);
    }

    @Override
    public boolean existsByInvestmentId(Long investmentId) {
        return investmentRepository.existsByInvestmentId(investmentId);
    }

    @Override
    public Investment getByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType) {
        return investmentRepository.findByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(client, investmentName, investmentPrice, currency, amount, receivedDate, investmentType);
    }

    @Override
    public List<Investment> getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType) {
        return investmentRepository.findAllInvestmentsByClientAndAmountGreaterThanAndInvestmentType(client, 0, investmentType);
    }

    @Override
    public List<Investment> getAllProductInvestmentsGreaterThanZeroByClient(Client client) {
        return investmentRepository.findByClientAndLeftAmountGreaterThanAndInvestmentType(client, 0, "Producto");
    }

    @Override
    public List<Investment> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client) {
        return investmentRepository.findByLeftAmountGreaterThanAndClient(leftAmount, client);
    }

    public List<Investment> getNonZeroInvestmentsByClient(Client client) {
        return investmentRepository.findByLeftAmountGreaterThanAndClient(0, client);
    }

}
