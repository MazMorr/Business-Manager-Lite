package com.marcosoft.storageSoftware.application.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SellDataTable {
    private String productName;
    private String sellPriceAndCurrency;
    private String warehouseName;
    private Integer productAmount;
    private String buyPriceAndCurrency;
    private String style = "";

    public SellDataTable(
            String productName, String sellPriceAndCurrency, String warehouseName, Integer productAmount,
            String buyPriceAndCurrency
    ) {
        this.productName = productName;
        this.sellPriceAndCurrency = sellPriceAndCurrency;
        this.warehouseName = warehouseName;
        this.productAmount = productAmount;
        this.buyPriceAndCurrency = buyPriceAndCurrency;
    }
}
