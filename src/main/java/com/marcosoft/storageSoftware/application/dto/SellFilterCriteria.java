package com.marcosoft.storageSoftware.application.dto;

public record SellFilterCriteria
        (String productNameFilter,
         String warehouseNameFilter,
         Integer minAmount,
         Integer maxAmount,
         Double minPrice,
         Double maxPrice)
{ }
