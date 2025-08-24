package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RealizedSellsDataTable {
    private Long id;
    private String warehouseName;
    private String productName;
    private Integer productAmount;
    private String sellPriceAndCurrency;
    private LocalDate sellDate;
}
