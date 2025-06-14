package com.esms.service;

import com.esms.model.entity.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Integer productId);
    List<Product> searchProducts(String keyword);
    List<Product> filterByStatus(boolean status);
    List<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> getAvailableProducts();
    Product addProduct(Product product);
    Product updateProduct(Integer productId, Product product);
    void deleteProduct(Integer productId);
    void updateStock(Integer productId, Integer quantity);
}
