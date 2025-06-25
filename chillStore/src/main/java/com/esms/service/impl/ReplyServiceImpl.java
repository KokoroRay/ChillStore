package com.esms.service.impl;

import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.model.entity.Feedback;
import com.esms.model.entity.Reply;
import com.esms.model.entity.Staff;
import com.esms.repository.FeedbackRepository;
import com.esms.repository.ReplyRepository;
import com.esms.repository.StaffRepository;
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

    @Override
    public void saveReply(ReplyFeedbackDTO dto) {
        Feedback feedback = feedbackRepository.findById(dto.getFeedbackId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy feedback"));
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff"));

        Reply reply = new Reply();
        reply.setFeedback(feedback);
        reply.setStaff(staff);
        reply.setContent(dto.getContent());
        reply.setCreatedAt(LocalDateTime.now());

        replyRepository.save(reply);

        feedback.setStatus("Replied");
        feedbackRepository.save(feedback);
    }
}

