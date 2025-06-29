package com.esms.util.MapperUtils;

import com.esms.model.dto.ProductDto;
import com.esms.model.entity.Product;
import com.esms.model.entity.Category;
import com.esms.model.entity.Brand;

public class ProductMapper {
    
    /**
     * Chuyển đổi từ Product entity sang ProductDto
     */
    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQty(product.getStockQty());
        dto.setStatus(product.isStatus());
        dto.setImageUrl(product.getImageUrl());
        
        // Map category
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // Map brand
        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }
        
        return dto;
    }
    
    /**
     * Chuyển đổi từ ProductDto sang Product entity
     */
    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQty(dto.getStockQty());
        product.setStatus(dto.isStatus());
        product.setImageUrl(dto.getImageUrl());
        
        // Map category
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        
        // Map brand
        if (dto.getBrandId() != null) {
            Brand brand = new Brand();
            brand.setId(dto.getBrandId());
            product.setBrand(brand);
        }
        
        return product;
    }
    
    /**
     * Cập nhật Product entity từ ProductDto
     */
    public static void updateEntityFromDto(Product product, ProductDto dto) {
        if (product == null || dto == null) {
            return;
        }
        
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQty(dto.getStockQty());
        product.setStatus(dto.isStatus());
        
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            product.setImageUrl(dto.getImageUrl());
        }
        
        // Update category
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        
        // Update brand
        if (dto.getBrandId() != null) {
            Brand brand = new Brand();
            brand.setId(dto.getBrandId());
            product.setBrand(brand);
        }
    }
} 