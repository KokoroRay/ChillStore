package com.esms.service;

import com.esms.model.dto.CategoryDTO;
import com.esms.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllCategory();


    Page<Category> searchCategory(String keyword, Pageable pa);

    Optional<Category> getCategoryById(Integer id);
    Category createCategory(CategoryDTO categoryDto);
    Category updateCategory(Integer id, CategoryDTO categoryDto);
    void deleteCategory(Integer id);

    List<Category> getAllParentOptions();
//    List<Category> getAllParentOptions();
//    List<Brand> findAllById(List<Long> brandIds);
}
