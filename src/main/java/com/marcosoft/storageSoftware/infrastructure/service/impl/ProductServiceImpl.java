package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Product;
import com.marcosoft.storageSoftware.domain.repository.ProductRepository;
import com.marcosoft.storageSoftware.domain.service.ProductService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Lazy
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
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product getByProductNameAndClient(String productName, Client client) {
        return productRepository.findByProductNameAndClient(productName, client);
    }

    @Override
    public boolean existsByProductNameAndClient(String productName, Client client) {
        return productRepository.existsByProductNameAndClient(productName, client);
    }

    @Override
    public List<Product> getAllProductsByClient(Client client) {
        return productRepository.findByClient(client);
    }

}
