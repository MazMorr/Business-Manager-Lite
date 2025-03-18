package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.*;
import com.marcosoft.storageSoftware.repository.CategoryRepository;
import com.marcosoft.storageSoftware.repository.ProductRepository;
import com.marcosoft.storageSoftware.repository.TransactionRepository;
import com.marcosoft.storageSoftware.service.CategoryService;
import com.marcosoft.storageSoftware.service.ProductService;
import com.marcosoft.storageSoftware.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    CategoryService categoryService;
    TransactionService transactionService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public void addProduct(String productName, String categoryName, BigDecimal price, int quantity,
                           LocalDate date, Currency currency) {
        Category category = categoryService.findOrCreateCategory(categoryName);
        Product product = new Product(null, productName, category);
        Transaction transaction = transactionService.createTransaction(product, price, quantity, date, currency);

        categoryRepository.save(category);
        productRepository.save(product);
        transactionRepository.save(transaction);
    }
}