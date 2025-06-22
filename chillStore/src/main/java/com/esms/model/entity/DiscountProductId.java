package com.esms.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key class cho bảng promotion_products
 * Chứa promo_id và product_id làm primary key
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountProductId implements Serializable {
    
    private Integer promoId; // ID của discount
    private Integer productId; // ID của product
} 