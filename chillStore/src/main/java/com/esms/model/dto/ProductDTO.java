package com.esms.model.dto;

import com.esms.model.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTO {
    private Integer productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Category category;
    private String description;
    private Integer stockQty;
    private boolean status;
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private String discountName;
    private Double discountPercent;
    private LocalDateTime discountStart;
    private LocalDateTime discountEnd;


    public ProductDTO(Integer productId, String name, BigDecimal price, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public ProductDTO(String name, String imageUrl, BigDecimal price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public ProductDTO(Integer productId, String name, String imageUrl, BigDecimal price, Category category) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.category = category;
    }

    public ProductDTO() {

    }
// Getters and Setters

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getBrandId() { return brandId; }
    public void setBrandId(Integer brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public LocalDateTime getDiscountStart() {
        return discountStart;
    }

    public void setDiscountStart(LocalDateTime discountStart) {
        this.discountStart = discountStart;
    }

    public LocalDateTime getDiscountEnd() {
        return discountEnd;
    }

    public void setDiscountEnd(LocalDateTime discountEnd) {
        this.discountEnd = discountEnd;
    }
}