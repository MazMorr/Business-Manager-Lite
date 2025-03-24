package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction save(Transaction transaction);
    Transaction getTransactionById(Long id);
    List<Transaction> getAllTransactions();
    void deleteById(Long id);
}
