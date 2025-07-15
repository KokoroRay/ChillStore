package com.esms.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto tăng
    private Integer transId; //Mã giao dịch kho(PK)

    @ManyToOne(fetch = FetchType.EAGER) // Liên kết đến product, luôn load ngay
    @JoinColumn(name = "product_id", nullable = false)
// Chỉ rõ rằng cột product_id là (FK) and mỗi giao dịch phải gắn 1 product
    private Product product; //Sản phẩm liên quan đến giao dịch kho

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange; //Số lượng thay đổi (+ or - tồn kho)

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter; // Số lượng tồn kho sau giao dịch

    @Column(name = "type", nullable = false, length = 10)
    private String type; //Loại giao dịch

    @Column(name = "trans_date")
    private LocalDateTime transDate; //Thời điểm thực hiện giao dịch kho

    @ManyToOne(fetch = FetchType.EAGER)// Liên kết đến Admin xử lý (Có thể null)
    @JoinColumn(name = "admin_id")
    private Admin admin; //Người thực hiện giao dịch

    @Column(name = "notes", length = 255)
    private String notes; //Ghi chú tùy chọn về giao dịch

    public Warehouse() {
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    //update
}
