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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    private static final String CUP = "CUP";
    private static final String MLC = "MLC";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private final InvestmentRepository investmentRepository;
    private final CurrencyRepository currencyRepository;
    private Map<String, Double> currencyRatesCache = new ConcurrentHashMap<>();

    public InvestmentServiceImpl(CurrencyRepository currencyRepository, InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
        this.currencyRepository = currencyRepository;
        initializeCurrencyCache();
    }

    private void initializeCurrencyCache() {
        currencyRatesCache.put(MLC, currencyRepository.findByCurrencyName(MLC).getCurrencyPriceInCUP());
        currencyRatesCache.put(USD, currencyRepository.findByCurrencyName(USD).getCurrencyPriceInCUP());
        currencyRatesCache.put(EUR, currencyRepository.findByCurrencyName(EUR).getCurrencyPriceInCUP());
        currencyRatesCache.put(CUP, 1.0); // Tasa fija para CUP
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
        return calculateExpense(client, initDate, endDate, currency, "Renta");
    }

    public Double getTotalSalaryExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Salario");
    }

    public Double getTotalPublicityExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Publicidad");
    }

    public Double getTotalProductExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Producto");
    }

    public Double getTotalServiceExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Servicio");
    }

    private Double calculateExpense(
            Client client, LocalDate initDate, LocalDate endDate, Currency currency, String investmentType
    ) {
        Map<String, Double> amounts = new HashMap<>();
        amounts.put(CUP, 0.0);
        amounts.put(MLC, 0.0);
        amounts.put(USD, 0.0);
        amounts.put(EUR, 0.0);

        investmentRepository.findAllInvestmentsByClientAndInvestmentType(client, investmentType)
                .stream()
                .filter(inv -> isWithinDateRange(inv.getReceivedDate(), initDate, endDate))
                .forEach(inv -> {
                    String curr = inv.getCurrency().getCurrencyName();
                    amounts.merge(curr, inv.getInvestmentPrice(), Double::sum);
                });

        return convertToTargetCurrency(amounts, currency);
    }

    private boolean isWithinDateRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private Double convertToTargetCurrency(Map<String, Double> amounts, Currency targetCurrency) {
        double totalInCUP = amounts.get(CUP)
                + amounts.get(MLC) * currencyRatesCache.get(MLC)
                + amounts.get(USD) * currencyRatesCache.get(USD)
                + amounts.get(EUR) * currencyRatesCache.get(EUR);

        String targetCurrencyName = targetCurrency.getCurrencyName();

        if (CUP.equals(targetCurrencyName)) {
            return totalInCUP;
        }

        Double rate = currencyRatesCache.get(targetCurrencyName);
        return rate != null ? totalInCUP / rate : totalInCUP;
    }
}
