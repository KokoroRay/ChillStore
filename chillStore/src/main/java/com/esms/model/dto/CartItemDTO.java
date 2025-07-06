package com.esms.model.dto;

public class CartItemDTO {
    private int cartItemId;
    private String productName;
    private double price;
    private double discount;
    private int quantity;
    private double totalPrice;


    public CartItemDTO(int cartItemId, String productName, double price, double discount, int quantity, double totalPrice) {
        this.cartItemId = cartItemId;
        this.productName = productName;
        this.price = price;
        this.discount = discount;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
