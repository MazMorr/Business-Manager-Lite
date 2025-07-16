package com.marcosoft.storageSoftware.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryObservableList {
    private Long id;
    private String productName;
    private Integer productAmount;
    private String warehouseName;
}
