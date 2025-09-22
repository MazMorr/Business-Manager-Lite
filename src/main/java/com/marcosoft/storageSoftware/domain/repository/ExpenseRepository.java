package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    boolean existsByExpenseId(Long investmentId);

    List<Expense> findAllExpensesByClientAndAmountGreaterThanAndExpenseType(Client client, Integer amount, String investmentType);



    List<Expense> findAllExpensesByClientAndExpenseType(Client client, String expenseType);

    List<Expense> findAllExpensesByClientOrderByReceivedDateDesc(Client client);

    List<Expense> findListByExpenseNameAndExpensePriceAndCurrencyAndAmountAndReceivedDateAndExpenseTypeAndClientOrderByExpenseIdAsc(String expenseName, Double expensePrice, Currency currency, Integer amount, LocalDate receivedDate, String expenseType, Client client);

}