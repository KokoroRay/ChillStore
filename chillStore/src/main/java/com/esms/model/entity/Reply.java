package com.esms.model.entity;

import com.esms.model.entity.Admin;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name ="replies")
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="reply_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;
    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = true)
    private Admin admin;

    @Column(name = "content", length = 1000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Reply() {
    }

    public Reply(int id, Feedback feedback, Staff staff, Admin admin, String content, LocalDateTime createdAt) {
        this.id = id;
        this.feedback = feedback;
        this.staff = staff;
        this.admin = admin;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
