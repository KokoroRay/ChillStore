package com.esms.model.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class CustomerOrderDetailDTO {
    private Integer orderId;
    private Date orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private BigDecimal discountAmount;
    private String refundStatus;
    private List<OrderItemDetailDTO> items;

    // Customer information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<OrderItemDetailDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetailDTO> items) {
        this.items = items;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public BigDecimal getShippingCost() {
        return calculateShippingCost(this.customerAddress);
    }

    private BigDecimal calculateShippingCost(String address) {
        if (address == null || address.trim().isEmpty()) {
            return BigDecimal.valueOf(20000); // Default shipping cost
        }

        String addressLower = address.toLowerCase();

        // Free shipping for Cần Thơ
        if (addressLower.contains("cần thơ") || addressLower.contains("can tho")) {
            return BigDecimal.ZERO;
        }

        // List of northern provinces
        String[] northernProvinces = {
                "hà nội", "hanoi", "hải phòng", "haiphong", "bắc ninh", "bắc giang", "lào cai",
                "lao cai", "điện biên", "dien bien", "hòa bình", "hoa binh", "lai châu", "lai chau",
                "sơn la", "son la", "hà giang", "ha giang", "cao bằng", "cao bang", "bắc kạn", "bac kan",
                "lạng sơn", "lang son", "tuyên quang", "tuyen quang", "thái nguyên", "thai nguyen",
                "phú thọ", "phu tho", "vĩnh phúc", "vinh phuc", "quảng ninh", "quang ninh",
                "hải dương", "hai duong", "hưng yên", "hung yen", "thái bình", "thai binh",
                "hà nam", "ha nam", "nam định", "nam dinh", "ninh bình", "ninh binh", "thanh hóa", "thanh hoa"
        };

        // Check if province is in northern list
        for (String province : northernProvinces) {
            if (addressLower.contains(province)) {
                return BigDecimal.valueOf(40000); // Northern provinces
            }
        }

        return BigDecimal.valueOf(20000); // Other provinces
    }
}
