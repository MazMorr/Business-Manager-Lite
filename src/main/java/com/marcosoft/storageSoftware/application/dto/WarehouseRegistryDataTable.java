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
public class WarehouseRegistryDataTable {
    private Long id;
    private String warehouseName;
    private String registryType;
    private LocalDateTime date;
}
