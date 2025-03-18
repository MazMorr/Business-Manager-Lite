package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.*;
import com.marcosoft.storageSoftware.repository.TransactionRepository;
import com.marcosoft.storageSoftware.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
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

    @Override
    public Transaction createTransaction(Product product, BigDecimal price, int quantity, LocalDate date, Currency currency) {
        Transaction transaction = new Transaction();
        transaction.setIdProduct(product);
        transaction.setPrice(price);
        transaction.setStock(quantity);
        transaction.setDate(date);
        transaction.setCurrency(currency);

        TransactionType transactionType = new TransactionType();
        transactionType.setTransactionName("Compra");
        transaction.setTransactionType(transactionType);

        Client client = new Client();
        client.setClientName("Cuenta Predeterminada");
        client.setClientPassword("password");
        transaction.setIdClient(client);

        return transaction;
    }
}
