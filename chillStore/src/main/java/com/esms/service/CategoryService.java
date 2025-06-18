package com.esms.service;

import com.esms.model.dto.CategoryDto;
import com.esms.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllCategory();


    Page<Category> searchCategory(String keyword, Pageable pa);

    Optional<Category> getCategoryById(Integer id);
    Category createCategory(CategoryDto categoryDto);
    Category updateCategory(Integer id, CategoryDto categoryDto);
    void deleteCategory(Integer id);
    List<Category> getAllParentOptions();
}
