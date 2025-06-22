package com.esms.repository;

import com.esms.model.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Discount
 * Chứa các method để truy vấn dữ liệu discount từ database
 */
@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    
    /**
     * Tìm discount theo code
     * @param code mã code của discount
     * @return Optional<Discount>
     */
    Optional<Discount> findByCode(String code);
    
    /**
     * Tìm tất cả discount đang hoạt động
     * @return List<Discount>
     */
    List<Discount> findByActiveTrue();
    
    /**
     * Tìm discount theo loại áp dụng
     * @param applyType loại áp dụng (product, brand, category)
     * @return List<Discount>
     */
    List<Discount> findByApplyType(String applyType);
    
    /**
     * Tìm discount theo ngày hiện tại (đang trong thời gian áp dụng)
     * @param currentDate ngày hiện tại
     * @return List<Discount>
     */
    @Query("SELECT d FROM Discount d WHERE d.startDate <= :currentDate AND d.endDate >= :currentDate AND d.active = true")
    List<Discount> findActiveDiscountsByDate(@Param("currentDate") java.time.LocalDate currentDate);
    
    /**
     * Kiểm tra code đã tồn tại chưa
     * @param code mã code cần kiểm tra
     * @return boolean
     */
    boolean existsByCode(String code);
} 