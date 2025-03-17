package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.TransactionType;
import org.springframework.data.repository.CrudRepository;

public interface TransactionTypeRepository extends CrudRepository<TransactionType, Long> {
}