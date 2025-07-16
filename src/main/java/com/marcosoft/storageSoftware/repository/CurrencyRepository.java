package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Currency;
import org.springframework.data.repository.CrudRepository;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    Currency findByCurrencyName(String currencyName);

    boolean existsByCurrencyName(String currencyName);
}