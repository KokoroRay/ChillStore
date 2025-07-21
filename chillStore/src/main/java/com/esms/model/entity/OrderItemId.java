package com.esms.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable {
    @Column(name = "order_id")
    private Integer orderId;
    
    @Column(name = "product_id")
    private Integer productId;


    public OrderItemId() {}
    public OrderItemId(Integer orderId, Integer productId) {
        this.orderId = orderId;
        this.productId = productId;
    }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(orderId, productId);
    }
} 