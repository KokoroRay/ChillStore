package com.esms.model.entity;

import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import java.time.LocalDateTime;

@Entity
@Table(name ="feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int id;

    @ManyToOne
    @JoinColumn (name ="customer_id", nullable = false)
    private Customer customer;
    @ManyToOne
    @JoinColumn (name ="product_id", nullable = false)
    private Product product;

    private Byte rating;
    private String comment;
    private String status;

    @Column(name ="created_at")
    private LocalDateTime createdAt;

    public Feedback() {

    }
    public Feedback(int id, Customer customer, Product product, Byte rating, String comment, String status, LocalDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.product = product;
        this.rating = rating;
        this.comment = comment;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Byte getRating() {
        return rating;
    }

    public void setRating(Byte rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
