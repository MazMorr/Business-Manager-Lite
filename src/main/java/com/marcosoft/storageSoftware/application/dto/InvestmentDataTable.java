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
public class InvestmentDataTable {
    private Long id;
    private String investmentName;
    private String investmentType;
    private Double price;
    private String currency;
    private Integer amount;
    private LocalDate receivedDate;
}
