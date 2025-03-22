package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
}