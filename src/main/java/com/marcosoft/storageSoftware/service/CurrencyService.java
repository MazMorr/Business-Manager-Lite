package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Currency;

import java.util.List;


public interface CurrencyService {
    List<Currency> getAllCurrencies();
    Currency getCurrencyById(String id);
    Currency saveCurrency(Currency currency);
    void deleteCurrencyById(String id);
}
