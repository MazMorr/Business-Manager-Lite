package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Currency;
import com.marcosoft.storageSoftware.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.service.CurrencyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository){
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Currency save(Currency currency) {
        return currencyRepository.save(currency);
    }

    @Override
    public Currency getCurrencyById(Long id) {
        return currencyRepository.findById(id).orElse(null);
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return (List<Currency>) currencyRepository.findAll();
    }

    @Override
    public void deleteCurrencyById(Long id) {
        currencyRepository.deleteById(id);
    }

    @Override
    public Currency getCurrencyByName(String name) {
        return currencyRepository.findByCurrencyName(name);
    }

    @Override
    public boolean existsByCurrencyName(String currencyName) {
        return currencyRepository.existsByCurrencyName(currencyName);
    }
}
