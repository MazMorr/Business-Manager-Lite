package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Product;
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
    @Transactional
    public void deleteByProductName(String productName) {
        productRepository.deleteByProductName(productName);
    }

    @Override
    public Product getByProductName(String name) {
        return productRepository.findByProductName(name);
    }

    @Override
    public void updateQuantityInStorageByProductName(Integer stock, String name) {
        productRepository.updateQuantityInStorageByProductName(stock, name);
    }

    @Override
    public List<Product> getAllProductsByClient_IsClientActive(Boolean isClientActive) {
        return productRepository.findByClient_IsClientActive(isClientActive);
    }

    public boolean productExists(String productName) {
        return productRepository.findByProductName(productName) != null;
    }

    public Product createOrUpdateProduct(Product product) {
        validateProductInputs(product); // Validar entradas
        Product existingProduct = productRepository.findByProductName(product.getProductName());
        if (existingProduct != null) {
            // Actualizar producto existente
            existingProduct.setCategoryName(product.getCategoryName());
            existingProduct.setQuantityInStorage(product.getQuantityInStorage());
            existingProduct.setBuyPrice(product.getBuyPrice());
            existingProduct.setSellPrice(product.getSellPrice());
            existingProduct.setCurrencyBuyName(product.getCurrencyBuyName());
            existingProduct.setCurrencySellName(product.getCurrencySellName());
            existingProduct.setStoredIn(product.getStoredIn());
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
        if (product.getQuantityInStorage() == null || product.getQuantityInStorage() < 0) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero válido");
        }
        if (product.getBuyPrice() == null || product.getBuyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de compra debe ser un número válido");
        }
        if (product.getSellPrice() == null || product.getSellPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser un número válido");
        }
        if (product.getCategoryName() == null || product.getCategoryName().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
        if (product.getStoredIn() == null || product.getStoredIn().isEmpty()) {
            throw new IllegalArgumentException("El lugar de almacenamiento no puede estar vacío");
        }
    }
}
