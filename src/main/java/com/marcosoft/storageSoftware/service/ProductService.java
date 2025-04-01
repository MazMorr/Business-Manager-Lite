package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Product;

import java.util.List;

public interface ProductService {
    Product save(Product product);
    List<Product> getAllProducts();
    void deleteByProductName(String productName);
    Product getByProductName(String name);
    void updateQuantityInStorageByProductName(Integer stock, String name);
}
