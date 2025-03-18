package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Transaction;

import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransactions();

    Transaction getTransactionbyId(Long id);

    Transaction saveTransaction(Transaction transaction);

    void deleteTransactionById(Long id);
}
