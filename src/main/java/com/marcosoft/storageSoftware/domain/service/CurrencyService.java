package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Currency;

import java.util.List;

public interface CurrencyService {
    Currency save(Currency currency);

    Currency getCurrencyById(Long id);

    List<Currency> getAllCurrencies();

    void deleteCurrencyById(Long id);

    Currency getCurrencyByName(String name);

    boolean existsByCurrencyName(String currencyName);
}
