package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category getCategoryByName(String name);

    Category saveCategory(Category category);

    void deleteCategoryById(Long id);

    Category findOrCreateCategory(String categoryName);
}
