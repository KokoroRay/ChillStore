package com.esms.service;

import com.esms.model.dto.CategoryDto;
import com.esms.model.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllCategory();
    List<Category> searchCategory(String keyword);
    Optional<Category> getCategoryById(Integer id);
    Category createCategory(CategoryDto categoryDto);
    Category updateCategory(Integer id, CategoryDto categoryDto);
    void deleteCategory(Integer id);
    List<Category> getAllParentOptions();
}
