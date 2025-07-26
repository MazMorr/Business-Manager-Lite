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
public class InvestmentWarehouseDataTable {
    private Long investmentId;
    private String productName;
    private Integer productAmount;
    private LocalDate investmentDate;
}
