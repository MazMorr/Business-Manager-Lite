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
public class ExpenseDataTable {
    private Long id;
    private String expenseName;
    private String expenseType;
    private String priceAndCurrency;
    private Integer amount;
    private LocalDate receivedDate;
}
