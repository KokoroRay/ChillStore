package com.esms.service;

import com.esms.model.dto.ProductDTO;
import com.esms.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    List<ProductDTO> getTopBestSellingProducts(int limit);
    List<ProductDTO> getNewestProducts(int limit);
    List<ProductDTO> getRandomProducts(int limit);
    List<ProductDTO> getRandomProductsPaged(int page, int size);
    int getRandomProductsTotalPages(int size);
    int getTotalSoldQuantity(Integer productId);

    /**
     * Tìm sản phẩm theo tên chính xác (không phân biệt hoa thường)
     */
    Product getProductByName(String name);
   /*Tìm sản phẩm theo keywword */
    List<ProductDTO> searchProductsByKeyword(String keyword);
    List<ProductDTO> searchByCategoryAndBrand(Long categoryId, Long brandId);
    List<ProductDTO> getProductsByCategoryId(Long categoryId);
    List<ProductDTO> getProductsByBrandId(Long brandId);
    List<String> suggestProductNames(String term);


}

