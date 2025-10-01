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
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final String CUP = "CUP";
    private static final String MLC = "MLC";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private final ExpenseRepository expenseRepository;
    private final CurrencyRepository currencyRepository;
    private final Map<String, Double> currencyRatesCache = new ConcurrentHashMap<>();

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
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Override
    public List<Expense> getAllExpenses() {
        return (List<Expense>) expenseRepository.findAll();
    }

    @Override
    public List<Expense> getAllExpensesByClient(Client client) {
        return expenseRepository.findAllExpensesByClientOrderByReceivedDateDesc(client);
    }

    @Override
    @Transactional
    public void deleteExpenseById(Long id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public boolean existsByExpenseId(Long investmentId) {
        return expenseRepository.existsByExpenseId(investmentId);
    }

    @Override
    public List<Expense> getExpenseListByExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseTypeAndClientOrderByExpenseIdAsc(
            String expenseName, Double expensePrice, Currency currency, Integer amount, LocalDate receivedDate, String expenseType, Client client) {
        return expenseRepository.findListByExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseTypeAndClientOrderByExpenseIdAsc(
                expenseName, expensePrice, currency, amount, receivedDate, expenseType, client);
    }

    @Override
    public List<Expense> getAllExpensesByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType) {
        return expenseRepository.findAllExpensesByClientAndAmountGreaterThanAndExpenseType(client, 0, investmentType);
    }

    public Double getTotalFuelsAndLubricantsExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Combustibles y Lubricantes");
    }

    public Double getTotalStaffExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Gastos de Personal");
    }

    public Double getTotalEnergyExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Energía");
    }

    public Double getTotalRawMaterialsAndSuppliesExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Materias Primas y Materiales");
    }

    public Double getTotalOtherMonetaryExpense(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        return calculateExpense(client, initDate, endDate, currency, "Otros Gastos Monetarios");
    }

    private Double calculateExpense(
            Client client, LocalDate initDate, LocalDate endDate, Currency currency, String investmentType
    ) {
        Map<String, Double> amounts = new HashMap<>();
        amounts.put(CUP, 0.0);
        amounts.put(MLC, 0.0);
        amounts.put(USD, 0.0);
        amounts.put(EUR, 0.0);

        expenseRepository.findAllExpensesByClientAndExpenseType(client, investmentType)
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

    public List<Expense> getExpensesInDateRange(Client client, LocalDate startDate, LocalDate endDate) {
        return getAllExpensesByClient(client).stream()
                .filter(ex -> {
                    LocalDate receivedDate = ex.getReceivedDate();
                    return !receivedDate.isBefore(startDate) && !receivedDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}
