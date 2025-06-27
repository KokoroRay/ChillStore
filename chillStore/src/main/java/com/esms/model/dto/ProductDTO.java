package com.esms.model.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private String name;
    private String imageUrl;
    private BigDecimal price;

    public ProductDTO() {}

    public ProductDTO(String name, String imageUrl, BigDecimal price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
