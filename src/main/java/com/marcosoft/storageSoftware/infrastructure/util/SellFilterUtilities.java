package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.domain.model.Inventory;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.model.SellFilterCriteria;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Lazy
@Component
public class SellFilterUtilities {

    private final ParseDataTypes parseDataTypes;

    public SellFilterUtilities(ParseDataTypes parseDataTypes) {
        this.parseDataTypes = parseDataTypes;
    }

    public SellFilterCriteria getFilterCriteria(
            String productName, String warehouseName, String minFilterAmount, String maxFilterAmount,
            String minFilterPrice, String maxFilterPrice
            ) {
        return new SellFilterCriteria(
                productName.trim().toLowerCase(),
                warehouseName.trim().toLowerCase(),
                minFilterAmount.isEmpty() ? null : parseDataTypes.parseInt(minFilterAmount),
                maxFilterAmount.isEmpty() ? null : parseDataTypes.parseInt(maxFilterAmount),
                minFilterPrice.isEmpty() ? null : parseDataTypes.parseDouble(minFilterPrice),
                maxFilterPrice.isEmpty() ? null : parseDataTypes.parseDouble(maxFilterPrice)
        );
    }

    public boolean matchesProductFilters(Product product, SellFilterCriteria filters) {
        if (!filters.productNameFilter().isEmpty() &&
                !product.getProductName().toLowerCase().contains(filters.productNameFilter())) {
            return false;
        }
        if (filters.minPrice() != null && (product.getSellPrice() == null || product.getSellPrice() < filters.minPrice())) {
            return false;
        }
        return filters.maxPrice() == null || (product.getSellPrice() != null && product.getSellPrice() <= filters.maxPrice());
    }

    public List<Inventory> filterByWarehouseAndAmount(List<Inventory> invList, SellFilterCriteria filters) {
        return invList.stream()
                .filter(inv -> {
                    if (!filters.warehouseNameFilter().isEmpty() &&
                            !inv.getWarehouse().getWarehouseName().toLowerCase().contains(filters.warehouseNameFilter())) {
                        return false;
                    }
                    if (filters.minAmount() != null && inv.getAmount() < filters.minAmount()) {
                        return false;
                    }
                    return filters.maxAmount() == null || inv.getAmount() <= filters.maxAmount();
                })
                .toList();
    }

    public Map<Product, List<Inventory>> groupAndFilterInventories(List<Inventory> inventories, SellFilterCriteria filters) {
        return inventories.stream()
                .filter(inv -> inv != null && inv.getProduct() != null && inv.getWarehouse() != null)
                .filter(inv -> matchesProductFilters(inv.getProduct(), filters))
                .collect(Collectors.groupingBy(Inventory::getProduct));
    }
}
