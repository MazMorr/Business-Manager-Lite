package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BuyRegistryDataTable {
    private String registryType;
    private LocalDateTime registryDate;
    private Long id;
    private String buyName;
    private String unitaryPriceAndCurrency;
    private String totalPriceAndCurrency;
    private Integer amount;
}
