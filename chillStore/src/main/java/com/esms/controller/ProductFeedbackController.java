package com.esms.controller;

import com.esms.model.dto.FeedbackDTO;
import com.esms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/product/{productId}/feedbacks")
public class ProductFeedbackController {
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private com.esms.repository.OrderItemRepository orderItemRepository;
    @Autowired
    private com.esms.repository.FeedbackRepository feedbackRepository;

    // Lấy feedback theo sản phẩm (có filter rating, phân trang)
    @GetMapping
    public Map<String, Object> getFeedbacksByProduct(@PathVariable("productId") int productId,
                                                   @RequestParam(value = "rating", required = false) Byte rating,
                                                   @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                   @RequestParam(value = "size", required = false, defaultValue = "5") int size) {
        Page<FeedbackDTO> feedbackPage = feedbackService.getFeedbacksByProductPaged(productId, rating, page, size);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("content", feedbackPage.getContent());
        result.put("totalElements", feedbackPage.getTotalElements());
        result.put("totalPages", feedbackPage.getTotalPages());
        result.put("page", feedbackPage.getNumber());
        return result;
    }

    // Gửi feedback mới
    @PostMapping
    public FeedbackDTO addFeedback(@PathVariable("productId") int productId,
                                   @RequestParam("rating") Byte rating,
                                   @RequestParam(value = "comment", required = false) String comment,
                                   HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            throw new RuntimeException("Bạn cần đăng nhập để đánh giá sản phẩm!");
        }
        return feedbackService.addFeedback(customerId, productId, rating, comment);
    }

    // Lấy thống kê feedback
    @GetMapping("/stats")
    public Map<String, Object> getFeedbackStats(@PathVariable("productId") int productId) {
        return feedbackService.getFeedbackStats(productId);
    }

    // Sửa feedback
    @PutMapping
    public FeedbackDTO updateFeedback(@PathVariable("productId") int productId,
                                      @RequestParam("rating") Byte rating,
                                      @RequestParam(value = "comment", required = false) String comment,
                                      HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            throw new RuntimeException("Bạn cần đăng nhập để sửa đánh giá!");
        }
        // Bảo vệ: chỉ cho phép sửa feedback của chính mình
        var feedback = feedbackRepository.findByCustomer_CustomerIdAndProduct_ProductId(customerId, productId);
        if (feedback == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền sửa feedback này!");
        }
        return feedbackService.updateFeedback(customerId, productId, rating, comment);
    }

    // Xóa feedback
    @DeleteMapping
    public void deleteFeedback(@PathVariable("productId") int productId, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            throw new RuntimeException("Bạn cần đăng nhập để xóa đánh giá!");
        }
        // Bảo vệ: chỉ cho phép xóa feedback của chính mình
        var feedback = feedbackRepository.findByCustomer_CustomerIdAndProduct_ProductId(customerId, productId);
        if (feedback == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xóa feedback này!");
        }
        feedbackService.deleteFeedback(customerId, productId);
    }

    // API check đã mua hàng cho feedback (chuẩn RESTful, không lồng /api)
    @GetMapping("/check-bought")
    public java.util.Map<String, Boolean> checkBought(@PathVariable("productId") int productId, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        boolean bought = false;
        if (customerId != null) {
            bought = orderItemRepository.existsByCustomerAndProduct(customerId, productId);
        }
        java.util.Map<String, Boolean> result = new java.util.HashMap<>();
        result.put("bought", bought);
        return result;
    }
} 