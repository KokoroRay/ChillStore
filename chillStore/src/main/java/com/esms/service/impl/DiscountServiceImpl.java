package com.esms.service.impl;

import com.esms.exception.ResourceNotFoundException;
import com.esms.model.dto.DiscountDTO;
import com.esms.model.entity.Discount;
import com.esms.model.entity.DiscountProduct;
import com.esms.model.entity.DiscountProductId;
import com.esms.model.entity.Product;
import com.esms.repository.DiscountProductRepository;
import com.esms.repository.DiscountRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation cho Discount
 * Chứa logic xử lý business cho discount
 */
@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {
    
    @Autowired
    private DiscountRepository discountRepository;
    
    @Autowired
    private DiscountProductRepository discountProductRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Lấy tất cả discount
     * @return List<DiscountDTO>
     */
    @Override
    public List<DiscountDTO> getAllDiscounts() {
        // Lấy tất cả discount từ database
        List<Discount> discounts = discountRepository.findAll();
        
        // Chuyển đổi thành DTO và trả về
        return discounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy discount theo ID
     * @param promoId ID của discount
     * @return DiscountDTO
     */
    @Override
    public DiscountDTO getDiscountById(Integer promoId) {
        // Tìm discount theo ID, nếu không tìm thấy thì throw exception
        Discount discount = discountRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + promoId));
        
        // Chuyển đổi thành DTO và trả về
        return convertToDto(discount);
    }
    
    /**
     * Lấy discount theo code
     * @param code mã code của discount
     * @return DiscountDTO
     */
    @Override
    public DiscountDTO getDiscountByCode(String code) {
        // Tìm discount theo code, nếu không tìm thấy thì throw exception
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with code: " + code));
        
        // Chuyển đổi thành DTO và trả về
        return convertToDto(discount);
    }
    
    /**
     * Lấy tất cả discount đang hoạt động
     * @return List<DiscountDTO>
     */
    @Override
    public List<DiscountDTO> getActiveDiscounts() {
        // Lấy tất cả discount đang active
        List<Discount> discounts = discountRepository.findByActiveTrue();
        
        // Chuyển đổi thành DTO và trả về
        return discounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy discount theo loại áp dụng
     * @param applyType loại áp dụng (product, brand, category)
     * @return List<DiscountDTO>
     */
    @Override
    public List<DiscountDTO> getDiscountsByApplyType(String applyType) {
        // Lấy discount theo loại áp dụng
        List<Discount> discounts = discountRepository.findByApplyType(applyType);
        
        // Chuyển đổi thành DTO và trả về
        return discounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy discount đang trong thời gian áp dụng
     * @return List<DiscountDTO>
     */
    @Override
    public List<DiscountDTO> getCurrentActiveDiscounts() {
        // Lấy discount đang trong thời gian áp dụng
        List<Discount> discounts = discountRepository.findActiveDiscountsByDate(LocalDate.now());
        
        // Chuyển đổi thành DTO và trả về
        return discounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Tạo mới discount
     * @param discountDto thông tin discount cần tạo
     * @return DiscountDTO
     */
    @Override
    public DiscountDTO createDiscount(DiscountDTO discountDto) {
        // Kiểm tra code đã tồn tại chưa
        if (discountRepository.existsByCode(discountDto.getCode())) {
            throw new IllegalArgumentException("Discount code already exists: " + discountDto.getCode());
        }
        
        // Chuyển đổi DTO thành entity
        Discount discount = convertToEntity(discountDto);
        
        // Lưu discount vào database
        Discount savedDiscount = discountRepository.save(discount);
        
        // Xử lý product associations nếu có
        if (discountDto.getProductIds() != null && !discountDto.getProductIds().isEmpty()) {
            saveProductAssociations(savedDiscount.getPromoId(), discountDto.getProductIds());
        }
        
        // Chuyển đổi thành DTO và trả về
        return convertToDto(savedDiscount);
    }
    
    /**
     * Cập nhật discount
     * @param promoId ID của discount
     * @param discountDto thông tin discount cần cập nhật
     * @return DiscountDTO
     */
    @Override
    public DiscountDTO updateDiscount(Integer promoId, DiscountDTO discountDto) {
        // Tìm discount theo ID, nếu không tìm thấy thì throw exception
        Discount existingDiscount = discountRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + promoId));
        
        // Cập nhật thông tin discount
        existingDiscount.setCode(discountDto.getCode());
        existingDiscount.setDescription(discountDto.getDescription());
        existingDiscount.setDiscountPct(discountDto.getDiscountPct());
        existingDiscount.setStartDate(discountDto.getStartDate());
        existingDiscount.setEndDate(discountDto.getEndDate());
        existingDiscount.setActive(discountDto.getActive());
        existingDiscount.setApplyType(discountDto.getApplyType());
        
        // Lưu discount đã cập nhật
        Discount updatedDiscount = discountRepository.save(existingDiscount);
        
        // Xử lý product associations nếu có
        if (discountDto.getProductIds() != null) {
            saveProductAssociations(promoId, discountDto.getProductIds());
        }
        
        // Chuyển đổi thành DTO và trả về
        return convertToDto(updatedDiscount);
    }
    
    /**
     * Xóa discount
     * @param promoId ID của discount
     */
    @Override
    public void deleteDiscount(Integer promoId) {
        // Kiểm tra discount có tồn tại không
        if (!discountRepository.existsById(promoId)) {
            throw new ResourceNotFoundException("Discount not found with id: " + promoId);
        }
        
        // Xóa các liên kết product trước
        discountProductRepository.deleteByPromoId(promoId);
        
        // Xóa discount
        discountRepository.deleteById(promoId);
    }
    
    /**
     * Kích hoạt/vô hiệu hóa discount
     * @param promoId ID của discount
     * @param active trạng thái mới
     * @return DiscountDTO
     */
    @Override
    public DiscountDTO toggleDiscountStatus(Integer promoId, Boolean active) {
        // Tìm discount theo ID, nếu không tìm thấy thì throw exception
        Discount discount = discountRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + promoId));
        
        // Cập nhật trạng thái
        discount.setActive(active);
        
        // Lưu discount đã cập nhật
        Discount updatedDiscount = discountRepository.save(discount);
        
        // Chuyển đổi thành DTO và trả về
        return convertToDto(updatedDiscount);
    }
    
    /**
     * Chuyển đổi từ Entity sang DTO
     * @param discount entity cần chuyển đổi
     * @return DiscountDTO
     */
    private DiscountDTO convertToDto(Discount discount) {
        DiscountDTO dto = new DiscountDTO();
        
        // Copy các thuộc tính cơ bản
        dto.setPromoId(discount.getPromoId());
        dto.setCode(discount.getCode());
        dto.setDescription(discount.getDescription());
        dto.setDiscountPct(discount.getDiscountPct());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        dto.setActive(discount.getActive());
        dto.setCreatedBy(discount.getCreatedBy());
        dto.setApplyType(discount.getApplyType());
        
        // Lấy tên admin tạo discount
        if (discount.getAdmin() != null) {
            dto.setAdminName(discount.getAdmin().getName());
        }
        
        // Lấy danh sách tên sản phẩm được áp dụng
        List<DiscountProduct> discountProducts = discountProductRepository.findByPromoId(discount.getPromoId());
        if (discountProducts != null && !discountProducts.isEmpty()) {
            List<String> productNames = discountProducts.stream()
                    .map(dp -> dp.getProduct().getName())
                    .collect(Collectors.toList());
            dto.setProductNames(productNames);
            dto.setProductCount(productNames.size());
            
            // Lấy danh sách product IDs
            List<Integer> productIds = discountProducts.stream()
                    .map(dp -> dp.getProduct().getProductId())
                    .collect(Collectors.toList());
            dto.setProductIds(productIds);
        } else {
            dto.setProductCount(0);
        }
        
        return dto;
    }
    
    /**
     * Chuyển đổi từ DTO sang Entity
     * @param discountDto DTO cần chuyển đổi
     * @return Discount
     */
    private Discount convertToEntity(DiscountDTO discountDto) {
        Discount discount = new Discount();
        
        // Copy các thuộc tính cơ bản
        discount.setCode(discountDto.getCode());
        discount.setDescription(discountDto.getDescription());
        discount.setDiscountPct(discountDto.getDiscountPct());
        discount.setStartDate(discountDto.getStartDate());
        discount.setEndDate(discountDto.getEndDate());
        discount.setActive(discountDto.getActive());
        discount.setCreatedBy(discountDto.getCreatedBy());
        discount.setApplyType(discountDto.getApplyType());
        
        return discount;
    }
    
    /**
     * Lưu các liên kết giữa discount và products
     * @param promoId ID của discount
     * @param productIds Danh sách ID sản phẩm
     */
    private void saveProductAssociations(Integer promoId, List<Integer> productIds) {
        // Xóa các liên kết cũ (nếu có)
        discountProductRepository.deleteByPromoId(promoId);
        
        // Tạo các liên kết mới
        for (Integer productId : productIds) {
            DiscountProduct discountProduct = new DiscountProduct();
            
            // Tạo composite key
            DiscountProductId id = new DiscountProductId();
            id.setPromoId(promoId);
            id.setProductId(productId);
            discountProduct.setId(id);
            
            // Set các entity liên quan
            Discount discount = new Discount();
            discount.setPromoId(promoId);
            discountProduct.setDiscount(discount);
            
            Product product = new Product();
            product.setProductId(productId);
            discountProduct.setProduct(product);
            
            discountProductRepository.save(discountProduct);
        }
    }
    
    /**
     * Tìm kiếm và lọc discount theo nhiều tiêu chí
     * Method này cung cấp chức năng search và filter nâng cao cho discount
     * 
     * @param search Từ khóa tìm kiếm (code, description) - tìm kiếm không phân biệt hoa thường
     * @param status Trạng thái active (true/false) - lọc theo trạng thái hoạt động
     * @param applyType Loại áp dụng (product/brand/category) - lọc theo cách áp dụng discount
     * @param startDate Ngày bắt đầu (format: yyyy-MM-dd) - lọc discount có start date từ ngày này
     * @param endDate Ngày kết thúc (format: yyyy-MM-dd) - lọc discount có end date đến ngày này
     * @param categoryId ID category - lọc discount áp dụng cho sản phẩm thuộc category này
     * @param brandId ID brand - lọc discount áp dụng cho sản phẩm thuộc brand này
     * @param productId ID product - lọc discount áp dụng cho sản phẩm cụ thể này
     * @param discountRange Khoảng discount percentage (0-10, 10-20, 20-30, 30-50, 50+) - lọc theo % giảm giá
     * @return List<DiscountDTO> Danh sách discount đã được lọc theo các tiêu chí
     */
    @Override
    public List<DiscountDTO> searchAndFilterDiscounts(String search, String status, String applyType,
                                                      String startDate, String endDate, Integer categoryId,
                                                      Integer brandId, Integer productId, String discountRange) {
        // Lấy tất cả discount từ database để xử lý filtering
        List<Discount> allDiscounts = discountRepository.findAll();
        
        // Sử dụng Stream API để lọc discount theo các tiêu chí
        return allDiscounts.stream()
                .filter(discount -> {
                    // Filter by search term - tìm kiếm theo code hoặc description
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase();
                        boolean matchesSearch = discount.getCode().toLowerCase().contains(searchLower) ||
                                               (discount.getDescription() != null && 
                                                discount.getDescription().toLowerCase().contains(searchLower));
                        if (!matchesSearch) return false;
                    }
                    
                    // Filter by status - lọc theo trạng thái active/inactive
                    if (status != null && !status.trim().isEmpty()) {
                        boolean isActive = Boolean.parseBoolean(status);
                        if (discount.getActive() != isActive) return false;
                    }
                    
                    // Filter by apply type - lọc theo loại áp dụng
                    if (applyType != null && !applyType.trim().isEmpty()) {
                        if (!applyType.equals(discount.getApplyType())) return false;
                    }
                    
                    // Filter by start date - lọc theo ngày bắt đầu
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        LocalDate filterStartDate = LocalDate.parse(startDate);
                        if (discount.getStartDate().isBefore(filterStartDate)) return false;
                    }
                    
                    // Filter by end date - lọc theo ngày kết thúc
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        LocalDate filterEndDate = LocalDate.parse(endDate);
                        if (discount.getEndDate().isAfter(filterEndDate)) return false;
                    }
                    
                    // Filter by discount percentage range - lọc theo khoảng % giảm giá
                    if (discountRange != null && !discountRange.trim().isEmpty()) {
                        double discountPct = discount.getDiscountPct().doubleValue();
                        switch (discountRange) {
                            case "0-10":
                                if (discountPct < 0 || discountPct > 10) return false;
                                break;
                            case "10-20":
                                if (discountPct < 10 || discountPct > 20) return false;
                                break;
                            case "20-30":
                                if (discountPct < 20 || discountPct > 30) return false;
                                break;
                            case "30-50":
                                if (discountPct < 30 || discountPct > 50) return false;
                                break;
                            case "50+":
                                if (discountPct < 50) return false;
                                break;
                        }
                    }
                    
                    return true;
                })
                .map(this::convertToDto)
                .filter(dto -> {
                    // Filter by category, brand, product - yêu cầu truy vấn thêm để kiểm tra product associations
                    if (categoryId != null) {
                        // Kiểm tra xem discount có áp dụng cho sản phẩm thuộc category này không
                        List<DiscountProduct> discountProducts = discountProductRepository.findByPromoId(dto.getPromoId());
                        boolean hasCategoryProduct = discountProducts.stream()
                                .anyMatch(dp -> dp.getProduct().getCategory().getId().equals(categoryId));
                        if (!hasCategoryProduct) return false;
                    }
                    
                    if (brandId != null) {
                        // Kiểm tra xem discount có áp dụng cho sản phẩm thuộc brand này không
                        List<DiscountProduct> discountProducts = discountProductRepository.findByPromoId(dto.getPromoId());
                        boolean hasBrandProduct = discountProducts.stream()
                                .anyMatch(dp -> dp.getProduct().getBrand().getId().equals(brandId));
                        if (!hasBrandProduct) return false;
                    }
                    
                    if (productId != null) {
                        // Kiểm tra xem discount có áp dụng cho sản phẩm cụ thể này không
                        List<DiscountProduct> discountProducts = discountProductRepository.findByPromoId(dto.getPromoId());
                        boolean hasProduct = discountProducts.stream()
                                .anyMatch(dp -> dp.getProduct().getProductId().equals(productId));
                        if (!hasProduct) return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
} 