package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.TransactionType;

import java.util.List;

public interface TransactionTypeService {

    TransactionType save(TransactionType transactionType);
    TransactionType getTransactionById(Long id);
    List<TransactionType> getAllTransactions();
    void deleteById(long id);
    TransactionType findByTransactionTypeName(String name);

}
