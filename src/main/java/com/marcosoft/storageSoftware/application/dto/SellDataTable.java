package com.marcosoft.storageSoftware.application.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SellDataTable {
    private String productName;
    private Double sellPrice ;
    private String warehouseName;
    private Integer productAmount;
}
