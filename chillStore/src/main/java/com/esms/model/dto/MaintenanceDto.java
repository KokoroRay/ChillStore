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
    
    @NotBlank(message = "Request type is required")
    private String requestType;
    
    @NotNull(message = "Maintenance date is required")
    @FutureOrPresent(message = "Maintenance date must be today or in the future")
    private LocalDateTime requestDate;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String status;

    // Constructors
    public MaintenanceDto() {}

    public MaintenanceDto(Integer requestId, Integer orderId, Integer productId, Integer customerId, String requestType, LocalDateTime requestDate, String reason, String status) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.reason = reason;
        this.status = status;
    }

}
