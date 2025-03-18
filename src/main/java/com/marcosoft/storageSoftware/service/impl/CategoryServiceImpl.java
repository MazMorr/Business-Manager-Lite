package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Category;
import com.marcosoft.storageSoftware.repository.CategoryRepository;
import com.marcosoft.storageSoftware.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return (List<Category>) categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByCategoryName(name);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category findOrCreateCategory(String categoryName) {
        Category existingCategory = categoryRepository.findByCategoryName(categoryName);
        if (existingCategory != null) {
            return existingCategory;
        }
        Category newCategory = new Category(null, categoryName);
        categoryRepository.save(newCategory);
        return newCategory;
    }

}
