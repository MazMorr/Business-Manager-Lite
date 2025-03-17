package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Integer> {

  Category findByCategoryName(String categoryName);
}