package com.esms.service.impl;

import com.esms.model.dto.ProductDTO;
import com.esms.model.entity.Product;
import com.esms.repository.ProductRepository;
import com.esms.service.ProductService;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.text.Normalizer;
import com.esms.repository.DiscountProductRepository;
import com.esms.repository.DiscountRepository;
import com.esms.model.entity.Discount;
import java.time.LocalDate;
import com.esms.model.entity.DiscountProduct;
import com.esms.repository.OrderItemRepository;

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
        // existingProduct.setStockQty(product.getStockQty());

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
    @Override
    public List<ProductDTO> getAllProductDTOs() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> new ProductDTO(
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice()
                ))
                .collect(Collectors.toList());
    }
    @Override
    public Page<ProductDTO> getProductDTOsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> convertToDTO(product));
    }

    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(product.getProductId(), product.getName(), product.getPrice(), product.getImageUrl());
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryNameIgnoreCase(category, pageable);

        return productPage.map(product -> {
            ProductDTO dto = new ProductDTO();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setImageUrl(product.getImageUrl());

            if (product.getCategory() != null) {
                dto.setCategory(product.getCategory());
            }

            return dto;
        });

    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Autowired
    private DiscountProductRepository discountProductRepository;
    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public Page<Product> getDiscountProducts(Pageable pageable) {
        // Lấy tất cả Discount còn hiệu lực (active, trong thời gian áp dụng, loại áp dụng là product)
        List<Discount> activeDiscounts = discountRepository.findActiveDiscountsByDate(LocalDate.now());
        List<Integer> promoIds = activeDiscounts.stream()
                .filter(d -> "product".equalsIgnoreCase(d.getApplyType()))
                .map(Discount::getPromoId)
                .toList();
        // Lấy tất cả DiscountProduct liên quan
        List<Product> discountProducts = discountProductRepository.findAll().stream()
                .filter(dp -> promoIds.contains(dp.getDiscount().getPromoId()))
                .map(dp -> dp.getProduct())
                .distinct()
                .toList();
        // Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), discountProducts.size());
        List<Product> pageContent = (start > discountProducts.size()) ? List.of() : discountProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, discountProducts.size());
    }

    @Override
    @Transactional
    public Product getProductWithDetails(Integer productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getSpecifications());
        return product;
    }

    @Override
    public Discount getActiveDiscountForProduct(Product product) {
        List<DiscountProduct> discountProducts = discountProductRepository.findByProductId(product.getProductId());
        for (DiscountProduct dp : discountProducts) {
            Discount discount = dp.getDiscount();
            if (discount.getActive() != null && discount.getActive()
                && discount.getStartDate() != null && discount.getEndDate() != null
                && !discount.getStartDate().isAfter(LocalDate.now())
                && !discount.getEndDate().isBefore(LocalDate.now())) {
                return discount;
            }
        }
        return null;
    }

    @Override
    public Page<Product> searchDiscountProductsWithFilters(
            String keyword,
            Integer categoryId,
            Integer brandId,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortDir,
            Pageable pageable) {
        
        // Lấy tất cả sản phẩm có discount trước
        List<Discount> activeDiscounts = discountRepository.findActiveDiscountsByDate(LocalDate.now());
        List<Integer> promoIds = activeDiscounts.stream()
                .filter(d -> "product".equalsIgnoreCase(d.getApplyType()))
                .map(Discount::getPromoId)
                .toList();
        
        List<Product> products = discountProductRepository.findAll().stream()
                .filter(dp -> promoIds.contains(dp.getDiscount().getPromoId()))
                .map(dp -> dp.getProduct())
                .distinct()
                .toList();

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

        // Filter by price range (giá sau discount)
        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> {
                        try {
                            // Tính giá sau discount
                            Discount discount = getActiveDiscountForProduct(p);
                            double finalPrice = p.getPrice().doubleValue();
                            if (discount != null && discount.getDiscountPct() != null) {
                                double discountPct = discount.getDiscountPct().doubleValue();
                                finalPrice = p.getPrice().doubleValue() * (100.0 - discountPct) / 100.0;
                            }
                            return finalPrice >= minPrice;
                        } catch (Exception e) {
                            // Nếu có lỗi, sử dụng giá gốc
                            return p.getPrice().doubleValue() >= minPrice;
                        }
                    })
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> {
                        try {
                            // Tính giá sau discount
                            Discount discount = getActiveDiscountForProduct(p);
                            double finalPrice = p.getPrice().doubleValue();
                            if (discount != null && discount.getDiscountPct() != null) {
                                double discountPct = discount.getDiscountPct().doubleValue();
                                finalPrice = p.getPrice().doubleValue() * (100 - discountPct) / 100;
                            }
                            return finalPrice <= maxPrice;
                        } catch (Exception e) {
                            // Nếu có lỗi, sử dụng giá gốc
                            return p.getPrice().doubleValue() <= maxPrice;
                        }
                    })
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
                    products.sort((p1, p2) -> {
                        try {
                            // Tính giá sau discount cho cả hai sản phẩm
                            Discount discount1 = getActiveDiscountForProduct(p1);
                            Discount discount2 = getActiveDiscountForProduct(p2);
                            
                            double price1 = p1.getPrice().doubleValue();
                            double price2 = p2.getPrice().doubleValue();
                            
                            if (discount1 != null && discount1.getDiscountPct() != null) {
                                double discountPct1 = discount1.getDiscountPct().doubleValue();
                                price1 = p1.getPrice().doubleValue() * (100.0 - discountPct1) / 100.0;
                            }
                            if (discount2 != null && discount2.getDiscountPct() != null) {
                                double discountPct2 = discount2.getDiscountPct().doubleValue();
                                price2 = p2.getPrice().doubleValue() * (100.0 - discountPct2) / 100.0;
                            }
                            
                            return isAsc ? Double.compare(price1, price2) : Double.compare(price2, price1);
                        } catch (Exception e) {
                            // Nếu có lỗi, sort theo giá gốc
                            return isAsc ? 
                                p1.getPrice().compareTo(p2.getPrice()) : 
                                p2.getPrice().compareTo(p1.getPrice());
                        }
                    });
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

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public int getTotalSoldQuantity(Integer productId) {
        Integer count = orderItemRepository.countTotalSoldByProductId(productId);
        return count != null ? count : 0;
    }

}
