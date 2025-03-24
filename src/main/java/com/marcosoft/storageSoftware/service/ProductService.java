package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Product;

import java.util.List;

public interface ProductService {
    Product save(Product product);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    void deleteById(Long id);
    Product findByProductName(String name);
    void updateQuantityInStorageByProductName(Integer stock, String name);
}
