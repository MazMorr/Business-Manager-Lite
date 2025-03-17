package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}