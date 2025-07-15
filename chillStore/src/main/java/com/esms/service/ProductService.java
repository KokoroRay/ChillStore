package com.esms.service;

import com.esms.model.dto.ProductDTO;
import com.esms.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ProductService {
    List<Product> findAll();

    Page<Product> getAllProducts(Pageable pageable);

    Product getProductById(Integer productId);

    List<Product> searchProducts(String keyword);

    Page<Product> searchProductsWithFilters(
            String keyword,
            Integer categoryId,
            Integer brandId,
            Double minPrice,
            Double maxPrice,
            Integer minStock,
            String sortBy,
            String sortDir,
            Pageable pageable,
            Boolean status
    );

    List<Product> filterByStatus(boolean status);

    Product updateProduct(Integer productId, Product product);

    void deleteProduct(Integer productId);

    Product saveProduct(Product product);

    // Lấy discount còn hiệu lực cho 1 sản phẩm (nếu có)
    com.esms.model.entity.Discount getActiveDiscountForProduct(Product product);

    List<ProductDTO> getAllProductDTOs();

    Page<ProductDTO> getProductDTOsPaginated(Pageable pageable);

    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);

    /**
     * Lấy danh sách sản phẩm đang có discount còn hiệu lực (active và trong thời gian áp dụng)
     */
    Page<Product> getDiscountProducts(Pageable pageable);

    /**
     * Tìm kiếm và lọc sản phẩm có discount với các filter
     */
    Page<Product> searchDiscountProductsWithFilters(
            String keyword,
            Integer categoryId,
            Integer brandId,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortDir,
            Pageable pageable
    );

    Product getProductWithDetails(Integer productId);

    /**
     * Tìm kiếm sản phẩm theo từ khóa với phân trang
     */
    Page<Product> searchProductsByKeyword(String keyword, Pageable pageable);

    /**
     * Lấy danh sách gợi ý sản phẩm cho autocomplete (name, image, price)
     */
    List<Map<String, Object>> getProductSuggestions(String query);
}

