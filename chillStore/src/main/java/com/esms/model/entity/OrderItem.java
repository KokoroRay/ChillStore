package com.esms.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price_each")
    private BigDecimal priceEach;

    public OrderItem() {}
    public OrderItem(OrderItemId id, int quantity, BigDecimal priceEach) {
        this.id = id;
        this.quantity = quantity;
        this.priceEach = priceEach;
    }
    public OrderItemId getId() { return id; }
    public void setId(OrderItemId id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPriceEach() { return priceEach; }
    public void setPriceEach(BigDecimal priceEach) { this.priceEach = priceEach; }
} 