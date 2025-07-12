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

    public Product createOrUpdateProduct(Product product) {
        validateProductInputs(product); // Validar entradas
        Product existingProduct = productRepository.findById(product.getId()).orElse(null);
        if (existingProduct != null) {
            // Actualizar producto existente
            existingProduct.setProductName(product.getProductName());
            existingProduct.setClient(product.getClient());
            return productRepository.save(existingProduct);
        } else {
            // Crear nuevo producto
            return productRepository.save(product);
        }
    }

    public void validateProductInputs(Product product) {
        if (product.getProductName() == null || product.getProductName().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
    }
}
