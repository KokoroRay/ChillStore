package com.esms.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class cho bảng promotion_products (liên kết giữa Discount và Product)
 * Chứa thông tin về các sản phẩm được áp dụng discount
 */
@Entity
@Table(name = "promotion_products") // Giữ nguyên tên bảng trong database
@Data
@NoArgsConstructor
@AllArgsConstructor
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
} 