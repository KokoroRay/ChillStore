package com.esms.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity class cho bảng Discount (thay thế cho Promotion)
 * Chứa thông tin về các chương trình khuyến mãi/giảm giá
 *
 * @author ChillStore Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "promotions") // Giữ nguyên tên bảng trong database
public class Discount {

    // ==================== PRIMARY KEY ====================
    /**
     * ID chính của discount, tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_id")
    private Integer promoId; // ID của discount

    // ==================== BASIC INFORMATION ====================
    /**
     * Mã code của discount (ví dụ: SUMMER2024, NEWUSER20)
     * Bắt buộc phải có, tối đa 50 ký tự
     */
    @Column(name = "code", nullable = false, length = 50)
    private String code; // Mã code của discount

    /**
     * Mô tả chi tiết về discount
     * Có thể để trống, tối đa 255 ký tự
     */
    @Column(name = "description", length = 255)
    private String description; // Mô tả chi tiết về discount

    // ==================== DISCOUNT DETAILS ====================
    /**
     * Phần trăm giảm giá (ví dụ: 20.00 = 20%)
     * Bắt buộc phải có, độ chính xác 5 chữ số, 2 chữ số thập phân
     */
    @Column(name = "discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPct; // Phần trăm giảm giá

    // ==================== TIME PERIOD ====================
    /**
     * Ngày bắt đầu áp dụng discount
     * Bắt buộc phải có
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Ngày bắt đầu áp dụng

    /**
     * Ngày kết thúc áp dụng discount
     * Bắt buộc phải có
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // Ngày kết thúc áp dụng

    // ==================== STATUS ====================
    /**
     * Trạng thái hoạt động của discount
     * true: đang hoạt động, false: không hoạt động
     */
    @Column(name = "active")
    private Boolean active; // Trạng thái hoạt động

    // ==================== CREATOR INFORMATION ====================
    /**
     * ID của admin tạo discount
     * Bắt buộc phải có
     */
    @Column(name = "created_by", nullable = false)
    private Integer createdBy; // ID của admin tạo discount

    // ==================== APPLICATION TYPE ====================
    /**
     * Loại áp dụng discount
     * Có thể là: "product", "brand", "category"
     * Tối đa 20 ký tự
     */
    @Column(name = "apply_type", length = 20)
    private String applyType; // Loại áp dụng: product, brand, category

    // ==================== RELATIONSHIPS ====================
    /**
     * Quan hệ với Admin (người tạo discount)
     * FetchType.LAZY: chỉ load khi cần thiết
     * insertable=false, updatable=false: không thay đổi created_by qua quan hệ này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Admin admin;

    /**
     * Quan hệ với danh sách sản phẩm được áp dụng discount
     * mappedBy="discount": trỏ đến field discount trong DiscountProduct
     * cascade=CascadeType.ALL: khi xóa discount thì xóa luôn DiscountProduct
     * fetch=FetchType.LAZY: chỉ load khi cần thiết
     */
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiscountProduct> discountProducts;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructor mặc định (không tham số)
     * Cần thiết cho JPA
     */
    public Discount() {
        // Constructor mặc định
    }

    /**
     * Constructor đầy đủ tham số
     *
     * @param promoId     ID của discount
     * @param code        Mã code discount
     * @param description Mô tả discount
     * @param discountPct Phần trăm giảm giá
     * @param startDate   Ngày bắt đầu
     * @param endDate     Ngày kết thúc
     * @param active      Trạng thái hoạt động
     * @param createdBy   ID admin tạo
     * @param applyType   Loại áp dụng
     */
    public Discount(Integer promoId, String code, String description, BigDecimal discountPct,
                    LocalDate startDate, LocalDate endDate, Boolean active, Integer createdBy, String applyType) {
        this.promoId = promoId;
        this.code = code;
        this.description = description;
        this.discountPct = discountPct;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.createdBy = createdBy;
        this.applyType = applyType;
    }

    // ==================== GETTERS ====================

    /**
     * Lấy ID của discount
     *
     * @return ID của discount
     */
    public Integer getPromoId() {
        return promoId;
    }

    /**
     * Lấy mã code của discount
     *
     * @return Mã code discount
     */
    public String getCode() {
        return code;
    }

    /**
     * Lấy mô tả của discount
     *
     * @return Mô tả discount
     */
    public String getDescription() {
        return description;
    }

    /**
     * Lấy phần trăm giảm giá
     *
     * @return Phần trăm giảm giá
     */
    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    /**
     * Lấy ngày bắt đầu áp dụng
     *
     * @return Ngày bắt đầu
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Lấy ngày kết thúc áp dụng
     *
     * @return Ngày kết thúc
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Lấy trạng thái hoạt động
     *
     * @return Trạng thái hoạt động
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Lấy ID của admin tạo discount
     *
     * @return ID admin tạo
     */
    public Integer getCreatedBy() {
        return createdBy;
    }

    /**
     * Lấy loại áp dụng discount
     *
     * @return Loại áp dụng
     */
    public String getApplyType() {
        return applyType;
    }

    /**
     * Lấy thông tin admin tạo discount
     *
     * @return Đối tượng Admin
     */
    public Admin getAdmin() {
        return admin;
    }

    /**
     * Lấy danh sách sản phẩm được áp dụng discount
     *
     * @return Danh sách DiscountProduct
     */
    public List<DiscountProduct> getDiscountProducts() {
        return discountProducts;
    }

    // ==================== SETTERS ====================

    /**
     * Thiết lập ID của discount
     *
     * @param promoId ID của discount
     */
    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    /**
     * Thiết lập mã code của discount
     *
     * @param code Mã code discount
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Thiết lập mô tả của discount
     *
     * @param description Mô tả discount
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Thiết lập phần trăm giảm giá
     *
     * @param discountPct Phần trăm giảm giá
     */
    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    /**
     * Thiết lập ngày bắt đầu áp dụng
     *
     * @param startDate Ngày bắt đầu
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Thiết lập ngày kết thúc áp dụng
     *
     * @param endDate Ngày kết thúc
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Thiết lập trạng thái hoạt động
     *
     * @param active Trạng thái hoạt động
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Thiết lập ID của admin tạo discount
     *
     * @param createdBy ID admin tạo
     */
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Thiết lập loại áp dụng discount
     *
     * @param applyType Loại áp dụng
     */
    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    /**
     * Thiết lập thông tin admin tạo discount
     *
     * @param admin Đối tượng Admin
     */
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    /**
     * Thiết lập danh sách sản phẩm được áp dụng discount
     *
     * @param discountProducts Danh sách DiscountProduct
     */
    public void setDiscountProducts(List<DiscountProduct> discountProducts) {
        this.discountProducts = discountProducts;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Kiểm tra xem discount có đang hoạt động không
     *
     * @return true nếu discount đang hoạt động và trong thời gian hiệu lực
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return Boolean.TRUE.equals(active) &&
                !today.isBefore(startDate) &&
                !today.isAfter(endDate);
    }

    /**
     * Kiểm tra xem discount có hết hạn chưa
     *
     * @return true nếu discount đã hết hạn
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Kiểm tra xem discount có chưa bắt đầu chưa
     *
     * @return true nếu discount chưa bắt đầu
     */
    public boolean isNotStarted() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Tạo chuỗi biểu diễn của đối tượng Discount
     *
     * @return Chuỗi thông tin discount
     */
    @Override
    public String toString() {
        return "Discount{" +
                "promoId=" + promoId +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", discountPct=" + discountPct +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                ", createdBy=" + createdBy +
                ", applyType='" + applyType + '\'' +
                '}';
    }
} 