package com.esms.repository;

import com.esms.model.entity.Feedback;
import com.esms.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    Optional<Feedback> findByCustomerCustomerIdAndProductProductId(Integer customerId, Integer productId);
    List<Feedback> findByProductProductId(Integer productId);
    List<Feedback> findByCustomerCustomerId(Integer customerId);
    Page<Feedback> findByProductProductId(Integer productId, Pageable pageable);
}

