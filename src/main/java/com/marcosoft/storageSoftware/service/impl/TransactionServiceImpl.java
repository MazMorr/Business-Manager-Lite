package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Transaction;
import com.marcosoft.storageSoftware.repository.TransactionRepository;
import com.marcosoft.storageSoftware.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository=transactionRepository;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return (List<Transaction>) transactionRepository.findAll();
    }

    @Override
    public Transaction getTransactionbyId(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransactionById(Long id) {
        transactionRepository.deleteById(id);
    }
}
