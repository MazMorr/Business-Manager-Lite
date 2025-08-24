package com.marcosoft.storageSoftware.domain.model;

public record SellFilterCriteria
        (String productNameFilter,
         String warehouseNameFilter,
         Integer minAmount,
         Integer maxAmount,
         Double minPrice,
         Double maxPrice)
{ }
