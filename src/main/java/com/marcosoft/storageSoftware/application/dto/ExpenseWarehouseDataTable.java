package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseWarehouseDataTable {
    private Long expenseId;
    private String expenseName;
    private Integer productAmount;
    private LocalDate expenseDate;
}
