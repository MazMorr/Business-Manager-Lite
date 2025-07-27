package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    Currency findByCurrencyName(String currencyName);

    boolean existsByCurrencyName(String currencyName);

    List<Currency> findAllCurrenciesByCurrencyNameAndClient(String currencyName, Client client);
}