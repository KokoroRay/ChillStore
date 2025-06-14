package com.esms.service;

import com.esms.model.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> searchProducts(String keyword);
    List<Product> filterByCategory(String category);
    List<Product> filterByPriceRange(Double minPrice, Double maxPrice);
    Product addProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
