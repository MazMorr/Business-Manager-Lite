package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.TransactionType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository extends CrudRepository<TransactionType, Long> {
    TransactionType findByTransactionName(String transactionName);
}