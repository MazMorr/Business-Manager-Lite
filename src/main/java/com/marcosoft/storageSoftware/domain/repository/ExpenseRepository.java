package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Expense;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    boolean existsByInvestmentId(Long investmentId);

    List<Expense> findByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);

    List<Expense> findAllInvestmentsByClientAndAmountGreaterThanAndInvestmentType(Client client, Integer amount, String investmentType);

    Expense findByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType);

    List<Expense> findByClientAndLeftAmountGreaterThanAndInvestmentType(Client client, Integer leftAmount, String investmentType);

    List<Expense> findAllInvestmentsByClient(Client client);

    List<Expense> findAllInvestmentsByClientAndInvestmentType(Client client, String investmentType);
}