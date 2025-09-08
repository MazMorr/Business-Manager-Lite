package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.*;
import com.marcosoft.storageSoftware.domain.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.domain.repository.SellRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.SellRegistryService;
import com.marcosoft.storageSoftware.infrastructure.util.ParseDataTypes;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellRegistryServiceImpl implements SellRegistryService {

    private final SellRegistryRepository sellRegistryRepository;
    private final CurrencyRepository currencyRepository;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final InventoryServiceImpl inventoryService;

    public SellRegistryServiceImpl(
            CurrencyRepository currencyRepository, SellRegistryRepository sellRegistryRepository, ParseDataTypes parseDataTypes,
            GeneralRegistryServiceImpl generalRegistryService, InventoryServiceImpl inventoryService
    ) {
        this.sellRegistryRepository = sellRegistryRepository;
        this.currencyRepository = currencyRepository;
        this.parseDataTypes = parseDataTypes;
        this.generalRegistryService = generalRegistryService;
        this.inventoryService = inventoryService;
    }

    @Override
    public SellRegistry save(SellRegistry sellRegistry) {
        return sellRegistryRepository.save(sellRegistry);
    }

    @Override
    public List<SellRegistry> getAllSellRegistriesByClient(Client client) {
        return sellRegistryRepository.findAllSellRegistriesByClient(client);
    }

    @Override
    public SellRegistry getByIdAndClient(Long id, Client client) {
        return sellRegistryRepository.findByIdAndClient(id, client);
    }

    @Override
    public boolean existsByIdAndClient(Long id, Client client) {
        return sellRegistryRepository.existsByIdAndClient(id, client);
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
            case "MLC" -> totalInCUP / mlcRate;
            case "USD" -> totalInCUP / usdRate;
            case "EUR" -> totalInCUP / eurRate;
            default -> totalInCUP;
        };
    }



    public void registerSaleTransaction(
            String productName, int productAmount, String sellProductPrice, String currency, LocalDate date,
            String sellWarehouse, Client client
    ) {
        // Datos adicionales del formulario
        double price = parseDataTypes.parseDouble(sellProductPrice);

        SellRegistry sellRegistry = new SellRegistry(
                null, client, "Venta", LocalDateTime.now(),
                productName, currency, price, date,
                sellWarehouse, productAmount
        );
        save(sellRegistry);

        // Registrar en historial general
        GeneralRegistry generalRegistry = new GeneralRegistry(
                null, client, "Ventas",
                "Venta de " + productAmount + " unidades de " + productName,
                LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);
    }

    public void processSale(
            Inventory inventory, int productAmount, String productName, String sellProductPrice, String sellProductCurrency,
            LocalDate sellProductDate, String sellWarehouse, Client client
    ) {
        // Actualizar inventario
        int newAmount = inventory.getAmount() - productAmount;

        inventory.setAmount(newAmount);
        inventoryService.save(inventory);

        // Registrar venta
        registerSaleTransaction(
                productName, productAmount, sellProductPrice, sellProductCurrency, sellProductDate, sellWarehouse, client
        );
    }

    public List<SellRegistry> getSalesInDateRange(Client client, LocalDate startDate, LocalDate endDate) {
        return getAllSellRegistriesByClient(client).stream()
                .filter(ex -> {
                    LocalDate receivedDate = ex.getSellDate();
                    return !receivedDate.isBefore(startDate) && !receivedDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}
