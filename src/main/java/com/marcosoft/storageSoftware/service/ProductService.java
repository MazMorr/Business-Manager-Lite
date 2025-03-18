package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product saveProduct(Product product);

    void deleteProductById(Long id);
}
