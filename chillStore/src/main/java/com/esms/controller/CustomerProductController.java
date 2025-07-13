package com.esms.controller;

import com.esms.model.entity.*;
import com.esms.service.BrandService;
import com.esms.service.CategoryService;
import com.esms.service.CustomerService;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class CustomerProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/Product")
    public String viewProductPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model) {
        // Price validation logic (same as admin)
        String priceError = null;
        if (minPrice != null && minPrice < 1000) {
            priceError = "Minimum price must be at least 1,000 VND";
            minPrice = null;
        }
        if (maxPrice != null && maxPrice > 1000000000) {
            priceError = "Maximum price cannot exceed 1,000,000,000 VND";
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            priceError = "Minimum price cannot be greater than maximum price";
            minPrice = null;
            maxPrice = null;
        }
        // Parse sortBy and sortDir from sortOption
        String sortBy = null;
        String sortDir = null;
        if (sortOption != null && !sortOption.equals("default")) {
            switch (sortOption) {
                case "name_asc": sortBy = "name"; sortDir = "asc"; break;
                case "name_desc": sortBy = "name"; sortDir = "desc"; break;
                case "price_asc": sortBy = "price"; sortDir = "asc"; break;
                case "price_desc": sortBy = "price"; sortDir = "desc"; break;
                default: break;
            }
        }
        Pageable pageable;
        if (sortBy != null && sortDir != null) {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortDir.equalsIgnoreCase("asc") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, sortBy));
        } else {
            pageable = PageRequest.of(page, size);
        }
        Page<Product> products = productService.searchProductsWithFilters(
                keyword, categoryId, brandId, minPrice, maxPrice, null, sortBy, sortDir, pageable, null);
        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        // Bổ sung: Map productId -> discount (nếu có) để hiển thị giá gốc gạch ngang ngoài trang danh sách
        Map<Integer, Discount> productDiscountMap = new HashMap<>();
        for (Product product : products) {
            Discount discount = productService.getActiveDiscountForProduct(product);
            if (discount != null) {
                productDiscountMap.put(product.getProductId(), discount);
            }
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("priceError", priceError);
        // Bổ sung: truyền map discount ra view
        model.addAttribute("productDiscountMap", productDiscountMap);
        
        return "customer/product/viewProduct";
    }

    @GetMapping("/Customer/Wishlist")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public String wishlistPage() {
        return "customer/wishlist/Wishlist";
    }

    @GetMapping("/Product/{id}")
    public String viewProductDetail(
            @PathVariable("id") Integer id,
            Model model) {
        Product product = productService.getProductById(id);
        // Lấy discount cho sản phẩm này (nếu có)
        com.esms.model.entity.Discount discount = productService.getActiveDiscountForProduct(product);
        String primaryImage = product.getImages()
                .stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl).findFirst().orElse(product.getImageUrl());
        model.addAttribute("product", product);
        model.addAttribute("discount", discount);
        model.addAttribute("primaryImage", primaryImage);
        model.addAttribute("specifications", product.getSpecifications());
        model.addAttribute("imageGallery", product.getImages());
        return "customer/product/viewProductDetail";
    }

    @GetMapping("/DiscountProducts")
    public String viewDiscountProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        // Price validation logic (relaxed for discount page)
        String priceError = null;
        if (minPrice != null && minPrice < 0) {
            priceError = "Minimum price cannot be negative";
            minPrice = null;
        }
        if (maxPrice != null && maxPrice < 0) {
            priceError = "Maximum price cannot be negative";
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            priceError = "Minimum price cannot be greater than maximum price";
            minPrice = null;
            maxPrice = null;
        }
        // Parse sortBy and sortDir from sortOption
        String sortBy = null;
        String sortDir = null;
        if (sortOption != null && !sortOption.equals("default")) {
            switch (sortOption) {
                case "name_asc": sortBy = "name"; sortDir = "asc"; break;
                case "name_desc": sortBy = "name"; sortDir = "desc"; break;
                case "price_asc": sortBy = "price"; sortDir = "asc"; break;
                case "price_desc": sortBy = "price"; sortDir = "desc"; break;
                default: break;
            }
        }
        Pageable pageable;
        if (sortBy != null && sortDir != null) {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortDir.equalsIgnoreCase("asc") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, sortBy));
        } else {
            pageable = PageRequest.of(page, size);
        }
        Page<Product> products = productService.searchDiscountProductsWithFilters(
                keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortDir, pageable);
        // Nếu page vượt quá giới hạn, chuyển về trang cuối cùng
        if (page >= products.getTotalPages() && products.getTotalPages() > 0) {
            int lastPage = products.getTotalPages() - 1;
            pageable = PageRequest.of(lastPage, size, pageable.getSort());
            products = productService.searchDiscountProductsWithFilters(
                keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortDir, pageable);
            page = lastPage;
        }
        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        // Map productId -> discount (nếu có)
        Map<Integer, Discount> productDiscountMap = new HashMap<>();
        for (Product product : products) {
            Discount discount = productService.getActiveDiscountForProduct(product);
            if (discount != null) {
                productDiscountMap.put(product.getProductId(), discount);
            }
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("priceError", priceError);
        model.addAttribute("productDiscountMap", productDiscountMap);
        
        return "customer/product/discountProducts";
    }

    @GetMapping("/Product/api/products/suggest")
    @ResponseBody
    public Map<String, Object> suggestProducts(@RequestParam("q") String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        // Danh sách từ khóa mặc định - mở rộng thêm nhiều từ khóa hơn
        List<String> defaultKeywords = List.of(
            "Tivi", "Tủ lạnh", "Máy lạnh", "Robot hút bụi", "Quạt", "Máy lọc nước",
            "Máy giặt", "Lò vi sóng", "Bếp điện", "Nồi cơm điện", "Máy xay sinh tố",
            "Máy hút bụi", "Quạt điều hòa", "Điều hòa", "Tủ đông", "Tủ mát"
        );
        
        // Cải thiện logic lọc từ khóa gợi ý
        List<String> filteredKeywords;
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu không có keyword, hiển thị tất cả từ khóa phổ biến
            filteredKeywords = defaultKeywords.stream()
                .limit(6)
                .toList();
        } else {
            String searchTerm = keyword.toLowerCase().trim();
            // Lọc từ khóa chứa search term (không phân biệt hoa thường)
            filteredKeywords = defaultKeywords.stream()
                .filter(k -> k.toLowerCase().contains(searchTerm))
                .distinct()
                .limit(8)
                .toList();
        }
        
        result.put("keywords", filteredKeywords);
        // Gợi ý sản phẩm với cache
        List<Product> products = productService.searchProducts(keyword);
        List<Map<String, Object>> productList = products.stream()
            .limit(6)
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getProductId());
                map.put("name", p.getName());
                map.put("image", p.getImageUrl());
                map.put("price", p.getPrice());
                // Thêm discount nếu có
                Discount discount = productService.getActiveDiscountForProduct(p);
                if (discount != null) {
                    map.put("hasDiscount", true);
                    map.put("discountPercent", discount.getDiscountPct() != null ? discount.getDiscountPct().intValue() : 0);
                } else {
                    map.put("hasDiscount", false);
                }
                return map;
            })
            .toList();
        result.put("products", productList);
        return result;
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "categoryId", required = false) Integer categoryId,
                             @RequestParam(value = "brandId", required = false) Integer brandId,
                             @RequestParam(value = "minPrice", required = false) Double minPrice,
                             @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                             @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "12") int size,
                             Model model) {
        String priceError = null;
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            priceError = "Minimum price cannot be greater than maximum price";
            minPrice = null;
            maxPrice = null;
        }
        String sortBy = null;
        String sortDir = null;
        if (sortOption != null && !sortOption.equals("default")) {
            switch (sortOption) {
                case "name_asc": sortBy = "name"; sortDir = "asc"; break;
                case "name_desc": sortBy = "name"; sortDir = "desc"; break;
                case "price_asc": sortBy = "price"; sortDir = "asc"; break;
                case "price_desc": sortBy = "price"; sortDir = "desc"; break;
                default: break;
            }
        }
        Pageable pageable = (sortBy != null && sortDir != null)
            ? PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortDir.equalsIgnoreCase("asc") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, sortBy))
            : PageRequest.of(page, size);
        Page<Product> products = productService.searchProductsWithFilters(
            keyword, categoryId, brandId, minPrice, maxPrice, null, sortBy, sortDir, pageable, null);
        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("priceError", priceError);
        
        return "search";
    }
    

} 