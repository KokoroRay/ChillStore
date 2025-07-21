package com.esms.service;

import com.esms.model.dto.CategoryDTO;
import com.esms.model.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface CategoryService {

    List<Category> getAllCategory();


    List<Category> getCategories();

    Optional<Category> getCategoryById(Integer id);
    Category createCategory(CategoryDTO categoryDto);
    Category updateCategory(Integer id, CategoryDTO categoryDto);
    void deleteCategory(Integer id);

    List<Category> getAllParentOptions();
//    List<Category> getAllParentOptions();
//    List<Brand> findAllById(List<Long> brandIds);
}
