package com.esms.repository;

import com.esms.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR str(o.orderId) LIKE %:keyword% OR lower(o.customer.name) LIKE lower(concat('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR o.status = :status)")
    List<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") String status);

    //tổng doanh thu
    @Query("SELECT coalesce(SUM(o.totalAmount + o.discountAmount), 0) " +
            "FROM Order o "
            + "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') "
            + "AND o.orderDate >= :startDate "
            + "AND o.orderDate <= :endDate")
    BigDecimal getGrossRevenue(@Param("startDate") Date startDate,
                               @Param("endDate") Date endDate);

    //doanh thu thuần
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
            "FROM Order o " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND o.orderDate >= :startDate " +
            "AND o.orderDate <= :endDate")
    BigDecimal getNetRevenueBetween(@Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    //số lượng đơn
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND o.orderDate >= :startDate " +
            "AND o.orderDate <= :endDate")
    Long getOrderCountBetween(@Param("startDate") Date startDate,
                              @Param("endDate") Date endDate);

    @Query("SELECT DISTINCT SUBSTRING(c.address, 1, 20) FROM Order o JOIN o.customer c")
    List<String> findAllRegions();


    //doanh thu theo thời gian
    @Query(value = "SELECT FORMAT(o.order_date, 'yyyy-MM-dd') AS period, SUM(o.total_amount) AS revenue " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.customer_id " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND o.order_date >= :startDate " +
            "AND o.order_date <= :endDate " +
            "AND (:categoryId IS NULL OR EXISTS (SELECT 1 FROM order_items oi JOIN products p ON oi.product_id = p.product_id WHERE oi.order_id = o.order_id AND p.category_id = :categoryId)) " +
            "AND (:region IS NULL OR :region = '' OR c.address LIKE CONCAT('%', :region, '%')) " +
            "AND (:status IS NULL OR :status = '' OR o.status = :status) " +
            "GROUP BY FORMAT(o.order_date, 'yyyy-MM-dd') " +
            "ORDER BY period ", nativeQuery = true)
    List<Object[]> getRevenueTrend(@Param("period") String period,
                                   @Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("categoryId") Integer categoryId,
                                   @Param("region") String region,
                                   @Param("status") String status);


    //doanh thu theo category
    @Query(value = "SELECT c.name, SUM(oi.quantity * oi.price_each) " +
            "FROM orders o " +
            "JOIN order_items oi ON o.order_id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "WHERE o.status IN ('Paid','Shipped','Delivered') " +
            "AND o.order_date >= :startDate " +
            "AND o.order_date <= :endDate " +
            "GROUP BY c.name", nativeQuery = true)
    List<Object[]> getRevenueByCategory(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    //tỉ lệ hủy đơn
    @Query("SELECT COALESCE(CAST(SUM(CASE WHEN o.status = 'Cancelled' THEN 1.0 ELSE 0.0 END) / COUNT(o) AS double), 0.0) " +
            "FROM Order o " +
            "WHERE o.orderDate >= :startDate " +
            "AND o.orderDate <= :endDate")
    Double getCancellationRate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    //Doanh thu theo khu vực
    @Query("SELECT SUBSTRING(c.address, 1, 20), SUM(o.totalAmount) " +
            "FROM Order o " +
            "JOIN o.customer c WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND o.orderDate >= :startDate " +
            "AND o.orderDate <= :endDate " +
            "AND (:categoryId IS NULL OR EXISTS (SELECT 1 FROM OrderItem oi JOIN oi.product p WHERE oi.order = o AND p.category.id = :categoryId)) " +
            "AND (:region IS NULL OR :region = '' OR c.address LIKE CONCAT('%', :region, '%')) " +
            "AND (:status IS NULL OR :status = '' OR o.status = :status) " +
            "GROUP BY SUBSTRING(c.address, 1, 20)")
    List<Object[]> getRevenueByRegion(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("categoryId") Integer categoryId,
                                      @Param("region") String region,
                                      @Param("status") String status);

    //pareto
    @Query(value = "WITH TotalRevenue AS ( " +
            "  SELECT SUM(oi.quantity * oi.price_each) AS total " +
            "  FROM order_items oi " +
            "  JOIN orders o ON oi.order_id = o.order_id " +
            "  WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "  AND o.order_date >= :startDate " +
            "  AND o.order_date <= :endDate " +
            ") " +
            "SELECT TOP 10 p.name, " +
            "  SUM(oi.quantity * oi.price_each) AS revenue, " +
            "  (SUM(oi.quantity * oi.price_each) * 100.0 / tr.total) AS percentage " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "CROSS JOIN TotalRevenue tr " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND o.order_date >= :startDate " +
            "AND o.order_date <= :endDate " +
            "GROUP BY p.name, tr.total " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> getParetoDate(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );


    }