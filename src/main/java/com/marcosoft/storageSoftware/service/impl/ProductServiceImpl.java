package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Product;
import com.marcosoft.storageSoftware.repository.ProductRepository;
import com.marcosoft.storageSoftware.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll();
    }

    @Override
    public Product getByProductName(String name) {
        return productRepository.findByProductName(name);
    }


    public boolean productExists(String productName) {
        return productRepository.findByProductName(productName) != null;
    }
}
