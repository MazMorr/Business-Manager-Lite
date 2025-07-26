package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellDataTable {
    private String productName;
    private Double sellPrice ;
    private String warehouseName;
    private Integer productAmount;
}
