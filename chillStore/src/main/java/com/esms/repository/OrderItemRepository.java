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

    @Query(value = "SELECT p.name, SUM(oi.quantity), oi.price_each, SUM(oi.quantity * oi.price_each) " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.order_id " +
           "JOIN products p ON oi.product_id = p.product_id " +
           "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
           "GROUP BY p.name, oi.price_each", nativeQuery = true)
    List<Object[]> getProductSalesStatistics();

    @Query(value = "SELECT TOP 10 s.name, SUM(oi.quantity) as total_sold " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.order_id " +
           "JOIN staff s ON o.staff_id = s.staff_id " +
           "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
           "GROUP BY s.name " +
           "ORDER BY total_sold DESC", nativeQuery = true)
    List<Object[]> getTop10StaffBySales();
} 