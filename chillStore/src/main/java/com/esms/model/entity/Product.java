package com.esms.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Entity đại diện cho bảng "Product" trong cơ sở dữ liệu.
 * Lưu thông tin về các sản phẩm có trong hệ thống.
 */
@Entity
@Table(name = "products") //Gắn class with table products
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //ID auto tăng
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "name")
    private String name; // Name product

    @Column(name = "description")
    private String description; // Mô tả chi tiết sản phẩm

    @Column(name = "price")
    private BigDecimal price; //price of the product(Dùng Bigdecimal để xác định số thapaj phân )

    @Column(name = "stock_qty")
    private Integer stockQty; // Quantity hàng tồn kho

    @Column(name = "active")
    private boolean status;

    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Quan hệ One-to-Many với bảng warehouse.
     * Một sản phẩm có thể liên kết với nhiều giao dịch trong kho (nhập/xuất).
     * mappedBy = "product" nghĩa là bảng Warehouse có trường `product` là khóa ngoại.
     */
    @OneToMany(mappedBy = "product")
    private List<Warehouse> warehouseTransactions;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Warehouse> getWarehouseTransactions() {
        return warehouseTransactions;
    }

    public void setWarehouseTransactions(List<Warehouse> warehouseTransactions) {
        this.warehouseTransactions = warehouseTransactions;
    }
}