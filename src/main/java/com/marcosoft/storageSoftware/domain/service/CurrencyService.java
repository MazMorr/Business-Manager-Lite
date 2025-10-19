package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Inventory;

import java.util.List;
import java.util.Map;

public interface CurrencyService {
    Currency save(Currency currency);

    Currency getCurrencyById(Long id);

    List<Currency> getAllCurrencies();

    void deleteCurrencyById(Long id);

    Currency getCurrencyByName(String name);

    boolean existsByCurrencyName(String currencyName);

    Double convertToCUP(Double amount, String fromCurrency);

    Double convertFromCUP(Double amountInCUP, String toCurrency);

    Double convertCurrency(Double amount, String fromCurrency, String toCurrency);

    Double calculateTotalInCUP(List<CurrencyTransaction> transactions);

    boolean isValidCurrency(String currencyName);

    Double getExchangeRateToCUP(String currencyName);

    Double calculateWeightedAverage(List<CurrencyTransaction> transactions, String targetCurrency);

    Double calculateWeightedAverage(List<Double> amounts, List<Double> quantities);

    Double calculateInventoryWeightedAverage(List<Inventory> inventories, String targetCurrency);

    Map<String, Double> consolidateInventoriesByWarehouse(List<Inventory> inventories, String targetCurrency);
}
