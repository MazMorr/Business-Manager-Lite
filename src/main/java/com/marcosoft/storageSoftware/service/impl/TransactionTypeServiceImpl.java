package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.TransactionType;
import com.marcosoft.storageSoftware.repository.TransactionTypeRepository;
import com.marcosoft.storageSoftware.service.TransactionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionTypeServiceImpl implements TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    @Autowired
    public TransactionTypeServiceImpl(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Override
    public TransactionType save(TransactionType transactionType) {
        return transactionTypeRepository.save(transactionType);
    }

    @Override
    public TransactionType getTransactionById(Long id) {
        return transactionTypeRepository.findById(id).orElse(null);
    }

    @Override
    public List<TransactionType> getAllTransactions() {
        return (List<TransactionType>) transactionTypeRepository.findAll();
    }

    @Override
    public void deleteById(long id) {
        transactionTypeRepository.deleteById(id);
    }

    @Override
    public TransactionType findByTransactionTypeName(String name) {
        return transactionTypeRepository.findByTransactionName(name);
    }
}
