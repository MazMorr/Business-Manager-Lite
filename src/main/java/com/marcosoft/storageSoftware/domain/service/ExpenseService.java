package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    Expense save(Expense expense);

    Expense getExpenseById(Long id);

    List<Expense> getAllExpenses();

    List<Expense> getAllExpensesByClient(Client client);

    void deleteExpenseById(Long id);

    boolean existsByExpenseId(Long investmentId);

    List<Expense> getExpenseListByExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseTypeAndClientOrderByExpenseIdAsc(
            String expenseName, Double expensePrice, Currency currency, Integer amount, LocalDate receivedDate, String expenseType, Client client);

    List<Expense> getAllExpensesByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String expenseType);

}
