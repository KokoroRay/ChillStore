package com.esms.model.dto;

import java.math.BigDecimal;
import java.util.Date;

public class OrderDto {
    private Integer orderId;
    private String customerName;
    private BigDecimal discountAmount;
    private Date orderDate;
    private Double totalAmount;
    private String status;
    private String paymentMethod;

    public OrderDto(Integer orderId, String customerName, BigDecimal discountAmount, Date orderDate, Double totalAmount, String status, String paymentMethod) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.discountAmount = discountAmount;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
} 