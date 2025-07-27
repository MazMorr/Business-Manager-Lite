package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Product;

import java.util.List;

public interface ProductService {
    Product save(Product product);

    List<Product> getAllProducts();

    Product getByProductNameAndClient(String productName, Client client);

    Product getByProductName(String name);

    boolean existsByProductNameAndClient(String productName, Client client);

    List<Product> getAllProductsByClient(Client client);
}
