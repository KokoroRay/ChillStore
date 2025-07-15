package com.esms.model.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * Composite key class cho bảng promotion_products
 * Chứa promo_id và product_id làm primary key
 */
@Embeddable
public class DiscountProductId implements Serializable {

    private Integer promoId; // ID của discount
    private Integer productId; // ID của product

    // Constructor mặc định
    public DiscountProductId() {
    }

    // Constructor đầy đủ
    public DiscountProductId(Integer promoId, Integer productId) {
        this.promoId = promoId;
        this.productId = productId;
    }

    // Getters
    public Integer getPromoId() {
        return promoId;
    }

    public Integer getProductId() {
        return productId;
    }

    // Setters
    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    // equals và hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiscountProductId that = (DiscountProductId) o;

        if (promoId != null ? !promoId.equals(that.promoId) : that.promoId != null) return false;
        return productId != null ? productId.equals(that.productId) : that.productId == null;
    }

    @Override
    public int hashCode() {
        int result = promoId != null ? promoId.hashCode() : 0;
        result = 31 * result + (productId != null ? productId.hashCode() : 0);
        return result;
    }
} 