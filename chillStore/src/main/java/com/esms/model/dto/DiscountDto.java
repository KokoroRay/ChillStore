package com.esms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO class cho Discount
 * Chứa thông tin về discount để hiển thị và xử lý
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDto {
    
    private Integer promoId; // ID của discount
    private String code; // Mã code của discount
    private String description; // Mô tả chi tiết về discount
    private BigDecimal discountPct; // Phần trăm giảm giá
    private LocalDate startDate; // Ngày bắt đầu áp dụng
    private LocalDate endDate; // Ngày kết thúc áp dụng
    private Boolean active; // Trạng thái hoạt động
    private Integer createdBy; // ID của admin tạo discount
    private String applyType; // Loại áp dụng: product, brand, category
    private String adminName; // Tên admin tạo discount
    private List<String> productNames; // Danh sách tên sản phẩm được áp dụng
    private Integer productCount; // Số lượng sản phẩm được áp dụng
    
    // Các field bổ sung cho việc tạo/chỉnh sửa
    private List<Integer> productIds; // Danh sách ID sản phẩm được chọn
    private Integer brandId; // ID brand được chọn
    private Integer categoryId; // ID category được chọn
} 