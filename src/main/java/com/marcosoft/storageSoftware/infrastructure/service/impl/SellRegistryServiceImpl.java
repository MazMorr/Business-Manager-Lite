package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import com.marcosoft.storageSoftware.domain.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.domain.repository.SellRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.SellRegistryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SellRegistryServiceImpl implements SellRegistryService {

    private final SellRegistryRepository sellRegistryRepository;
    private final CurrencyRepository currencyRepository;

    public SellRegistryServiceImpl(CurrencyRepository currencyRepository, SellRegistryRepository sellRegistryRepository){
        this.sellRegistryRepository = sellRegistryRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public SellRegistry save(SellRegistry sellRegistry) {
        return sellRegistryRepository.save(sellRegistry);
    }

    @Override
    public List<SellRegistry> getAllSellRegistriesByClient(Client client) {
        return sellRegistryRepository.findAllSellRegistriesByClient(client);
    }

    public Double getTotalProductProfit(Client client, LocalDate initDate, LocalDate endDate, Currency currency) {
        double cup = 0.0, mlc = 0.0, usd = 0.0, eur = 0.0;

        List<SellRegistry> sellRegistryList = sellRegistryRepository.findAllSellRegistriesByClient(client);
        for (SellRegistry sell : sellRegistryList) {
            LocalDate date = sell.getSellDate();
            if ((date.isEqual(initDate) || date.isAfter(initDate)) &&
                    (date.isEqual(endDate) || date.isBefore(endDate))) {

                double price = sell.getSellPrice();
                String curr = sell.getSellCurrency();

                switch (curr) {
                    case "CUP" -> cup += price;
                    case "MLC" -> mlc += price;
                    case "USD" -> usd += price;
                    case "EUR" -> eur += price;
                }
            }
        }

        double mlcRate = currencyRepository.findByCurrencyName("MLC").getCurrencyPriceInCUP();
        double usdRate = currencyRepository.findByCurrencyName("USD").getCurrencyPriceInCUP();
        double eurRate = currencyRepository.findByCurrencyName("EUR").getCurrencyPriceInCUP();

        double totalInCUP = cup + mlc * mlcRate + usd * usdRate + eur * eurRate;

        return switch (currency.getCurrencyName()) {
            case "CUP" -> totalInCUP;
            case "MLC" -> totalInCUP / mlcRate;
            case "USD" -> totalInCUP / usdRate;
            case "EUR" -> totalInCUP / eurRate;
            default -> totalInCUP;
        };
    }

}
