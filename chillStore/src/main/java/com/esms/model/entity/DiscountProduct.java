package com.esms.model.entity;

import jakarta.persistence.*;

/**
 * Entity class cho bảng promotion_products (liên kết giữa Discount và Product)
 * Chứa thông tin về các sản phẩm được áp dụng discount
 */
@Entity
@Table(name = "promotion_products") // Giữ nguyên tên bảng trong database
public class DiscountProduct {
    
    @EmbeddedId
    private DiscountProductId id; // Composite key
    
    // Quan hệ với Discount
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promoId")
    @JoinColumn(name = "promo_id")
    private Discount discount;
    
    // Quan hệ với Product
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;
    
    // Constructor mặc định
    public DiscountProduct() {
    }
    
    // Constructor đầy đủ
    public DiscountProduct(DiscountProductId id, Discount discount, Product product) {
        this.id = id;
        this.discount = discount;
        this.product = product;
    }
    
    // Getters
    public DiscountProductId getId() {
        return id;
    }
    
    public Discount getDiscount() {
        return discount;
    }
    
    public Product getProduct() {
        return product;
    }
    
    // Setters
    public void setId(DiscountProductId id) {
        this.id = id;
    }
    
    public void setDiscount(Discount discount) {
        this.discount = discount;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
} 