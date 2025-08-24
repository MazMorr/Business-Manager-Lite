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
    private Double sellPrice;
    private String currency;
    private String warehouseName;
    private Integer productAmount;
    private String style="";

    public SellDataTable(
            String productName, Double sellPrice, String currency, String warehouseName, Integer productAmount
    ) {
        this.productName = productName;
        this.sellPrice = sellPrice;
        this.currency = currency;
        this.warehouseName = warehouseName;
        this.productAmount = productAmount;
    }
}
