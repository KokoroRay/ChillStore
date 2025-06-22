package com.esms.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO class cho Discount
 * Chứa thông tin về discount để hiển thị và xử lý
 * 
 * @author ChillStore Team
 * @version 1.0
 * @since 2024
 */
public class DiscountDto {
    
    // ==================== BASIC INFORMATION ====================
    /**
     * ID của discount
     */
    private Integer promoId; // ID của discount
    
    /**
     * Mã code của discount (ví dụ: SUMMER2024, NEWUSER20)
     */
    private String code; // Mã code của discount
    
    /**
     * Mô tả chi tiết về discount
     */
    private String description; // Mô tả chi tiết về discount
    
    // ==================== DISCOUNT DETAILS ====================
    /**
     * Phần trăm giảm giá (ví dụ: 20.00 = 20%)
     */
    private BigDecimal discountPct; // Phần trăm giảm giá
    
    // ==================== TIME PERIOD ====================
    /**
     * Ngày bắt đầu áp dụng discount
     */
    private LocalDate startDate; // Ngày bắt đầu áp dụng
    
    /**
     * Ngày kết thúc áp dụng discount
     */
    private LocalDate endDate; // Ngày kết thúc áp dụng
    
    // ==================== STATUS ====================
    /**
     * Trạng thái hoạt động của discount
     * true: đang hoạt động, false: không hoạt động
     */
    private Boolean active; // Trạng thái hoạt động
    
    // ==================== CREATOR INFORMATION ====================
    /**
     * ID của admin tạo discount
     */
    private Integer createdBy; // ID của admin tạo discount
    
    /**
     * Tên admin tạo discount (để hiển thị)
     */
    private String adminName; // Tên admin tạo discount
    
    // ==================== APPLICATION TYPE ====================
    /**
     * Loại áp dụng discount
     * Có thể là: "product", "brand", "category"
     */
    private String applyType; // Loại áp dụng: product, brand, category
    
    // ==================== PRODUCT INFORMATION ====================
    /**
     * Danh sách tên sản phẩm được áp dụng discount (để hiển thị)
     */
    private List<String> productNames; // Danh sách tên sản phẩm được áp dụng
    
    /**
     * Số lượng sản phẩm được áp dụng discount
     */
    private Integer productCount; // Số lượng sản phẩm được áp dụng
    
    // ==================== SELECTION FIELDS (for form) ====================
    /**
     * Danh sách ID sản phẩm được chọn (dùng cho form)
     */
    private List<Integer> productIds; // Danh sách ID sản phẩm được chọn
    
    /**
     * ID brand được chọn (dùng cho form)
     */
    private Integer brandId; // ID brand được chọn
    
    /**
     * ID category được chọn (dùng cho form)
     */
    private Integer categoryId; // ID category được chọn
    
    // ==================== CONSTRUCTORS ====================
    /**
     * Constructor mặc định (không tham số)
     * Cần thiết cho Spring MVC
     */
    public DiscountDto() {
        // Constructor mặc định
    }
    
    /**
     * Constructor đầy đủ tham số
     * @param promoId ID của discount
     * @param code Mã code discount
     * @param description Mô tả discount
     * @param discountPct Phần trăm giảm giá
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param active Trạng thái hoạt động
     * @param createdBy ID admin tạo
     * @param applyType Loại áp dụng
     * @param adminName Tên admin
     * @param productNames Danh sách tên sản phẩm
     * @param productCount Số lượng sản phẩm
     * @param productIds Danh sách ID sản phẩm
     * @param brandId ID brand
     * @param categoryId ID category
     */
    public DiscountDto(Integer promoId, String code, String description, BigDecimal discountPct,
                      LocalDate startDate, LocalDate endDate, Boolean active, Integer createdBy,
                      String applyType, String adminName, List<String> productNames, Integer productCount,
                      List<Integer> productIds, Integer brandId, Integer categoryId) {
        this.promoId = promoId;
        this.code = code;
        this.description = description;
        this.discountPct = discountPct;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.createdBy = createdBy;
        this.applyType = applyType;
        this.adminName = adminName;
        this.productNames = productNames;
        this.productCount = productCount;
        this.productIds = productIds;
        this.brandId = brandId;
        this.categoryId = categoryId;
    }
    
    // ==================== GETTERS ====================
    /**
     * Lấy ID của discount
     * @return ID của discount
     */
    public Integer getPromoId() {
        return promoId;
    }
    
    /**
     * Lấy mã code của discount
     * @return Mã code discount
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Lấy mô tả của discount
     * @return Mô tả discount
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Lấy phần trăm giảm giá
     * @return Phần trăm giảm giá
     */
    public BigDecimal getDiscountPct() {
        return discountPct;
    }
    
    /**
     * Lấy ngày bắt đầu áp dụng
     * @return Ngày bắt đầu
     */
    public LocalDate getStartDate() {
        return startDate;
    }
    
    /**
     * Lấy ngày kết thúc áp dụng
     * @return Ngày kết thúc
     */
    public LocalDate getEndDate() {
        return endDate;
    }
    
    /**
     * Lấy trạng thái hoạt động
     * @return Trạng thái hoạt động
     */
    public Boolean getActive() {
        return active;
    }
    
    /**
     * Lấy ID của admin tạo discount
     * @return ID admin tạo
     */
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    /**
     * Lấy tên admin tạo discount
     * @return Tên admin
     */
    public String getAdminName() {
        return adminName;
    }
    
    /**
     * Lấy loại áp dụng discount
     * @return Loại áp dụng
     */
    public String getApplyType() {
        return applyType;
    }
    
    /**
     * Lấy danh sách tên sản phẩm được áp dụng
     * @return Danh sách tên sản phẩm
     */
    public List<String> getProductNames() {
        return productNames;
    }
    
    /**
     * Lấy số lượng sản phẩm được áp dụng
     * @return Số lượng sản phẩm
     */
    public Integer getProductCount() {
        return productCount;
    }
    
    /**
     * Lấy danh sách ID sản phẩm được chọn
     * @return Danh sách ID sản phẩm
     */
    public List<Integer> getProductIds() {
        return productIds;
    }
    
    /**
     * Lấy ID brand được chọn
     * @return ID brand
     */
    public Integer getBrandId() {
        return brandId;
    }
    
    /**
     * Lấy ID category được chọn
     * @return ID category
     */
    public Integer getCategoryId() {
        return categoryId;
    }
    
    // ==================== SETTERS ====================
    /**
     * Thiết lập ID của discount
     * @param promoId ID của discount
     */
    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
    
    /**
     * Thiết lập mã code của discount
     * @param code Mã code discount
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Thiết lập mô tả của discount
     * @param description Mô tả discount
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Thiết lập phần trăm giảm giá
     * @param discountPct Phần trăm giảm giá
     */
    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }
    
    /**
     * Thiết lập ngày bắt đầu áp dụng
     * @param startDate Ngày bắt đầu
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    /**
     * Thiết lập ngày kết thúc áp dụng
     * @param endDate Ngày kết thúc
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    /**
     * Thiết lập trạng thái hoạt động
     * @param active Trạng thái hoạt động
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    /**
     * Thiết lập ID của admin tạo discount
     * @param createdBy ID admin tạo
     */
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }
    
    /**
     * Thiết lập tên admin tạo discount
     * @param adminName Tên admin
     */
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * Thiết lập loại áp dụng discount
     * @param applyType Loại áp dụng
     */
    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }
    
    /**
     * Thiết lập danh sách tên sản phẩm được áp dụng
     * @param productNames Danh sách tên sản phẩm
     */
    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }
    
    /**
     * Thiết lập số lượng sản phẩm được áp dụng
     * @param productCount Số lượng sản phẩm
     */
    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }
    
    /**
     * Thiết lập danh sách ID sản phẩm được chọn
     * @param productIds Danh sách ID sản phẩm
     */
    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }
    
    /**
     * Thiết lập ID brand được chọn
     * @param brandId ID brand
     */
    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
    
    /**
     * Thiết lập ID category được chọn
     * @param categoryId ID category
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    // ==================== UTILITY METHODS ====================
    /**
     * Kiểm tra xem discount có đang hoạt động không
     * @return true nếu discount đang hoạt động và trong thời gian hiệu lực
     */
    public boolean isCurrentlyActive() {
        if (active == null || !active) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
    
    /**
     * Kiểm tra xem discount có hết hạn chưa
     * @return true nếu discount đã hết hạn
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }
    
    /**
     * Kiểm tra xem discount có chưa bắt đầu chưa
     * @return true nếu discount chưa bắt đầu
     */
    public boolean isNotStarted() {
        return LocalDate.now().isBefore(startDate);
    }
    
    /**
     * Tạo chuỗi biểu diễn của đối tượng DiscountDto
     * @return Chuỗi thông tin discount
     */
    @Override
    public String toString() {
        return "DiscountDto{" +
                "promoId=" + promoId +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", discountPct=" + discountPct +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                ", createdBy=" + createdBy +
                ", applyType='" + applyType + '\'' +
                ", adminName='" + adminName + '\'' +
                ", productCount=" + productCount +
                ", brandId=" + brandId +
                ", categoryId=" + categoryId +
                '}';
    }
} 