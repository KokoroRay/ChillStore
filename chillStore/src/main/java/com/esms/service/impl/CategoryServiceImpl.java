package com.esms.service.impl;

import com.esms.model.dto.CategoryDto;
import com.esms.model.entity.Category;
import com.esms.repository.CategoryRepository;
import com.esms.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> searchCategory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        if (categoryDto.getParent_id() != null) {
            categoryRepository.findById(categoryDto.getParent_id()).ifPresent(category::setParent);
        } else {
            category.setParent(null);
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Integer id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categorry không tồn tại" + id));
        if (categoryDto.getParent_id() != null) {
            category.setName(categoryDto.getName());
            if (categoryDto.equals(id)) {
                throw new RuntimeException("Không thể thay đổi thư mục cha");
            }
            categoryRepository.findById(categoryDto.getParent_id()).ifPresent(parentCat -> {
                category.setParent(parentCat);
            });
        } else {
            category.setParent(null);
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category cat = category.get();
            if (cat.getChildren() != null && !cat.getChildren().isEmpty()) {
                throw new RuntimeException("Không thể xóa thư mục chứa thư mục con");
            }
            categoryRepository.delete(cat);
        }else {
            throw new RuntimeException("Category không tồn tại id" + id);
        }
    }

    @Override
    public List<Category> getAllParentOptions() {
        return categoryRepository.findAll();
    }
}
