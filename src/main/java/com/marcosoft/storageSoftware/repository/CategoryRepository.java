package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {


    Category findByCategoryName(String categoryName);
}