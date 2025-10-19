package com.marcosoft.storageSoftware.domain.service;

/**
 * Clase auxiliar para representar transacciones monetarias con cantidad
 */
public record CurrencyTransaction(Double amount, String currency, Double quantity) {
    public CurrencyTransaction(Double amount, String currency, Double quantity) {
        this.amount = amount != null ? amount : 0.0;
        this.currency = currency != null ? currency : "CUP";
        this.quantity = quantity != null ? quantity : 0.0;
    }
}