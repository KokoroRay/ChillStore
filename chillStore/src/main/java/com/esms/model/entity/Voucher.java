package com.esms.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers")

public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id", nullable = false)
    private Integer voucher_id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "discount_amount")
    private BigDecimal discount_amount;

    @Column(name = "discount_pct")
    private BigDecimal discount_pct;

    @Column(name = "min_order_amount", nullable = false)
    private BigDecimal min_order_amount = BigDecimal.ZERO;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantity_available = 0;

    @Column(name = "start_date")
    private LocalDate start_date;

    @Column(name = "end_date")
    private LocalDate end_date;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceated_by")
    private Admin created_by;

    @Column(name = "created_at", nullable = false)
    private LocalDate created_at;

    @PrePersist
    protected void prePersist() {
        created_at = LocalDate.now();
    }

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

    public Integer Quantity_available() {
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

    public Admin getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Admin created_by) {
        this.created_by = created_by;
    }

    public LocalDate getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }
}
