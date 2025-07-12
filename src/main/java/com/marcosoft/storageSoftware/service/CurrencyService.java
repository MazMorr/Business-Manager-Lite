package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Currency;

import java.util.List;

public interface CurrencyService {
    Currency save(Currency currency);
    Currency getCurrencyById(Long id);
    List<Currency> getAllCurrencies();
    void deleteCurrencyById(Long id);
}
