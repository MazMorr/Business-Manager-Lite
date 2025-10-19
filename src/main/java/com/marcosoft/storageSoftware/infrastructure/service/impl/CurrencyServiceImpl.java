package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.repository.CurrencyRepository;
import com.marcosoft.storageSoftware.domain.service.CurrencyService;
import com.marcosoft.storageSoftware.domain.service.CurrencyTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository){
        this.currencyRepository = currencyRepository;
    }

    // ========== MÉTODOS EXISTENTES ORIGINALES ==========

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

    @Override
    public Double convertToCUP(Double amount, String fromCurrency) {
        if (amount == null) return 0.0;

        if ("CUP".equalsIgnoreCase(fromCurrency) || fromCurrency == null || fromCurrency.trim().isEmpty()) {
            return amount;
        }

        try {
            Currency currency = getCurrencyByName(fromCurrency);
            if (currency != null && currency.getValueInCUP() != null) {
                return amount * currency.getValueInCUP();
            }

            log.warn("No se encontró tasa de cambio para: {}", fromCurrency);
            return amount;

        } catch (Exception e) {
            log.error("Error convirtiendo a CUP: {} {} - {}", amount, fromCurrency, e.getMessage());
            return amount;
        }
    }

    @Override
    public Double convertFromCUP(Double amountInCUP, String toCurrency) {
        if (amountInCUP == null) return 0.0;

        if ("CUP".equalsIgnoreCase(toCurrency) || toCurrency == null || toCurrency.trim().isEmpty()) {
            return amountInCUP;
        }

        try {
            Currency currency = getCurrencyByName(toCurrency);
            if (currency != null && currency.getValueInCUP() != null && currency.getValueInCUP() != 0) {
                return amountInCUP / currency.getValueInCUP();
            }

            log.warn("No se encontró tasa de cambio válida para: {}", toCurrency);
            return amountInCUP;

        } catch (Exception e) {
            log.error("Error convirtiendo desde CUP: {} a {} - {}", amountInCUP, toCurrency, e.getMessage());
            return amountInCUP;
        }
    }

    @Override
    public Double convertCurrency(Double amount, String fromCurrency, String toCurrency) {
        if (amount == null) return 0.0;

        if (fromCurrency != null && fromCurrency.equals(toCurrency)) {
            return amount;
        }

        try {
            Double amountInCUP = convertToCUP(amount, fromCurrency);
            return convertFromCUP(amountInCUP, toCurrency);

        } catch (Exception e) {
            log.error("Error en conversión de moneda: {} {} -> {} - {}", amount, fromCurrency, toCurrency, e.getMessage());
            return amount;
        }
    }

    @Override
    public Double calculateTotalInCUP(List<CurrencyTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }

        return transactions.stream()
                .mapToDouble(transaction -> convertToCUP(transaction.amount(), transaction.currency()))
                .sum();
    }

    @Override
    public boolean isValidCurrency(String currencyName) {
        if (currencyName == null || currencyName.trim().isEmpty()) {
            return false;
        }

        if ("CUP".equalsIgnoreCase(currencyName)) {
            return true;
        }

        return getCurrencyByName(currencyName) != null;
    }

    @Override
    public Double getExchangeRateToCUP(String currencyName) {
        if ("CUP".equalsIgnoreCase(currencyName) || currencyName == null || currencyName.trim().isEmpty()) {
            return 1.0;
        }

        Currency currency = getCurrencyByName(currencyName);
        return (currency != null && currency.getValueInCUP() != null) ? currency.getValueInCUP() : 1.0;
    }

    // ========== NUEVOS MÉTODOS DE PROMEDIOS PONDERADOS ==========

    @Override
    public Double calculateWeightedAverage(List<CurrencyTransaction> transactions, String targetCurrency) {
        try {
            if (transactions == null || transactions.isEmpty()) {
                return 0.0;
            }

            BigDecimal totalWeightedValue = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (CurrencyTransaction transaction : transactions) {
                if (isValidTransaction(transaction)) {
                    Double amount = transaction.amount();
                    String sourceCurrency = transaction.currency();
                    Double quantity = transaction.quantity();

                    Double amountInTarget = convertCurrencySafe(amount, sourceCurrency, targetCurrency);

                    if (amountInTarget != null && amountInTarget > 0) {
                        BigDecimal amountBD = BigDecimal.valueOf(amountInTarget);
                        BigDecimal quantityBD = BigDecimal.valueOf(quantity);

                        totalWeightedValue = totalWeightedValue.add(amountBD.multiply(quantityBD));
                        totalQuantity = totalQuantity.add(quantityBD);
                    }
                }
            }

            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                return totalWeightedValue.divide(totalQuantity, 6, RoundingMode.HALF_UP).doubleValue();
            }
            return 0.0;

        } catch (Exception e) {
            log.error("Error calculando promedio ponderado: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Double calculateWeightedAverage(List<Double> amounts, List<Double> quantities) {
        try {
            if (amounts == null || quantities == null ||
                    amounts.size() != quantities.size() || amounts.isEmpty()) {
                return 0.0;
            }

            BigDecimal totalWeightedValue = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (int i = 0; i < amounts.size(); i++) {
                Double amount = amounts.get(i);
                Double quantity = quantities.get(i);

                if (amount != null && quantity != null && quantity > 0 && amount >= 0) {
                    BigDecimal amountBD = BigDecimal.valueOf(amount);
                    BigDecimal quantityBD = BigDecimal.valueOf(quantity);

                    totalWeightedValue = totalWeightedValue.add(amountBD.multiply(quantityBD));
                    totalQuantity = totalQuantity.add(quantityBD);
                }
            }

            if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                return totalWeightedValue.divide(totalQuantity, 6, RoundingMode.HALF_UP).doubleValue();
            }
            return 0.0;

        } catch (Exception e) {
            log.error("Error en calculateWeightedAverage: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Double calculateInventoryWeightedAverage(List<Inventory> inventories, String targetCurrency) {
        try {
            if (inventories == null || inventories.isEmpty()) {
                return 0.0;
            }

            List<CurrencyTransaction> transactions = inventories.stream()
                    .filter(this::isValidInventory)
                    .map(inv -> new CurrencyTransaction(
                            inv.getUnitPrice(),
                            inv.getCurrency(),
                            inv.getAmount() != null ? inv.getAmount().doubleValue() : 0.0
                    ))
                    .filter(this::isValidTransaction)
                    .collect(Collectors.toList());

            return calculateWeightedAverage(transactions, targetCurrency);

        } catch (Exception e) {
            log.error("Error calculando promedio de inventario: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Map<String, Double> consolidateInventoriesByWarehouse(List<Inventory> inventories, String targetCurrency) {
        Map<String, Double> consolidatedPrices = new HashMap<>();

        try {
            if (inventories == null || inventories.isEmpty()) {
                return consolidatedPrices;
            }

            Map<String, List<Inventory>> inventoriesByWarehouse = inventories.stream()
                    .filter(this::isValidInventory)
                    .collect(Collectors.groupingBy(inv ->
                            inv.getWarehouse() != null ? inv.getWarehouse().getWarehouseName() : "DEFAULT"
                    ));

            for (Map.Entry<String, List<Inventory>> entry : inventoriesByWarehouse.entrySet()) {
                Double weightedAverage = calculateInventoryWeightedAverage(entry.getValue(), targetCurrency);
                consolidatedPrices.put(entry.getKey(), weightedAverage);
            }

        } catch (Exception e) {
            log.error("Error consolidando inventarios: {}", e.getMessage());
        }

        return consolidatedPrices;
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private boolean isValidTransaction(CurrencyTransaction transaction) {
        return transaction != null &&
                transaction.amount() != null &&
                transaction.quantity() != null &&
                transaction.amount() >= 0 &&
                transaction.quantity() > 0 &&
                transaction.currency() != null;
    }

    private boolean isValidInventory(Inventory inventory) {
        return inventory != null &&
                inventory.getUnitPrice() != null &&
                inventory.getAmount() != null &&
                inventory.getUnitPrice() >= 0 &&
                inventory.getAmount() > 0;
    }

    private Double convertCurrencySafe(Double amount, String sourceCurrency, String targetCurrency) {
        try {
            if (amount == null || sourceCurrency == null || targetCurrency == null) {
                return amount;
            }

            if (sourceCurrency.equals(targetCurrency)) {
                return amount;
            }

            return convertCurrency(amount, sourceCurrency, targetCurrency);

        } catch (Exception e) {
            log.warn("Error en conversión de moneda, retornando valor original: {}", e.getMessage());
            return amount;
        }
    }
}