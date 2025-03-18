package com.marcosoft.storageSoftware.service;


import com.marcosoft.storageSoftware.model.TransactionType;

import java.util.List;

public interface TransactionTypeService {
    List<TransactionType> getAllTransactionTypes();
    TransactionType getTransactionTypeById(Long id);
    TransactionType saveTransactionType(TransactionType transactionType);
    void deleteTransactionTypeById(Long id);

}
