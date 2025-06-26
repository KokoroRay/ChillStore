package com.esms.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDto {
    
    private Integer productId;
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 100, message = "Tên sản phẩm phải từ 2 đến 100 ký tự")
    private String name;
    
    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 10, max = 500, message = "Mô tả phải từ 10 đến 500 ký tự")
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Giá không được vượt quá 999,999,999.99")
    private BigDecimal price;
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    @Max(value = 999999, message = "Số lượng tồn kho không được vượt quá 999,999")
    private Integer stockQty;
    
    private boolean status = true;
    
    private String imageUrl;
    
    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
    
    private String categoryName;
    
    @NotNull(message = "Thương hiệu không được để trống")
    private Integer brandId;
    
    private String brandName;
    
    // Constructors
    public ProductDto() {
    }
    
    public ProductDto(Integer productId, String name, String description, BigDecimal price, 
                     Integer stockQty, boolean status, String imageUrl, 
                     Integer categoryId, String categoryName, 
                     Integer brandId, String brandName) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.status = status;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
    }
    
    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQty() {
        return stockQty;
    }
    
    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }
    
    public boolean isStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getBrandId() {
        return brandId;
    }
    
    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    @Override
    public String toString() {
        return "ProductDto{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", status=" + status +
                ", imageUrl='" + imageUrl + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", brandId=" + brandId +
                ", brandName='" + brandName + '\'' +
                '}';
    }
} 