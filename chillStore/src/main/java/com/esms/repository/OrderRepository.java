package com.esms.repository;

import com.esms.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR str(o.orderId) LIKE %:keyword% OR lower(o.customer.name) LIKE lower(concat('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR o.status = :status)")
    List<Order> searchOrders(@Param("keyword") String keyword, @Param("status") String status);
} 