package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.TransactionType;
import com.marcosoft.storageSoftware.repository.TransactionTypeRepository;
import com.marcosoft.storageSoftware.service.TransactionTypeService;

import java.util.List;

public class TransactionTypeServiceImpl implements TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeServiceImpl(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Override
    public List<TransactionType> getAllTransactionTypes() {
        return (List<TransactionType>) transactionTypeRepository.findAll();
    }

    @Override
    public TransactionType getTransactionTypeById(Long id) {
        return transactionTypeRepository.findById(id).orElse(null);
    }

    @Override
    public TransactionType saveTransactionType(TransactionType transactionType) {
        return transactionTypeRepository.save(transactionType);
    }

    @Override
    public void deleteTransactionTypeById(Long id) {
        transactionTypeRepository.deleteById(id);
    }
}
