package com.esms.repository;

import com.esms.model.entity.Feedback;
import com.esms.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    // Lấy tất cả feedback của 1 sản phẩm
    List<Feedback> findByProduct_ProductId(int productId);

    // Lấy feedback của 1 sản phẩm theo rating
    List<Feedback> findByProduct_ProductIdAndRating(int productId, Byte rating);

    // Đếm số feedback theo rating cho 1 sản phẩm
    long countByProduct_ProductIdAndRating(int productId, Byte rating);

    // Đếm tổng số feedback cho 1 sản phẩm
    long countByProduct_ProductId(int productId);

    boolean existsByCustomer_CustomerIdAndProduct_ProductId(int customerId, int productId);

    Feedback findByCustomer_CustomerIdAndProduct_ProductId(int customerId, int productId);

    org.springframework.data.domain.Page<Feedback> findByProduct_ProductId(int productId, org.springframework.data.domain.Pageable pageable);
}

