package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;
import com.marcosoft.storageSoftware.domain.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.domain.repository.InvestmentRepository;
import com.marcosoft.storageSoftware.domain.service.InvestmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {
    InvestmentRepository investmentRepository;
    CurrencyRepository currencyRepository;

    public InvestmentServiceImpl(CurrencyRepository currencyRepository, InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
        this.currencyRepository = currencyRepository;
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

    public Double getTotalRentExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return getExpense(client, initDate, endDate, currency, "Renta");
    }

    public Double getTotalSalaryExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return getExpense(client, initDate, endDate, currency, "Salario");
    }

    public Double getTotalPublicityExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return getExpense(client, initDate, endDate, currency, "Publicidad");
    }

    public Double getTotalProductExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return getExpense(client, initDate, endDate, currency, "Producto");
    }

    public Double getTotalServiceExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return getExpense(client, initDate, endDate, currency, "Servicio");
    }

    public Double getExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency, String investmentType) {
        double cup = 0.0, mlc = 0.0, usd = 0.0, eur = 0.0;

        List<Investment> investmentList = investmentRepository.findAllInvestmentsByClientAndInvestmentType(client, investmentType);
        for (Investment inv : investmentList) {
            LocalDate date = inv.getReceivedDate();
            if ((date.isEqual(initDate) || date.isAfter(initDate)) &&
                    (date.isEqual(endDate) || date.isBefore(endDate))) {

                double price = inv.getInvestmentPrice();
                String curr = inv.getCurrency().getCurrencyName();

                switch (curr) {
                    case "CUP" -> cup += price;
                    case "MLC" -> mlc += price;
                    case "USD" -> usd += price;
                    case "EUR" -> eur += price;
                }
            }
        }

        double mlcRate = currencyRepository.findByCurrencyName("MLC").getCurrencyPriceInCUP();
        double usdRate = currencyRepository.findByCurrencyName("USD").getCurrencyPriceInCUP();
        double eurRate = currencyRepository.findByCurrencyName("EUR").getCurrencyPriceInCUP();

        double totalInCUP = cup + mlc * mlcRate + usd * usdRate + eur * eurRate;

        return switch (currency.getCurrencyName()) {
            case "CUP" -> totalInCUP;
            case "MLC" -> totalInCUP / mlcRate;
            case "USD" -> totalInCUP / usdRate;
            case "EUR" -> totalInCUP / eurRate;
            default -> totalInCUP;
        };
    }

}
