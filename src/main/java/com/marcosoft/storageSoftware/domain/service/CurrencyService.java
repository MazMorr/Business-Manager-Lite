package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;

import java.util.List;

public interface CurrencyService {
    Currency save(Currency currency);

    Currency getCurrencyById(Long id);

    List<Currency> getAllCurrencies();

    void deleteCurrencyById(Long id);

    Currency getCurrencyByName(String name);

    List<Currency> getAllCurrenciesByCurrencyNameAndClient(String currencyName, Client client);

    boolean existsByCurrencyName(String currencyName);
}
