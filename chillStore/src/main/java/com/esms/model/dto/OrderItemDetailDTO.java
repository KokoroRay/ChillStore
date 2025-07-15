package com.esms.model.dto;

public class OrderItemDetailDTO {
    private String productName;
    private int quantity;
    private double priceEach;
    private String categoryName;

    public OrderItemDetailDTO(String productName, int quantity, double priceEach, String categoryName) {
        this.productName = productName;
        this.quantity = quantity;
        this.priceEach = priceEach;
        this.categoryName = categoryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceEach() {
        return priceEach;
    }

    public void setPriceEach(double priceEach) {
        this.priceEach = priceEach;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
} 