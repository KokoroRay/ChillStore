package com.esms.service;

import com.esms.model.dto.FeedbackDTO;
import com.esms.model.entity.Feedback;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {
    List<FeedbackDTO> getAllFeedbacks();
    List<Feedback> getFeedbacksByProductId(Integer productId);
    Page<FeedbackDTO> getFeedbacksByProductIdPaged(Integer productId, Pageable pageable);
    Page<FeedbackDTO> getFeedbacksByProductIdAndRatingPaged(Integer productId, Byte rating, Pageable pageable);
    Feedback getFeedbackByCustomerAndProduct(Integer customerId, Integer productId);
    Feedback addFeedback(Integer customerId, Integer productId, Byte rating, String comment);
    Feedback updateFeedback(Integer feedbackId, Byte rating, String comment, Integer customerId);
    void deleteFeedback(Integer feedbackId, Integer customerId);
}
