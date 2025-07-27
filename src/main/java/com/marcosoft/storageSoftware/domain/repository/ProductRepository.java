package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {

    Product findByProductName(String productName);

    Product findByProductNameAndClient(String productName, Client client);

    boolean existsByProductNameAndClient(String productName, Client client);

    List<Product> findByClient(Client client);
}