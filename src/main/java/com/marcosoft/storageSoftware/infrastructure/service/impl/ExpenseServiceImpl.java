package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;
import com.marcosoft.storageSoftware.domain.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.domain.repository.ExpenseRepository;
import com.marcosoft.storageSoftware.domain.service.ExpenseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final String CUP = "CUP";
    private static final String MLC = "MLC";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private final ExpenseRepository expenseRepository;
    private final CurrencyRepository currencyRepository;
    private Map<String, Double> currencyRatesCache = new ConcurrentHashMap<>();

    public ExpenseServiceImpl(CurrencyRepository currencyRepository, ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
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
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getInvestmentById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Override
    public List<Expense> getAllInvestments() {
        return (List<Expense>) expenseRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteInvestmentById(Long id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public boolean existsByInvestmentId(Long investmentId) {
        return expenseRepository.existsByInvestmentId(investmentId);
    }

    @Override
    public Expense getByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType) {
        return expenseRepository.findByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(client, investmentName, investmentPrice, currency, amount, receivedDate, investmentType);
    }

    @Override
    public List<Expense> getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType) {
        return expenseRepository.findAllInvestmentsByClientAndAmountGreaterThanAndInvestmentType(client, 0, investmentType);
    }

    @Override
    public List<Expense> getAllProductInvestmentsGreaterThanZeroByClient(Client client) {
        return expenseRepository.findByClientAndLeftAmountGreaterThanAndInvestmentType(client, 0, "Producto");
    }

    @Override
    public List<Expense> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client) {
        return expenseRepository.findByLeftAmountGreaterThanAndClient(leftAmount, client);
    }

    public List<Expense> getNonZeroInvestmentsByClient(Client client) {
        return expenseRepository.findByLeftAmountGreaterThanAndClient(0, client);
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

        expenseRepository.findAllInvestmentsByClientAndInvestmentType(client, investmentType)
                .stream()
                .filter(inv -> isWithinDateRange(inv.getReceivedDate(), initDate, endDate))
                .forEach(inv -> {
                    String curr = inv.getCurrency().getCurrencyName();
                    amounts.merge(curr, inv.getExpensePrice(), Double::sum);
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
