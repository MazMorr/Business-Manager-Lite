package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Product;

import java.util.List;

public interface ProductService {
    Product save(Product product);

    List<Product> getAllProducts();

    Product getByProductName(String name);
}
