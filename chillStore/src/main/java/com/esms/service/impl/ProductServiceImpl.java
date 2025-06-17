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
import java.util.stream.Collectors;
import java.text.Normalizer;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
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
    public Page<Product> searchProductsWithFilters(
            String keyword,
            Integer categoryId,
            Integer brandId,
            Double minPrice,
            Double maxPrice,
            Integer minStock,
            String sortBy,
            String sortDir,
            Pageable pageable,
            Boolean status) {

        List<Product> products = productRepository.findAll();

        // Filter by keyword (tên sản phẩm, tên danh mục, mô tả)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = removeDiacritics(keyword.trim().toLowerCase());
            products = products.stream()
                    .filter(p -> {
                        String productName = removeDiacritics(p.getName() != null ? p.getName().toLowerCase() : "");
                        String productDesc = removeDiacritics(p.getDescription() != null ? p.getDescription().toLowerCase() : "");
                        String categoryName = removeDiacritics(p.getCategory() != null && p.getCategory().getName() != null ? p.getCategory().getName().toLowerCase() : "");
                        return productName.contains(finalKeyword)
                                || productDesc.contains(finalKeyword)
                                || categoryName.contains(finalKeyword);
                    })
                    .collect(Collectors.toList());
        }

        // Filter by category
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Filter by brand
        if (brandId != null) {
            products = products.stream()
                    .filter(p -> p.getBrand() != null && p.getBrand().getId().equals(brandId))
                    .collect(Collectors.toList());
        }

        // Filter by price range
        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice().doubleValue() >= minPrice)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice().doubleValue() <= maxPrice)
                    .collect(Collectors.toList());
        }

        // Filter by minStock
        if (minStock != null) {
            products = products.stream()
                    .filter(p -> p.getStockQty() >= minStock)
                    .collect(Collectors.toList());
        }

        // Filter by status
        if (status != null) {
            products = products.stream()
                    .filter(p -> p.isStatus() == status)
                    .collect(Collectors.toList());
        }

        // Sort products
        if (sortBy != null && sortDir != null) {
            boolean isAsc = sortDir.equalsIgnoreCase("asc");
            switch (sortBy.toLowerCase()) {
                case "name":
                    products.sort((p1, p2) -> isAsc ?
                            p1.getName().compareTo(p2.getName()) :
                            p2.getName().compareTo(p1.getName()));
                    break;
                case "price":
                    products.sort((p1, p2) -> isAsc ?
                            p1.getPrice().compareTo(p2.getPrice()) :
                            p2.getPrice().compareTo(p1.getPrice()));
                    break;
                default:
                    break;
            }
        }

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());

        if (start > products.size()) {
            return new PageImpl<>(List.of(), pageable, products.size());
        }

        return new PageImpl<>(
                products.subList(start, end),
                pageable,
                products.size()
        );
    }

    @Override
    public List<Product> filterByStatus(boolean status) {
        return productRepository.findByStatus(status);
    }

    @Override
    public Product updateProduct(Integer productId, Product product) {
        Product existingProduct = getProductById(productId);

        // Update basic information
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQty(product.getStockQty());

        // Update status
        existingProduct.setStatus(product.isStatus());

        // Update category and brand if they exist
        if (product.getCategory() != null) {
            existingProduct.setCategory(product.getCategory());
        }
        if (product.getBrand() != null) {
            existingProduct.setBrand(product.getBrand());
        }

        // Update image URL if provided
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            existingProduct.setImageUrl(product.getImageUrl());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }

    private String removeDiacritics(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}
