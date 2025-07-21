package com.esms.model.dto;

import java.math.BigDecimal;
import java.util.Date;

public class OrderDTO {
    private Integer orderId;
    private String customerName;
    private BigDecimal discountAmount;
    private Date orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private int itemsCount;
    private String representativeProductName;
    private String representativeProductImage;
    private String customerEmail;
    private String customerPhone;

    public OrderDTO(Integer orderId, String customerName, BigDecimal discountAmount, Date orderDate, BigDecimal totalAmount, String status, String paymentMethod, int itemsCount) {
        this(orderId, customerName, discountAmount, orderDate, totalAmount, status, paymentMethod, itemsCount, null, null, null, null);
    }

    public OrderDTO(Integer orderId, String customerName, BigDecimal discountAmount, Date orderDate, BigDecimal totalAmount, String status, String paymentMethod, int itemsCount, String representativeProductName, String representativeProductImage) {
        this(orderId, customerName, discountAmount, orderDate, totalAmount, status, paymentMethod, itemsCount, representativeProductName, representativeProductImage, null, null);
    }

    public OrderDTO(Integer orderId, String customerName, BigDecimal discountAmount, Date orderDate, BigDecimal totalAmount, String status, String paymentMethod, int itemsCount, String representativeProductName, String representativeProductImage, String customerEmail, String customerPhone) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.discountAmount = discountAmount;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.itemsCount = itemsCount;
        this.representativeProductName = representativeProductName;
        this.representativeProductImage = representativeProductImage;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
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

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getRepresentativeProductName() {
        return representativeProductName;
    }

    public void setRepresentativeProductName(String representativeProductName) {
        this.representativeProductName = representativeProductName;
    }

    public String getRepresentativeProductImage() {
        return representativeProductImage;
    }

    public void setRepresentativeProductImage(String representativeProductImage) {
        this.representativeProductImage = representativeProductImage;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    // Method to get refund status for VNPay cancelled orders
    public String getRefundStatus() {
        if ("Cancelled".equals(this.status) && "VNpay".equals(this.paymentMethod)) {
            if (discountAmount == null || discountAmount.intValue() == 0) return "pending_refund";
            return discountAmount.intValue() == 2 ? "refunded" : "pending_refund";
        }
        return null;
    }
    
    // Method to get display status (convert Paid to Confirmed for display)
    public String getDisplayStatus() {
        return "Paid".equals(this.status) ? "Confirmed" : this.status;
    }
} 