package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Currency;
import com.marcosoft.storageSoftware.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product saveProduct(Product product);

    void deleteProductById(Long id);

    void addProduct(String productName, String categoryName, BigDecimal price, int quantity, LocalDate date, Currency currency);
}
