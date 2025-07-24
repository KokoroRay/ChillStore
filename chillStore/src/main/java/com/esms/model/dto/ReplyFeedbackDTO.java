package com.esms.model.dto;

import com.esms.model.entity.Feedback;
import com.esms.model.entity.Staff;

import java.time.LocalDateTime;

public class ReplyFeedbackDTO {
    private int feedbackId;
    private Feedback feedback;
    private Staff staff;
    private String content;
    private LocalDateTime createdAt;
    private int staffId;
    private int adminId;


    public ReplyFeedbackDTO() {

    }

    public ReplyFeedbackDTO(int feedbackId, Feedback feedback, Staff staff, String content, LocalDateTime createdAt, int staffId, int adminId) {
        this.feedbackId = feedbackId;
        this.feedback = feedback;
        this.staff = staff;
        this.content = content;
        this.createdAt = createdAt;
        this.staffId = staffId;
        this.adminId = adminId;
    }

    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
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

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getAdminId() {
        return adminId;
    }
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
}
