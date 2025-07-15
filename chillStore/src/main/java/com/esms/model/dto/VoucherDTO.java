package com.esms.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class VoucherDTO {

    private Integer voucher_id;

    @NotBlank(message = "code không để trống")
    @Size(max = 50, message = "Code khong dai hon 50 ki tu")
    private String code;

    @Size(max = 255, message = "Description khong dai hon 255 ki tu")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "discount amount phai lon hon 0")
    private BigDecimal discount_amount;

    @DecimalMin(value = "0.0", inclusive = false, message = "discount percent phai lon hon 0")
    private BigDecimal discount_pct;

    @DecimalMin(value = "0.0", message = "min order amount phai lon hon 0")
    private BigDecimal min_order_amount = BigDecimal.ZERO;

    @Min(value = 0, message = "so luong phai lon hon 0")
    private Integer quantity_available = 0;

    @NotNull(message = "phai bat dau khong duoc phet null")
    private LocalDate start_date;

    @NotNull(message = "ngay ket thuc khong duoc phet null")
    private LocalDate end_date;

    private boolean active;

    private List<Integer> categoryIds;
    private List<Integer> brandIds;

    public Integer getVoucher_id() {
        return voucher_id;
    }

    public void setVoucher_id(Integer voucher_id) {
        this.voucher_id = voucher_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(BigDecimal discount_amount) {
        this.discount_amount = discount_amount;
    }

    public BigDecimal getDiscount_pct() {
        return discount_pct;
    }

    public void setDiscount_pct(BigDecimal discount_pct) {
        this.discount_pct = discount_pct;
    }

    public BigDecimal getMin_order_amount() {
        return min_order_amount;
    }

    public void setMin_order_amount(BigDecimal min_order_amount) {
        this.min_order_amount = min_order_amount;
    }

    public Integer getQuantity_available() {
        return quantity_available;
    }

    public void setQuantity_available(Integer quantity_available) {
        this.quantity_available = quantity_available;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Integer> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Integer> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<Integer> getBrandIds() {
        return brandIds;
    }

    public void setBrandIds(List<Integer> brandIds) {
        this.brandIds = brandIds;
    }
}
