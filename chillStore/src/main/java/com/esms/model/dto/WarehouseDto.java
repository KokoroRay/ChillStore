package com.esms.model.dto;

import java.time.LocalDateTime;

/**
 * WarehouseDTO là Data Transfer Object (DTO),
 * được sử dụng để truyền dữ liệu warehouse giữa các tầng khác nhau (Controller, Service, Repository).
 */

public class WarehouseDto {
    private Integer transId;
    private String productId;//Id product liên quan đến giao dịch
    private String productName;//name product
    private Integer quantityChange; //Quantity thay đổi trong giao dịch
    private Integer stockAfter;// Số Product tồn kho
    private String type; //Loại giao dịch kho
    private LocalDateTime transDate; //Ngay giờ thực hiện giao dịch
    private Integer adminId; //ID Admin thực hiện giao dịch
    private String adminName; //Name admin thực hiện giao dịch
    private String notes; //Ghi chú

    public WarehouseDto() {
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTransDate() {
        return transDate;
    }

    public void setTransDate(LocalDateTime transDate) {
        this.transDate = transDate;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    //update
}
