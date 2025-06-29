package com.esms.service;

import com.esms.model.dto.ProductDto;
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


    List<ProductDto> getAllProductDTOs();

    Page<ProductDto> getProductDTOsPaginated(Pageable pageable);

    Page<ProductDto> getProductsByCategory(String category, Pageable pageable);
}

