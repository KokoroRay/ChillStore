package com.esms.repository;

import com.esms.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "str(o.orderId) LIKE %:keyword% OR lower(o.customer.name) LIKE lower(concat('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR o.status = :status) ")
    List<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") String status);

    //tổng doanh thu
    @Query("SELECT COALESCE(SUM(o.totalAmount + o.discountAmount), 0) FROM Order o where o.status IN ('Paid', 'Shipped', 'Delivered')")
    Long getGrossRevenue();

    //doanh thu thuần
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o where o.status IN ('Paid', 'Shipped', 'Delivered')")
    Long getNetRevenue();

    //số lượng đơn
    @Query("SELECT COUNT(o) FROM Order o where o.status IN ('Paid', 'Shipped', 'Delivered')")
    Long getOrderCount();

    //giá trị đơn hàng trung bình
    @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o where o.status IN ('Paid', 'Shipped', 'Delivered')")
    Double getAverageOrderValue();

    //doanh thu theo thời gian
    @Query(value = "SELECT period, SUM(total_amount) as revenue " +
            "FROM (" +
            "   SELECT FORMAT(o.order_date, :periodFormat) as period, o.total_amount " +
            "   FROM orders o " +
            "   WHERE o.status IN ('Paid','Shipped','Delivered') " +
            "   AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "   AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            ") as subquery " +
            "GROUP BY period " +
            "ORDER BY MIN(CAST(period AS DATE))", nativeQuery = true)
    List<Object[]> getRevenueTrend(
            @Param("periodFormat") String periodFormat,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
    //doanh thu theo category
    @Query(value = "SELECT c.name, SUM(oi.quantity * oi.price_each) as revenue " +
            "FROM orders o " +
            "JOIN order_items oi ON o.order_id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "WHERE o.status IN ('Paid','Shipped','Delivered') " +
            "AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            "GROUP BY c.name", nativeQuery = true)
    List<Object[]> getRevenueByCategory(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    //doanh thu theo khách hàng
    @Query(value = "SELECT c.name, SUM(o.total_amount) as revenue " +
            "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            "GROUP BY c.name ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> getRevenueByCustomer(@Param("startDate") Date startDate,
                                        @Param("endDate") Date endDate);

    //tỉ lệ hủy đơn
    @Query("SELECT (CAST(COUNT(CASE WHEN o.status = 'Cancelled' THEN 1 END) AS float) / COUNT(o)) " +
            "FROM Order o " +
            "WHERE (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Double getCancellationRate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    //Doanh thu theo khu vực
    @Query(value = "SELECT SUBSTRING(c.address, 1, CHARINDEX(',', c.address))" +
            "as region, SUM(o.total_amount) as revenue " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.customer_id " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            "GROUP BY SUBSTRING(c.address, 1, CHARINDEX(',', c.address))",
            nativeQuery = true)
    List<Object[]> getRevenueByRegion(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);

    //
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)")
    Double getNetRevenueBetween(@Param("startDate") Date startDate,
                                @Param("endDate") Date endDate);

    //lấy danh sách đơn hàng
    @Query("SELECT o FROM Order o WHERE (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate)" +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findOrdersByFilters(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(value = "SELECT TOP 10 p.name, SUM(oi.quantity * oi.price_each) as revenue, " +
            "(SUM(oi.quantity * oi.price_each) / total.total * 100) as percentage " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "CROSS JOIN (SELECT SUM(oi2.quantity * oi2.price_each) as total FROM order_items oi2) total " +
            "WHERE o.status IN ('Paid', 'Shipped', 'Delivered') " +
            "AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            "GROUP BY p.name, total.total " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> getParetoDate(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );



 }