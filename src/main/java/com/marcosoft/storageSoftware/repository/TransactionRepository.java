package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findByClientId_IsClientActiveOrderByTransactionIdAsc(Boolean isClientActive);
}