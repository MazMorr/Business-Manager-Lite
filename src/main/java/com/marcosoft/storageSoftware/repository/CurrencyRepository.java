package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Currency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, String> {
}