package com.esms.repository;

import com.esms.model.entity.DiscountProduct;
import com.esms.model.entity.DiscountProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface cho DiscountProduct
 * Chứa các method để truy vấn dữ liệu liên kết giữa discount và product
 */
@Repository
public interface DiscountProductRepository extends JpaRepository<DiscountProduct, DiscountProductId> {
    
    /**
     * Tìm tất cả sản phẩm được áp dụng bởi một discount
     * @param promoId ID của discount
     * @return List<DiscountProduct>
     */
    @Query("SELECT dp FROM DiscountProduct dp WHERE dp.discount.promoId = :promoId")
    List<DiscountProduct> findByPromoId(@Param("promoId") Integer promoId);
    
    /**
     * Tìm tất cả discount được áp dụng cho một sản phẩm
     * @param productId ID của sản phẩm
     * @return List<DiscountProduct>
     */
    @Query("SELECT dp FROM DiscountProduct dp WHERE dp.product.productId = :productId")
    List<DiscountProduct> findByProductId(@Param("productId") Integer productId);
    
    /**
     * Xóa tất cả liên kết của một discount
     * @param promoId ID của discount
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DiscountProduct dp WHERE dp.discount.promoId = :promoId")
    void deleteByPromoId(@Param("promoId") Integer promoId);
} 