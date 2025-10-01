package com.marcosoft.storageSoftware.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDataTable {
    private String warehouseName;
    private String productName;
    private Integer productAmount;
    private String valueInCUP;
}
