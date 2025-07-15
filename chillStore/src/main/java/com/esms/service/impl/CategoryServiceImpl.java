package com.esms.service.impl;

import com.esms.model.dto.CategoryDTO;
import com.esms.model.entity.Category;
import com.esms.repository.CategoryRepository;
import com.esms.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> searchCategory(String keyword, Pageable pa) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll(pa);
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pa);
    }

    @Override
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(CategoryDTO categoryDto) {
        if (categoryDto.getName() == null || categoryDto.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên thư mục không được để trống");
        }
        Category category = new Category();
        category.setName(categoryDto.getName().trim());
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Integer id, CategoryDTO categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categorry không tồn tại" + id));
        if (categoryDto.getParentId() != null && categoryDto.getParentId().equals(id)) {
            throw new RuntimeException("Cannot set self as parent");
        }
        if (categoryDto.getName() != null && categoryDto.getName().trim().isEmpty()) {
            throw new RuntimeException("Name cannot be blank");
        }
        category.setName(categoryDto.getName());
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
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
            Integer maxId = categoryRepository.findMaxId();
            reseedIdentityTo(maxId);
        } else {
            throw new RuntimeException("Category không tồn tại id" + id);
        }
    }

    @Override
    public List<Category> getAllParentOptions() {
        return categoryRepository.findAll();
    }


    private void reseedIdentityTo(Integer newSeed) {
        String table = "categories";
        int seed = (newSeed != null) ? newSeed : 0;
        String sql = String.format("DBCC CHECKIDENT('%s', RESEED, %d)", table, seed);
        jdbcTemplate.execute(sql);
    }


}
