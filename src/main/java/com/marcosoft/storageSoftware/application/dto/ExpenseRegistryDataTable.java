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
public class ExpenseRegistryDataTable {
    private String registryType;
    private LocalDateTime registryDate;
    private Long expenseId;
    private String expenseName;
    private String buyPriceAndCurrency;
}
