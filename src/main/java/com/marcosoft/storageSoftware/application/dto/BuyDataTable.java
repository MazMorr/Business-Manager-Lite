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
public class BuyDataTable {
    private Long id;
    private String buyName;
    private String buyType;
    private String unitaryPriceAndCurrency;
    private String totalPriceAndCurrency;
    private Integer amount;
    private LocalDate receivedDate;
}
