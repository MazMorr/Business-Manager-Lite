package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.model.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, String> {
    @Query("select count(p) from Product p where p.quantityInStorage = ?1")
    long countByQuantityInStorage(Integer quantityInStorage);

    Product findByProductName(String productName);

    @Transactional
    @Modifying
    @Query("update Product p set p.quantityInStorage = ?1 where p.productName = ?2")
    int updateQuantityInStorageByProductName(Integer quantityInStorage, String productName);

    void deleteByProductName(String productName);

    List<Product> findByClient_IsClientActive(Boolean isClientActive);

}