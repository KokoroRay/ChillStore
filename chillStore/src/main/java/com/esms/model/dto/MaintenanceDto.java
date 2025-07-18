package com.esms.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MaintenanceDto {
    // Getters and Setters
    private Integer requestId;
    
    private Integer orderId;
    
    private Integer productId;
    
    private Integer customerId;
    
    private Integer staffId;
    
    private String staffName;
    
    @NotBlank(message = "Request type is required")
    private String requestType;
    
    @NotNull(message = "Maintenance date is required")
    @FutureOrPresent(message = "Maintenance date must be today or in the future")
    private LocalDateTime requestDate;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String status;

    // Additional fields for display
    private String orderInfo;
    private String productInfo;
    private String customerInfo;

    // Constructors
    public MaintenanceDto() {}

    public MaintenanceDto(Integer requestId, Integer orderId, Integer productId, Integer customerId, Integer staffId, String requestType, LocalDateTime requestDate, String reason, String status) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.staffId = staffId;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.reason = reason;
        this.status = status;
    }

    public MaintenanceDto(Integer requestId, Integer orderId, Integer productId, Integer customerId, Integer staffId, String requestType, LocalDateTime requestDate, String reason, String status, String orderInfo, String productInfo, String customerInfo, String staffName) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.staffId = staffId;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.reason = reason;
        this.status = status;
        this.orderInfo = orderInfo;
        this.productInfo = productInfo;
        this.customerInfo = customerInfo;
        this.staffName = staffName;
    }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

}
