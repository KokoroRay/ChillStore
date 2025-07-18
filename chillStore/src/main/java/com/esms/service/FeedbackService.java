package com.esms.service;

import com.esms.model.dto.FeedbackDTO;

import java.util.List;

public interface FeedbackService {
    List<FeedbackDTO> getAllFeedbacks();
    // Lấy feedback theo productId, có filter rating (nếu rating=null thì lấy tất cả)
    List<FeedbackDTO> getFeedbacksByProduct(int productId, Byte rating);

    // Lấy feedback theo productId, có filter rating và phân trang
    org.springframework.data.domain.Page<FeedbackDTO> getFeedbacksByProductPaged(int productId, Byte rating, int page, int size);

    // Thêm feedback mới từ customer
    FeedbackDTO addFeedback(int customerId, int productId, Byte rating, String comment);

    // Trả về thống kê feedback cho 1 sản phẩm
    java.util.Map<String, Object> getFeedbackStats(int productId);

    // Sửa feedback
    FeedbackDTO updateFeedback(int customerId, int productId, Byte rating, String comment);
    // Xóa feedback
    void deleteFeedback(int customerId, int productId);
}
