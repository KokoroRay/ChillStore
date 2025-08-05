package com.esms.service.impl;

import com.esms.model.dto.FeedbackDTO;
import com.esms.model.entity.Product;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Feedback;
import com.esms.repository.FeedbackRepository;
import com.esms.repository.ProductRepository;
import com.esms.repository.CustomerRepository;
import com.esms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<FeedbackDTO> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Feedback> getFeedbacksByProductId(Integer productId) {
        return feedbackRepository.findByProductProductId(productId);
    }

    @Override
    public Page<FeedbackDTO> getFeedbacksByProductIdPaged(Integer productId, Pageable pageable) {
        Page<Feedback> feedbackPage = feedbackRepository.findByProductProductId(productId, pageable);
        return feedbackPage.map(this::convertToDTO);
    }

    @Override
    public Page<FeedbackDTO> getFeedbacksByProductIdAndRatingPaged(Integer productId, Byte rating, Pageable pageable) {
        Page<Feedback> feedbackPage = feedbackRepository.findByProductProductIdAndRating(productId, rating, pageable);
        return feedbackPage.map(this::convertToDTO);
    }

    @Override
    public Feedback getFeedbackByCustomerAndProduct(Integer customerId, Integer productId) {
        return feedbackRepository.findByCustomerCustomerIdAndProductProductId(customerId, productId).orElse(null);
    }

    @Override
    @Transactional
    public Feedback addFeedback(Integer customerId, Integer productId, Byte rating, String comment) {
        if (feedbackRepository.findByCustomerCustomerIdAndProductProductId(customerId, productId).isPresent()) {
            throw new RuntimeException("You have already submitted feedback for this product!");
        }
        if (comment == null || comment.length() > 500) {
            throw new RuntimeException("Feedback content must not exceed 500 characters!");
        }
        if (rating == null) {
            throw new RuntimeException("You must select a rating!");
        }
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found!"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found!"));
        Feedback feedback = new Feedback();
        feedback.setCustomer(customer);
        feedback.setProduct(product);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setStatus("New");
        feedback.setCreatedAt(java.time.LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public Feedback updateFeedback(Integer feedbackId, Byte rating, String comment, Integer customerId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new RuntimeException("Không tìm thấy feedback!"));
        if (!feedback.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền sửa feedback này!");
        }
        feedback.setRating(rating);
        feedback.setComment(comment);
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(Integer feedbackId, Integer customerId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new RuntimeException("Không tìm thấy feedback!"));
        if (!feedback.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền xóa feedback này!");
        }
        feedbackRepository.delete(feedback);
    }

    private FeedbackDTO convertToDTO(Feedback fb) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(fb.getId());
        dto.setCustomer(fb.getCustomer());
        dto.setProduct(fb.getProduct().getName());
        dto.setRating(fb.getRating());
        dto.setComment(fb.getComment());
        dto.setStatus(fb.getStatus());
        dto.setCreatedAt(fb.getCreatedAt());
        return dto;
    }
}

