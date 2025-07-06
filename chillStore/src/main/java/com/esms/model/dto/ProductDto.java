package com.esms.model.dto;

import com.esms.model.entity.Category;

import java.math.BigDecimal;

public class ProductDTO {
    private Integer productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Category category;

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
}
