package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Currency;
import org.springframework.data.repository.CrudRepository;

public interface CurrencyRepository extends CrudRepository<Currency, String> {
}