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

    // Constants
    private static final String REGISTRY_TYPE_SALE = "Venta";
    private static final String REGISTRY_TYPE_SALE_WITH_COST = "Venta con Costo";
    private static final String CATEGORY_SALES = "Ventas";

    // Dependencies
    private final SellRegistryRepository sellRegistryRepository;
    private final BuyRegistryServiceImpl buyRegistryService;
    private final CurrencyRepository currencyRepository;
    private final ParseDataTypes parseDataTypes;
    private final GeneralRegistryServiceImpl generalRegistryService;
    private final InventoryServiceImpl inventoryService;

    public SellRegistryServiceImpl(
            CurrencyRepository currencyRepository,
            SellRegistryRepository sellRegistryRepository, BuyRegistryServiceImpl buyRegistryService,
            ParseDataTypes parseDataTypes,
            GeneralRegistryServiceImpl generalRegistryService,
            InventoryServiceImpl inventoryService
    ) {
        this.sellRegistryRepository = sellRegistryRepository;
        this.currencyRepository = currencyRepository;
        this.buyRegistryService = buyRegistryService;
        this.parseDataTypes = parseDataTypes;
        this.generalRegistryService = generalRegistryService;
        this.inventoryService = inventoryService;
    }

    // ===== INTERFACE IMPLEMENTATION =====
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

    // ===== SALE PROCESSING METHODS =====

    /**
     * Processes a sale transaction with inventory management
     */
    public void processSale(
            Inventory inventory,
            int productAmount,
            String productName,
            String sellProductPrice,
            String sellProductCurrency,
            LocalDate sellProductDate,
            String sellWarehouse,
            Client client
    ) {
        updateInventory(inventory, productAmount);
        registerSaleBasedOnCostAvailability(
                productAmount, productName, sellProductPrice,
                sellProductCurrency, sellProductDate, sellWarehouse, client
        );
    }

    /**
     * Registers a sale transaction without cost information (legacy method)
     */
    public void registerSaleTransaction(
            String productName,
            int productAmount,
            String sellProductPrice,
            String currency,
            LocalDate date,
            String sellWarehouse,
            Client client
    ) {
        double price = parseDataTypes.parseDouble(sellProductPrice);
        SellRegistry sellRegistry = createBasicSaleRegistry(
                productName, productAmount, price, currency, date, sellWarehouse, client
        );

        save(sellRegistry);
        createGeneralRegistry(client, productAmount, productName, null);
    }

    // ===== QUERY METHODS =====

    public List<SellRegistry> getSalesInDateRange(Client client, LocalDate startDate, LocalDate endDate) {
        return getAllSellRegistriesByClient(client).stream()
                .filter(sale -> isDateInRange(sale.getSellDate(), startDate, endDate))
                .collect(Collectors.toList());
    }

    public Double getTotalProfitInDateRange(Client client, LocalDate startDate, LocalDate endDate, String currency) {
        return getSalesInDateRange(client, startDate, endDate).stream()
                .mapToDouble(sale -> calculateProfitForSale(sale, currency))
                .sum();
    }


    // ===== PRIVATE HELPER METHODS =====

    private void updateInventory(Inventory inventory, int soldAmount) {
        int newAmount = inventory.getAmount() - soldAmount;
        inventory.setAmount(newAmount);
        inventoryService.save(inventory);
    }

    private void registerSaleBasedOnCostAvailability(
            int productAmount, String productName, String sellProductPrice,
            String sellProductCurrency, LocalDate sellProductDate, String sellWarehouse, Client client
    ) {
        registerSaleTransaction(
                productName, productAmount, sellProductPrice, sellProductCurrency, sellProductDate,
                sellWarehouse, client
        );

    }

    private SellRegistry createBasicSaleRegistry(
            String productName, int productAmount, double price, String currency,
            LocalDate date, String warehouse, Client client
    ) {
        SellRegistry registry = new SellRegistry();
        registry.setClient(client);
        registry.setRegistryType(REGISTRY_TYPE_SALE);
        registry.setRegistryDate(LocalDateTime.now());
        registry.setProductName(productName);
        registry.setSellCurrency(currency);
        registry.setSellPrice(price);
        registry.setSellDate(date);
        registry.setWarehouseName(warehouse);
        registry.setProductAmount(productAmount);
        return registry;
    }

    private void createGeneralRegistry(Client client, int productAmount, String productName, Double profit) {
        String description = profit != null
                ? String.format("Venta con costo de %d unidades de %s - Ganancia: %.2f",
                productAmount, productName, profit)
                : String.format("Venta de %d unidades de %s", productAmount, productName);

        GeneralRegistry generalRegistry = new GeneralRegistry(
                null, client, CATEGORY_SALES, description, LocalDateTime.now()
        );
        generalRegistryService.save(generalRegistry);
    }

    private double calculateProfitForSale(SellRegistry sale, String targetCurrency) {
        double profit = determineProfitAmount(sale);
        return convertProfitToCurrency(profit, sale.getSellCurrency(), targetCurrency);
    }

    private double determineProfitAmount(SellRegistry sale) {
        if (sale.getProfit() != null) {
            return sale.getProfit();
        } else if (sale.getTotalCost() != null) {
            return sale.getSellPrice() - sale.getTotalCost();
        } else {
            // If no cost information, profit equals sale price
            return sale.getSellPrice();
        }
    }

    private Double convertProfitToCurrency(Double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        double amountInCUP = convertToCUP(amount, fromCurrency);
        return convertFromCUP(amountInCUP, toCurrency);
    }

    private double convertToCUP(Double amount, String currency) {
        return amount * getCurrencyRate(currency);
    }

    private double convertFromCUP(double amountInCUP, String targetCurrency) {
        return switch (targetCurrency) {
            case "MLC" -> amountInCUP / getCurrencyRate("MLC");
            case "USD" -> amountInCUP / getCurrencyRate("USD");
            case "EUR" -> amountInCUP / getCurrencyRate("EUR");
            default -> amountInCUP; // CUP
        };
    }

    private double getCurrencyRate(String currencyName) {
        Currency currency = currencyRepository.findByCurrencyName(currencyName);
        return currency != null ? currency.getValueInCUP() : 1.0;
    }

    private boolean hasCostInformation(Double unitCost, String costCurrency) {
        return unitCost != null && costCurrency != null;
    }

    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}