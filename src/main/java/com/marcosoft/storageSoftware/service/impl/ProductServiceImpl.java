package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Product;
import com.marcosoft.storageSoftware.repository.ProductRepository;
import com.marcosoft.storageSoftware.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product findByProductName(String name) {
        return productRepository.findByProductName(name);
    }

    @Override
    public void updateQuantityInStorageByProductName(Integer stock, String name) {
        productRepository.updateQuantityInStorageByProductName(stock, name);
    }
}
