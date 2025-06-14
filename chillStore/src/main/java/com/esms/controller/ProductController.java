package com.esms.controller;

import com.esms.model.entity.Product;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // View all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // View single product
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Integer productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // Search products
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    // Filter products by status
    @GetMapping("/filter/status")
    public ResponseEntity<List<Product>> filterByStatus(@RequestParam boolean status) {
        return ResponseEntity.ok(productService.filterByStatus(status));
    }

    // Filter products by price range
    @GetMapping("/filter/price")
    public ResponseEntity<List<Product>> filterByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.filterByPriceRange(minPrice, maxPrice));
    }

    // Get available products
    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    // Add new product
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.addProduct(product));
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable("id") Integer productId,
            @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(productId, product));
    }

    // Delete product (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Integer productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }

    // Update stock
    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable("id") Integer productId,
            @RequestParam Integer quantity) {
        productService.updateStock(productId, quantity);
        return ResponseEntity.ok().build();
    }
} 