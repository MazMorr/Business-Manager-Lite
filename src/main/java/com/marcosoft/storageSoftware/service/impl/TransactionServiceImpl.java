package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Transaction;
import com.marcosoft.storageSoftware.repository.TransactionRepository;
import com.marcosoft.storageSoftware.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository=transactionRepository;
    }


    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return ( List<Transaction>) transactionRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }
}
