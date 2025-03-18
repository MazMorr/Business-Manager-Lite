package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Currency;
import com.marcosoft.storageSoftware.model.Product;
import com.marcosoft.storageSoftware.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransactions();

    Transaction getTransactionbyId(Long id);

    Transaction saveTransaction(Transaction transaction);

    void deleteTransactionById(Long id);

    Transaction createTransaction(Product product, BigDecimal price, int quantity, LocalDate date, Currency currency);
}
