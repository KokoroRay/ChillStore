package com.esms.service.impl;

import com.esms.model.entity.Product;
import com.esms.repository.ProductRepository;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    @Override
    public List<Product> filterByStatus(boolean status) {
        return productRepository.findByStatus(status);
    }

    @Override
    public List<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    @Override
    @Transactional
    public Product addProduct(Product product) {
        product.setStatus(true);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Integer productId, Product product) {
        Product existingProduct = getProductById(productId);
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQty(product.getStockQty());
        existingProduct.setStatus(product.isStatus());
        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = getProductById(productId);
        product.setStatus(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateStock(Integer productId, Integer quantity) {
        Product product = getProductById(productId);
        product.setStockQty(product.getStockQty() + quantity);
        productRepository.save(product);
    }
}
