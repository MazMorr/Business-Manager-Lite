package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface CategoryService {
    Category save(Category category);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
    void deleteCategoryById(Long id);
    Category  findByCategoryName(String name);
}
