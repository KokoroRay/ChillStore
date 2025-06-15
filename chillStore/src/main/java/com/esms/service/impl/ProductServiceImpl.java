package com.esms.service.impl;

import com.esms.model.entity.Product;
import com.esms.repository.ProductRepository;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
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
    public Product addProduct(Product product) {
        product.setStatus(true);
        return productRepository.save(product);
    }

    @Override
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
    public void deleteProduct(Integer productId) {
        Product product = getProductById(productId);
        product.setStatus(false);
        productRepository.save(product);
    }
}

