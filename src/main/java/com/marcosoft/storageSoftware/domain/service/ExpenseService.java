package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    Expense save(Expense expense);

    Expense getInvestmentById(Long id);

    List<Expense> getAllInvestments();

    void deleteInvestmentById(Long id);

    boolean existsByInvestmentId(Long investmentId);

    Expense getByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType);

    List<Expense> getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType);

    List<Expense> getAllProductInvestmentsGreaterThanZeroByClient(Client client);

    List<Expense> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);
}
