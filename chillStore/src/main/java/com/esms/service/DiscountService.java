package com.esms.service;

import com.esms.model.dto.DiscountDTO;

import java.util.List;

/**
 * Service interface cho Discount
 * Định nghĩa các method để xử lý business logic cho discount
 */
public interface DiscountService {
    
    /**
     * Lấy tất cả discount
     * @return List<DiscountDTO>
     */
    List<DiscountDTO> getAllDiscounts();
    
    /**
     * Lấy discount theo ID
     * @param promoId ID của discount
     * @return DiscountDTO
     */
    DiscountDTO getDiscountById(Integer promoId);
    
    /**
     * Lấy discount theo code
     * @param code mã code của discount
     * @return DiscountDTO
     */
    DiscountDTO getDiscountByCode(String code);
    
    /**
     * Lấy tất cả discount đang hoạt động
     * @return List<DiscountDTO>
     */
    List<DiscountDTO> getActiveDiscounts();
    
    /**
     * Lấy discount theo loại áp dụng
     * @param applyType loại áp dụng (product, brand, category)
     * @return List<DiscountDTO>
     */
    List<DiscountDTO> getDiscountsByApplyType(String applyType);
    
    /**
     * Lấy discount đang trong thời gian áp dụng
     * @return List<DiscountDTO>
     */
    List<DiscountDTO> getCurrentActiveDiscounts();
    
    /**
     * Tạo mới discount
     * @param discountDto thông tin discount cần tạo
     * @return DiscountDTO
     */
    DiscountDTO createDiscount(DiscountDTO discountDto);
    
    /**
     * Cập nhật discount
     * @param promoId ID của discount
     * @param discountDto thông tin discount cần cập nhật
     * @return DiscountDTO
     */
    DiscountDTO updateDiscount(Integer promoId, DiscountDTO discountDto);
    
    /**
     * Xóa discount
     * @param promoId ID của discount
     */
    void deleteDiscount(Integer promoId);
    
    /**
     * Kích hoạt/vô hiệu hóa discount
     * @param promoId ID của discount
     * @param active trạng thái mới
     * @return DiscountDTO
     */
    DiscountDTO toggleDiscountStatus(Integer promoId, Boolean active);
    
    /**
     * Tìm kiếm và lọc discount theo nhiều tiêu chí
     * @param search Từ khóa tìm kiếm (code, description)
     * @param status Trạng thái active
     * @param applyType Loại áp dụng
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param categoryId ID category
     * @param brandId ID brand
     * @param productId ID product
     * @param discountRange Khoảng discount percentage
     * @return List<DiscountDTO>
     */
    List<DiscountDTO> searchAndFilterDiscounts(String search, String status, String applyType,
                                               String startDate, String endDate, Integer categoryId,
                                               Integer brandId, Integer productId, String discountRange);
} 