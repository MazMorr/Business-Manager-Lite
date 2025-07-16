package com.marcosoft.storageSoftware.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentObservableList {
    private Long id;
    private String productName;
    private Double price;
    private String currency;
    private Integer amount;
    private LocalDate receivedDate;
}
