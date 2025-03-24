package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Currency;

import java.util.List;

public interface CurrencyService {

    Currency save(Currency currency);
    Currency getCurrencyById(String id);
    List<Currency> getAllCurrencies();
    void deleteById(String id);
}
