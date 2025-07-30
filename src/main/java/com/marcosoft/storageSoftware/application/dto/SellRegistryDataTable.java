package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellRegistryDataTable {
    private String registryType;
    private LocalDateTime registryDate;
    private String productName;
    private String sellPriceAndCurrency;
    private LocalDate sellDate;
    private String warehouseName;
    private Integer amount;
}
