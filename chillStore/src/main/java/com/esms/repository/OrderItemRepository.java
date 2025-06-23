package com.esms.repository;

import com.esms.model.entity.OrderItem;
import com.esms.model.entity.OrderItemId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    @Query(value = "SELECT COUNT(*) FROM order_items WHERE order_id = :orderId", nativeQuery = true)
    int countItemsByOrderId(@Param("orderId") Integer orderId);

    @Query(value = "SELECT SUM(quantity * price_each) FROM order_items WHERE order_id = :orderId", nativeQuery = true)
    Double sumTotalAmountByOrderId(@Param("orderId") Integer orderId);

    List<OrderItem> findByIdOrderId(Integer orderId);
} 