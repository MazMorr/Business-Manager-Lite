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
public class InvestmentRegistryDataTable {
    private String registryType;
    private LocalDateTime registryDate;
    private Long investmentId;
    private String investmentName;
    private String buyPriceAndCurrency;
}
