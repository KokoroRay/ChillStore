package com.esms.model.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Integer productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;

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
}
