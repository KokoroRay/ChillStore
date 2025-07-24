package com.esms.controller;

import com.esms.model.entity.*;
import com.esms.repository.BrandRepository;
import com.esms.repository.CategoryRepository;
import com.esms.service.BrandService;
import com.esms.service.CategoryService;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.esms.model.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.esms.model.entity.Customer;
import com.esms.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.esms.service.FeedbackService;
import com.esms.model.entity.Feedback;
import com.esms.repository.OrderRepository;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.security.Principal;
import com.esms.service.ReplyService;
import com.esms.model.entity.Reply;
import com.esms.model.dto.ReplyFeedbackDTO;
import com.esms.model.entity.Staff;
import com.esms.repository.StaffRepository;
import com.esms.model.dto.FeedbackDTO;
import org.springframework.http.ResponseEntity;

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
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private StaffRepository staffRepository;

    @GetMapping("/Product")
    public String viewProductPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
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
        // Map productId -> total sold quantity for displaying "đã bán"
        Map<Integer, Integer> productSoldMap = new HashMap<>();
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
        model.addAttribute("productSoldMap", productSoldMap);
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

        // Get current customer information if logged in
        Customer customer = getCurrentCustomer();
        Integer shippingCost = calculateShippingCost(customer);
        int soldQuantity = productService.getTotalSoldQuantity(id);
        model.addAttribute("product", product);
        model.addAttribute("soldQuantity", soldQuantity);
        model.addAttribute("discount", discount);
        model.addAttribute("primaryImage", primaryImage);
        model.addAttribute("specifications", product.getSpecifications());
        model.addAttribute("imageGallery", product.getImages());
        model.addAttribute("customer", customer);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("stockQty", product.getStockQty());
        // Thêm biến currentRole cho Thymeleaf
        String currentRole = "GUEST";
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
                for (var authority : authentication.getAuthorities()) {
                    String role = authority.getAuthority();
                    if (role.contains("ADMIN")) currentRole = "ADMIN";
                    else if (role.contains("STAFF")) currentRole = "STAFF";
                    else if (role.contains("CUSTOMER")) currentRole = "CUSTOMER";
                }
            }
        } catch (Exception e) { }
        model.addAttribute("currentRole", currentRole);
        return "customer/product/viewProductDetail";
    }

    /**
     * Gets the currently logged-in customer
     */
    private Customer getCurrentCustomer() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                String username = authentication.getName();
                return customerService.getCustomerByUsername(username);
            }
        } catch (Exception e) {
            // Log error but don't disrupt the flow
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculate shipping cost based on customer's address
     * Free for Can Tho, 20,000 VND for other southern locations, 40,000 VND for northern locations
     */
    private Integer calculateShippingCost(Customer customer) {
        if (customer == null || customer.getAddress() == null) {
            return 20000; // Default shipping cost
        }

        String address = customer.getAddress().toLowerCase();

        // Free shipping for Can Tho
        if (address.contains("cần thơ") || address.contains("can tho")) {
            return 0;
        }

        // List of northern provinces (simplified)
        String[] northernProvinces = {
            "hà nội", "hanoi", "hải phòng", "haiphong", "bắc ninh", "bắc giang", "lào cai",
            "lao cai", "điện biên", "dien bien", "hòa bình", "hoa binh", "lai châu", "lai chau",
            "sơn la", "son la", "hà giang", "ha giang", "cao bằng", "cao bang", "bắc kạn", "bac kan",
            "lạng sơn", "lang son", "tuyên quang", "tuyen quang", "thái nguyên", "thai nguyen",
            "phú thọ", "phu tho", "vĩnh phúc", "vinh phuc", "quảng ninh", "quang ninh",
            "hải dương", "hai duong", "hưng yên", "hung yen", "thái bình", "thai binh",
            "hà nam", "ha nam", "nam định", "nam dinh", "ninh bình", "ninh binh", "thanh hóa", "thanh hoa"
        };

        // Check if address contains any northern province
        for (String province : northernProvinces) {
            if (address.contains(province)) {
                return 40000; // Northern provinces shipping cost
            }
        }

        // Default for other southern provinces
        return 20000;
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @GetMapping("/products/category/{categoryId}")
    public String viewProductsByCategory(
            @PathVariable("categoryId") Integer categoryId,
            Model model) {
                Pageable pageable = PageRequest.of(0, 100); // Get a reasonable number of products
                Page<ProductDTO> productDTOsPage = productService.getProductsByCategory(categoryId.toString(), pageable);
                List<ProductDTO> products = productDTOsPage.getContent();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        model.addAttribute("products", products);
        model.addAttribute("category", category);
        model.addAttribute("title", "Products in " + category.getName());

        return "customer/product/viewProduct";
    }

    @GetMapping("/products/brand/{brandId}")
    public String viewProductsByBrand(
            @PathVariable("brandId") Integer brandId,
            Model model) {
                // Get products by brand using the searchProductsWithFilters method
                Pageable pageable = PageRequest.of(0, 100); // Get a reasonable number of products
                Page<Product> productsPage = productService.searchProductsWithFilters(
                    null, null, brandId, null, null, null, null, null, pageable, true);
                List<Product> products = productsPage.getContent();

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        model.addAttribute("products", products);
        model.addAttribute("brand", brand);
        model.addAttribute("title", "Products by " + brand.getName());

        return "customer/product/viewProduct";
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
        // Map productId -> total sold quantity for displaying "đã bán"
        Map<Integer, Integer> productSoldMap = new HashMap<>();
        for (Product product : products) {
            int soldQty = productService.getTotalSoldQuantity(product.getProductId());
            productSoldMap.put(product.getProductId(), soldQty);
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
        model.addAttribute("productSoldMap", productSoldMap);
        return "customer/product/discountProducts";
    }

    // API: Lấy danh sách feedback cho sản phẩm (có phân trang)
    @GetMapping("/api/product/{productId}/feedbacks")
    @ResponseBody
    public Map<String, Object> getFeedbacksByProduct(
            @PathVariable("productId") Integer productId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "rating", required = false) Byte rating) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<FeedbackDTO> feedbackPage;
        if (rating != null && rating >= 1 && rating <= 5) {
            feedbackPage = feedbackService.getFeedbacksByProductIdAndRatingPaged(productId, rating, pageable);
        } else {
            feedbackPage = feedbackService.getFeedbacksByProductIdPaged(productId, pageable);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("content", feedbackPage.getContent());
        result.put("totalElements", feedbackPage.getTotalElements());
        result.put("totalPages", feedbackPage.getTotalPages());
        result.put("currentPage", feedbackPage.getNumber());
        return result;
    }

    // API: Thêm feedback (chỉ cho user đã mua hàng, chưa feedback)
    @PostMapping("/api/product/{productId}/feedback")
    @ResponseBody
    public Feedback addFeedback(@PathVariable("productId") Integer productId, @RequestBody Feedback feedback, Principal principal) {
        Customer customer = getCurrentCustomer();
        System.out.println("Add feedback: customer=" + (customer != null ? customer.getCustomerId() : "null") + ", productId=" + productId);
        if (customer == null) throw new RuntimeException("Bạn cần đăng nhập để bình luận!");
        boolean hasDelivered = orderRepository.hasCustomerDeliveredProduct(customer.getCustomerId(), productId);
        System.out.println("hasDelivered: " + hasDelivered);
        if (!hasDelivered) throw new RuntimeException("Bạn chỉ có thể bình luận khi đơn hàng đã giao thành công!");
        return feedbackService.addFeedback(customer.getCustomerId(), productId, feedback.getRating(), feedback.getComment());
    }

    // API: Sửa feedback (chỉ cho chủ feedback)
    @PutMapping("/api/feedback/{feedbackId}")
    @ResponseBody
    public Feedback updateFeedback(@PathVariable("feedbackId") Integer feedbackId, @RequestBody Feedback feedback, Principal principal) {
        Customer customer = getCurrentCustomer();
        if (customer == null) throw new RuntimeException("Bạn cần đăng nhập để sửa bình luận!");
        return feedbackService.updateFeedback(feedbackId, feedback.getRating(), feedback.getComment(), customer.getCustomerId());
    }

    // API: Xóa feedback (chỉ cho chủ feedback)
    @DeleteMapping("/api/feedback/{feedbackId}")
    @ResponseBody
    public void deleteFeedback(@PathVariable("feedbackId") Integer feedbackId, Principal principal) {
        Customer customer = getCurrentCustomer();
        if (customer == null) throw new RuntimeException("Bạn cần đăng nhập để xóa bình luận!");
        feedbackService.deleteFeedback(feedbackId, customer.getCustomerId());
    }

    // API: Lấy reply cho feedback
    @GetMapping("/api/feedback/{feedbackId}/reply")
    @ResponseBody
    public Map<String, Object> getReplyByFeedbackId(@PathVariable("feedbackId") int feedbackId) {
        Reply reply = replyService.getReplyByFeedbackId(feedbackId);
        if (reply == null) {
            return new HashMap<>();
        }
        Map<String, Object> dto = new HashMap<>();
        dto.put("content", reply.getContent());
        dto.put("createdAt", reply.getCreatedAt());
        if (reply.getStaff() != null) {
            Map<String, Object> staff = new HashMap<>();
            staff.put("name", reply.getStaff().getName());
            staff.put("email", reply.getStaff().getEmail());
            dto.put("staff", staff);
        }
        if (reply.getAdmin() != null) {
            Map<String, Object> admin = new HashMap<>();
            admin.put("name", reply.getAdmin().getName());
            admin.put("email", reply.getAdmin().getEmail());
            dto.put("admin", admin);
        }
        return dto;
    }

    // API: Staff/Admin gửi reply cho feedback
    @PostMapping("/api/feedback/{feedbackId}/reply")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Reply replyToFeedback(@PathVariable("feedbackId") int feedbackId, @RequestBody ReplyFeedbackDTO dto, Principal principal) {
        String username = principal.getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer staffIdToLog = null;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            // Lấy staff đầu tiên trong DB để dùng làm staffId
            Staff anyStaff = staffRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No staff found!"));
            staffIdToLog = anyStaff.getId();
        } else {
            Staff staff = staffRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Staff account not found!"));
            staffIdToLog = staff.getId();
        }
        dto.setFeedbackId(feedbackId);
        dto.setStaffId(staffIdToLog);
        replyService.saveReply(dto);
        return replyService.getReplyByFeedbackId(feedbackId);
    }

    // API: Staff/Admin sửa reply cho feedback
    @PutMapping("/api/feedback/{feedbackId}/reply")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Reply updateReplyToFeedback(@PathVariable("feedbackId") int feedbackId, @RequestBody ReplyFeedbackDTO dto, Principal principal) {
        String username = principal.getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer staffIdToLog = null;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            Staff anyStaff = staffRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No staff found!"));
            staffIdToLog = anyStaff.getId();
        } else {
            Staff staff = staffRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Staff account not found!"));
            staffIdToLog = staff.getId();
        }
        dto.setFeedbackId(feedbackId);
        return replyService.updateReply(feedbackId, dto, staffIdToLog);
    }

    @GetMapping("/api/product/{productId}/can-review")
    @ResponseBody
    public boolean canReview(@PathVariable("productId") Integer productId, Principal principal) {
        Customer customer = getCurrentCustomer();
        if (customer == null) return false;
        return orderRepository.hasCustomerDeliveredProduct(customer.getCustomerId(), productId);
    }
}