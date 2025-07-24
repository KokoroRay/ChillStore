package com.esms.service.impl;

import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.model.entity.Feedback;
import com.esms.model.entity.Reply;
import com.esms.model.entity.Staff;
import com.esms.model.entity.Admin;
import com.esms.repository.FeedbackRepository;
import com.esms.repository.ReplyRepository;
import com.esms.repository.StaffRepository;
import com.esms.repository.AdminRepository;
import com.esms.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReplyServiceImpl implements ReplyService {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AdminRepository adminRepository;

    public AdminRepository getAdminRepository() {
        return adminRepository;
    }
    public StaffRepository getStaffRepository() {
        return staffRepository;
    }

    @Override
    public void saveReply(ReplyFeedbackDTO dto) {
        Feedback feedback = feedbackRepository.findById(dto.getFeedbackId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy feedback"));
        Reply reply = new Reply();
        reply.setFeedback(feedback);
        reply.setContent(dto.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        if (dto.getStaffId() > 0) {
            Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff"));
            reply.setStaff(staff);
            reply.setAdmin(null);
        } else if (dto.getAdminId() > 0) {
            Admin admin = adminRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));
            reply.setAdmin(admin);
            reply.setStaff(null);
        } else {
            throw new RuntimeException("Thiếu thông tin người trả lời");
        }
        replyRepository.save(reply);
        feedback.setStatus("Replied");
        feedbackRepository.save(feedback);
    }

    @Override
    public Reply getReplyByFeedbackId(int feedbackId) {
        return replyRepository.findByFeedbackId(feedbackId).orElse(null);
    }

    @Override
    public Reply updateReply(int feedbackId, ReplyFeedbackDTO dto, int staffId) {
        Reply reply = replyRepository.findByFeedbackId(feedbackId).orElseThrow(() -> new RuntimeException("Không tìm thấy reply"));
        if (reply.getStaff().getId() != staffId) {
            throw new RuntimeException("Bạn không có quyền sửa reply này!");
        }
        reply.setContent(dto.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        return replyRepository.save(reply);
    }
}

