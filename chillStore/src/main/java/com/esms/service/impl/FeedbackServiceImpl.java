package com.esms.service.impl;

import com.esms.model.dto.FeedbackDTO;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Feedback;
import com.esms.repository.CustomerRepository;
import com.esms.repository.FeedbackRepository;
import com.esms.repository.OrderItemRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.esms.model.entity.Product;
import com.esms.repository.ReplyRepository;
import com.esms.model.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @Override
    public List<FeedbackDTO> getAllFeedbacks() {
       /* return feedbackRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()); */
        return fakeData();

    }

    private List<FeedbackDTO> fakeData() {
        List<FeedbackDTO> list = new ArrayList<>();
        Customer customer = new Customer();
        customer.setName("Nguyễn Văn A");
        list.add(new FeedbackDTO(1, customer, "Máy lạnh", (byte) 5, "Sản phẩm rất tốt", "New", LocalDateTime.now()));
        list.add(new FeedbackDTO(2, customer, "Máy giặt", (byte) 4, "Tốt nhưng hơi ồn", "Replied", LocalDateTime.now().minusDays(1)));
        return list;
    }

    @Override
    public List<FeedbackDTO> getFeedbacksByProduct(int productId, Byte rating) {
        List<Feedback> feedbacks;
        if (rating == null) {
            feedbacks = feedbackRepository.findByProduct_ProductId(productId);
        } else {
            feedbacks = feedbackRepository.findByProduct_ProductIdAndRating(productId, rating);
        }
        List<FeedbackDTO> dtos = new ArrayList<>();
        for (Feedback fb : feedbacks) {
            dtos.add(convertToDTO(fb));
        }
        return dtos;
    }

    @Override
    public Page<FeedbackDTO> getFeedbacksByProductPaged(int productId, Byte rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Feedback> feedbacks;
        long total;
        if (rating == null) {
            var pageResult = feedbackRepository.findByProduct_ProductId(productId, pageable);
            feedbacks = pageResult.getContent();
            total = pageResult.getTotalElements();
        } else {
            feedbacks = feedbackRepository.findByProduct_ProductIdAndRating(productId, rating);
            total = feedbacks.size();
            // Tự phân trang thủ công nếu filter rating
            int from = Math.min(page * size, feedbacks.size());
            int to = Math.min(from + size, feedbacks.size());
            feedbacks = feedbacks.subList(from, to);
        }
        List<FeedbackDTO> dtos = new ArrayList<>();
        for (Feedback fb : feedbacks) {
            dtos.add(convertToDTO(fb));
        }
        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public FeedbackDTO addFeedback(int customerId, int productId, Byte rating, String comment) {
        // Rule 1: Chỉ cho phép khách đã mua hàng mới feedback
        boolean hasBought = orderItemRepository.existsByCustomerAndProduct(customerId, productId);
        if (!hasBought) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm đã mua!");
        }
        // Rule 2: Mỗi khách chỉ feedback 1 lần cho 1 sản phẩm
        boolean hasFeedback = feedbackRepository.existsByCustomer_CustomerIdAndProduct_ProductId(customerId, productId);
        if (hasFeedback) {
            throw new RuntimeException("Bạn chỉ được đánh giá 1 lần cho mỗi sản phẩm!");
        }
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        Feedback feedback = new Feedback();
        feedback.setCustomer(customer);
        feedback.setProduct(product);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setStatus("New");
        feedback.setCreatedAt(java.time.LocalDateTime.now());
        Feedback saved = feedbackRepository.save(feedback);
        return convertToDTO(saved);
    }

    @Override
    public FeedbackDTO updateFeedback(int customerId, int productId, Byte rating, String comment) {
        Feedback feedback = feedbackRepository.findByCustomer_CustomerIdAndProduct_ProductId(customerId, productId);
        if (feedback == null) throw new RuntimeException("Không tìm thấy feedback để sửa!");
        feedback.setRating(rating);
        feedback.setComment(comment);
        Feedback saved = feedbackRepository.save(feedback);
        return convertToDTO(saved);
    }

    @Override
    public void deleteFeedback(int customerId, int productId) {
        Feedback feedback = feedbackRepository.findByCustomer_CustomerIdAndProduct_ProductId(customerId, productId);
        if (feedback == null) throw new RuntimeException("Không tìm thấy feedback để xóa!");
        feedbackRepository.delete(feedback);
    }

    @Override
    public java.util.Map<String, Object> getFeedbackStats(int productId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        long total = feedbackRepository.countByProduct_ProductId(productId);
        double avg = 0;
        if (total > 0) {
            java.util.List<FeedbackDTO> feedbacks = getFeedbacksByProduct(productId, null);
            int sum = 0;
            for (var fb : feedbacks) sum += fb.getRating();
            avg = (double) sum / total;
        }
        stats.put("average", avg);
        stats.put("total", total);
        java.util.Map<Integer, Long> countByStar = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            countByStar.put(i, feedbackRepository.countByProduct_ProductIdAndRating(productId, (byte) i));
        }
        stats.put("countByStar", countByStar);
        return stats;
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
        // Bổ sung: trả về nội dung trả lời nếu có
        if (replyRepository != null) {
            var reply = replyRepository.findByFeedback_Id(fb.getId());
            if (reply != null) {
                dto.setReplyContent(reply.getContent());
            }
        }
        return dto;
    }
}

