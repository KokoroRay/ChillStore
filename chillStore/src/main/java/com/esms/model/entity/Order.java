package com.esms.model.entity;

import jakarta.persistence.*;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "is_voucher_acquisition")
    private boolean isVoucherAcquisition;

    public Order() {
    }

    public Order(Integer orderId, Customer customer, BigDecimal discountAmount, Date orderDate, BigDecimal totalAmount, String status, String paymentMethod) {
        this.orderId = orderId;
        this.customer = customer;
        this.discountAmount = discountAmount;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    // Xử lý hoàn tiền khi customer thanh toán bằng VNPay nhưng hủy đơn hàng
    @Transient
    public String getRefundStatus() {
        if ("Cancelled".equals(this.status) && "VNpay".equals(this.paymentMethod)) {
            if (discountAmount == null || discountAmount.intValue() == 0) return "pending_refund";
            return discountAmount.intValue() == 2 ? "refunded" : "pending_refund";
        }
        return null;
    }

    public void setRefundStatus(String refundStatus) {
        if ("VNpay".equals(this.paymentMethod)) {
            switch (refundStatus) {
                case "pending_refund":
                    this.discountAmount = BigDecimal.ONE;
                    break;
                case "refunded":
                    this.discountAmount = new BigDecimal(2);
                    break;
                default:
                    this.discountAmount = BigDecimal.ONE;
            }
        }
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public boolean isVoucherAcquisition() {
        return isVoucherAcquisition;
    }

    public void setVoucherAcquisition(boolean voucherAcquisition) {
        isVoucherAcquisition = voucherAcquisition;
    }
}