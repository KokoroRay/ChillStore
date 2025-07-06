package com.esms.model.dto;

import com.esms.model.entity.Customer;

import java.time.LocalDateTime;

public class FeedbackDto {
    private int id;
    private Customer customer;
    private String product;
    private Byte rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;

    public FeedbackDto() {

    }

    public FeedbackDto(int id, Customer customer, String product, Byte rating, String comment, String status, LocalDateTime createdAt) {
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
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