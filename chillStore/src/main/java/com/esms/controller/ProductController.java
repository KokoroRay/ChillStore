package com.esms.controller;

import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Product;
import com.esms.model.entity.ProductImage;
import com.esms.model.entity.ProductSpecification;
import com.esms.service.BrandService;
import com.esms.service.CategoryService;
import com.esms.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({"/admin/products", "/staff/products"})
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model, HttpServletRequest request) {

        // Validation logic for price range
        String priceError = null;
        if (minPrice != null && minPrice < 1000) {
            priceError = "Minimum price must be at least 1,000 VND";
            minPrice = null; // Reset to null to ignore invalid input
        }
        if (maxPrice != null && maxPrice > 99999999.99) {
            priceError = "Maximum price cannot exceed 99,999,999.99 VND";
            maxPrice = null; // Reset to null to ignore invalid input
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            priceError = "Minimum price cannot be greater than maximum price";
            minPrice = null;
            maxPrice = null;
        }

        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        List<Integer> availablePageSizes = List.of(5, 9, 15, 30);
        // Define sort options for the dropdown
        List<Map<String, String>> sortOptions = List.of(
                Map.of("value", "default", "label", "Default"),
                Map.of("value", "name_asc", "label", "Name (A-Z)"),
                Map.of("value", "name_desc", "label", "Name (Z-A)"),
                Map.of("value", "price_asc", "label", "Price (Low to High)"),
                Map.of("value", "price_desc", "label", "Price (High to Low)")
        );
        // Parse sortBy and sortDir from sortOption
        String sortBy = null;
        String sortDir = null;
        if (sortOption != null && !sortOption.equals("default")) {
            sortBy = "name";
            sortDir = "asc";
            switch (sortOption) {
                case "name_desc": sortBy = "name"; sortDir = "desc"; break;
                case "price_asc": sortBy = "price"; sortDir = "asc"; break;
                case "price_desc": sortBy = "price"; sortDir = "desc"; break;
                default: break;
            }
        }
        Pageable pageable;
        if (sortBy != null && sortDir != null) {
            pageable = PageRequest.of(page, size, Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
        } else {
            pageable = PageRequest.of(page, size);
        }
        Page<Product> products = productService.searchProductsWithFilters(
                keyword, categoryId, brandId, minPrice, maxPrice, minStock, sortBy, sortDir, pageable, filterStatus);
        // Pagination logic for page numbers
        int totalPages = products.getTotalPages();
        int currentPage = page;
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minStock", minStock);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("sortOptions", sortOptions);
        model.addAttribute("size", size);
        model.addAttribute("availablePageSizes", availablePageSizes);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("priceError", priceError);
        String requestURI = request.getRequestURI();
        if(requestURI.startsWith("/staff")){
            return "staff/ManageProduct/Product";
        } else {
            return "admin/ManageProduct/Product";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String productDetail(
            @PathVariable("id") Integer id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false) String sortOption,
            Model model, HttpServletRequest request) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minStock", minStock);
        model.addAttribute("sortOption", sortOption);

        // Determine current role for the view
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = "GUEST"; // Default
        if (authentication != null) {
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                currentRole = "ADMIN";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                currentRole = "STAFF";
            }
        }
        model.addAttribute("currentRole", currentRole);

        String requestURI = request.getRequestURI();
        if(requestURI.startsWith("/staff")){
            return "staff/ManageProduct/ProductDetail";
        } else {
            return "admin/ManageProduct/ProductDetail";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editProduct(
            @PathVariable("id") Integer id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false) String sortOption,
            Model model) {
        Product product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        // Lấy danh sách link ảnh phụ (chỉ lấy link, không lấy file local)
        List<String> galleryLinks = new ArrayList<>();
        if (product.getImages() != null) {
            for (ProductImage img : product.getImages()) {
                if (img.getImageUrl() != null && img.getImageUrl().startsWith("http")) {
                    galleryLinks.add(img.getImageUrl());
                }
            }
        }
        model.addAttribute("galleryImageLinks", galleryLinks);
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minStock", minStock);
        model.addAttribute("sortOption", sortOption);
        return "admin/ManageProduct/ProductForm";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String updateProduct(
            @PathVariable("id") Integer id,
            @ModelAttribute("product") Product product,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "galleryImages", required = false) MultipartFile[] galleryImages,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "specKeys", required = false) String[] specKeys,
            @RequestParam(value = "specValues", required = false) String[] specValues,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false) String sortOption,
            Model model,
            HttpServletRequest request
    ) {
        // Xử lý giá trị price nếu là String có dấu chấm hoặc phẩy
        String priceParam = request.getParameter("price");
        if (priceParam != null && !priceParam.isEmpty()) {
            String numericPrice = priceParam.replaceAll("[^\\d]", "");
            try {
                BigDecimal price = new java.math.BigDecimal(numericPrice);
                // Kiểm tra giá trị không vượt quá giới hạn (decimal(10,2) = max 99,999,999.99)
                if (price.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
                    model.addAttribute("priceError", "Product price cannot exceed 99,999,999.99 VND!");
                    model.addAttribute("product", productService.getProductById(id));
                    model.addAttribute("categories", categoryService.getAllCategory());
                    model.addAttribute("brands", brandService.getAllBrands());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("size", size);
                    model.addAttribute("keyword", keyword);
                    model.addAttribute("categoryId", categoryId);
                    model.addAttribute("brandId", brandId);
                    model.addAttribute("filterStatus", filterStatus);
                    model.addAttribute("minPrice", minPrice);
                    model.addAttribute("maxPrice", maxPrice);
                    model.addAttribute("minStock", minStock);
                    model.addAttribute("sortOption", sortOption);
                    return "admin/ManageProduct/ProductForm";
                }
                product.setPrice(price);
            } catch (Exception e) {
                model.addAttribute("priceError", "Invalid product price!");
                model.addAttribute("product", productService.getProductById(id));
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                model.addAttribute("currentPage", page);
                model.addAttribute("size", size);
                model.addAttribute("keyword", keyword);
                model.addAttribute("categoryId", categoryId);
                model.addAttribute("brandId", brandId);
                model.addAttribute("filterStatus", filterStatus);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
                model.addAttribute("minStock", minStock);
                model.addAttribute("sortOption", sortOption);
                return "admin/ManageProduct/ProductForm";
            }
        }

        // Xử lý giá trị price từ priceString (nếu có)
        if (product.getPriceString() != null && !product.getPriceString().isEmpty()) {
            String numericPrice = product.getPriceString().replaceAll("[^\\d]", "");
            try {
                BigDecimal price = new java.math.BigDecimal(numericPrice);
                // Kiểm tra giá trị không vượt quá giới hạn (decimal(10,2) = max 99,999,999.99)
                if (price.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
                    model.addAttribute("priceError", "Product price cannot exceed 99,999,999.99 VND!");
                    model.addAttribute("categories", categoryService.getAllCategory());
                    model.addAttribute("brands", brandService.getAllBrands());
                    return "admin/ManageProduct/ProductForm";
                }
                product.setPrice(price);
            } catch (Exception e) {
                model.addAttribute("priceError", "Invalid product price!");
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                return "admin/ManageProduct/ProductForm";
            }
        }

        // Kiểm tra giá trị price không được null hoặc âm
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("priceError", "Product price must be greater than 0!");
            model.addAttribute("product", productService.getProductById(id));
            model.addAttribute("categories", categoryService.getAllCategory());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("brandId", brandId);
            model.addAttribute("filterStatus", filterStatus);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("minStock", minStock);
            model.addAttribute("sortOption", sortOption);
            return "admin/ManageProduct/ProductForm";
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            product.setImageUrl(imageUrl);

        }
        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                // Generate unique filename
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = "product_" + System.currentTimeMillis() + fileExtension;

                // Save file to static/images directory
                String uploadDir = "src/main/resources/static/images/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                java.nio.file.Path filePath = uploadPath.resolve(filename);
                java.nio.file.Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Set image URL for product
                product.setImageUrl("/images/" + filename);
            } catch (Exception e) {
                // Handle file upload error
                model.addAttribute("priceError", "Error uploading image: " + e.getMessage());
                model.addAttribute("product", productService.getProductById(id));
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                model.addAttribute("currentPage", page);
                model.addAttribute("size", size);
                model.addAttribute("keyword", keyword);
                model.addAttribute("categoryId", categoryId);
                model.addAttribute("brandId", brandId);
                model.addAttribute("filterStatus", filterStatus);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
                model.addAttribute("minStock", minStock);
                model.addAttribute("sortOption", sortOption);
                return "admin/ManageProduct/ProductForm";
            }
        }

        // Xử lý gallery images (file upload)
        List<ProductImage> images = new ArrayList<>();
        if (galleryImages != null && galleryImages.length > 0) {
            for (MultipartFile file : galleryImages) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String filename = "product_gallery_" + System.currentTimeMillis() + "_" + originalFilename.hashCode() + fileExtension;
                        String uploadDir = "src/main/resources/static/images/";
                        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                        if (!java.nio.file.Files.exists(uploadPath)) {
                            java.nio.file.Files.createDirectories(uploadPath);
                        }
                        java.nio.file.Path filePath = uploadPath.resolve(filename);
                        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/images/" + filename);
                        img.setProduct(product);
                        img.setPrimary(false);
                        images.add(img);
                    } catch (Exception e) {
                        // Có thể log lỗi hoặc bỏ qua ảnh lỗi
                    }
                }
            }
        }
        // Xử lý gallery image links
        String[] galleryImageLinks = request.getParameterValues("galleryImageLinks");
        if (galleryImageLinks != null) {
            for (String link : galleryImageLinks) {
                if (link != null && !link.trim().isEmpty()) {
                    ProductImage img = new ProductImage();
                    img.setImageUrl(link.trim());
                    img.setProduct(product);
                    img.setPrimary(false);
                    images.add(img);
                }
            }
        }
        // Xóa ảnh phụ cũ trước khi set list mới
        Product existing = productService.getProductById(id);
        existing.getImages().clear();
        product.setImages(images);

        // Xử lý thông số kỹ thuật
        if (specKeys != null && specValues != null && specKeys.length == specValues.length) {
            List<ProductSpecification> specs = new ArrayList<>();
            for (int i = 0; i < specKeys.length; i++) {
                if (specKeys[i] != null && !specKeys[i].isBlank() && specValues[i] != null && !specValues[i].isBlank()) {
                    ProductSpecification spec = new ProductSpecification();
                    spec.setSpecKey(specKeys[i]);
                    spec.setSpecValue(specValues[i]);
                    spec.setProduct(product);
                    specs.add(spec);
                }
            }
            product.setSpecifications(specs);
        }

        productService.updateProduct(id, product);
        return String.format("redirect:/admin/products?page=%d&size=%d&keyword=%s&categoryId=%s&brandId=%s&filterStatus=%s&minPrice=%s&maxPrice=%s&minStock=%s&sortOption=%s",
                page, size,
                keyword != null ? keyword : "",
                categoryId != null ? categoryId : "",
                brandId != null ? brandId : "",
                filterStatus != null ? filterStatus : "",
                minPrice != null ? minPrice : "",
                maxPrice != null ? maxPrice : "",
                minStock != null ? minStock : "",
                sortOption != null ? sortOption : "");
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteProduct(
            @PathVariable("id") Integer id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false) String sortOption) {
        productService.deleteProduct(id);
        return String.format("redirect:/admin/products?page=%d&size=%d&keyword=%s&categoryId=%s&brandId=%s&filterStatus=%s&minPrice=%s&maxPrice=%s&minStock=%s&sortOption=%s",
                page, size,
                keyword != null ? keyword : "",
                categoryId != null ? categoryId : "",
                brandId != null ? brandId : "",
                filterStatus != null ? filterStatus : "",
                minPrice != null ? minPrice : "",
                maxPrice != null ? maxPrice : "",
                minStock != null ? minStock : "",
                sortOption != null ? sortOption : "");
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("brands", brandService.getAllBrands());
        return "admin/ManageProduct/ProductForm";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String addProduct(
            @ModelAttribute("product") Product product,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "galleryImages", required = false) MultipartFile[] galleryImages,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "specKeys", required = false) String[] specKeys,
            @RequestParam(value = "specValues", required = false) String[] specValues,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "sortOption", required = false) String sortOption,
            Model model,
            HttpServletRequest request
    ) {
        // Xử lý giá trị price nếu là String có dấu chấm hoặc phẩy
        String priceParam = request.getParameter("price");
        if (priceParam != null && !priceParam.isEmpty()) {
            String numericPrice = priceParam.replaceAll("[^\\d]", "");
            try {
                BigDecimal price = new java.math.BigDecimal(numericPrice);
                // Kiểm tra giá trị không vượt quá giới hạn (decimal(10,2) = max 99,999,999.99)
                if (price.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
                    model.addAttribute("priceError", "Product price cannot exceed 99,999,999.99 VND!");
                    model.addAttribute("categories", categoryService.getAllCategory());
                    model.addAttribute("brands", brandService.getAllBrands());
                    return "admin/ManageProduct/ProductForm";
                }
                product.setPrice(price);
            } catch (Exception e) {
                model.addAttribute("priceError", "Invalid product price!");
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                return "admin/ManageProduct/ProductForm";
            }
        }

        // Xử lý giá trị price từ priceString (nếu có)
        if (product.getPriceString() != null && !product.getPriceString().isEmpty()) {
            String numericPrice = product.getPriceString().replaceAll("[^\\d]", "");
            try {
                BigDecimal price = new java.math.BigDecimal(numericPrice);
                // Kiểm tra giá trị không vượt quá giới hạn (decimal(10,2) = max 99,999,999.99)
                if (price.compareTo(new java.math.BigDecimal("99999999.99")) > 0) {
                    model.addAttribute("priceError", "Product price cannot exceed 99,999,999.99 VND!");
                    model.addAttribute("categories", categoryService.getAllCategory());
                    model.addAttribute("brands", brandService.getAllBrands());
                    return "admin/ManageProduct/ProductForm";
                }
                product.setPrice(price);
            } catch (Exception e) {
                model.addAttribute("priceError", "Invalid product price!");
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                return "admin/ManageProduct/ProductForm";
            }
        }

        // Kiểm tra giá trị price không được null hoặc âm
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("priceError", "Product price must be greater than 0!");
            model.addAttribute("categories", categoryService.getAllCategory());
            model.addAttribute("brands", brandService.getAllBrands());
            return "admin/ManageProduct/ProductForm";
        }

        if (categoryId != null) {
            Category category = new Category();
            category.setId(categoryId);
            product.setCategory(category);
        }
        if (brandId != null) {
            Brand brand = new Brand();
            brand.setId(brandId);
            product.setBrand(brand);
        }

        // Handle image upload or link
        if (image != null && !image.isEmpty()) {
            try {
                // Generate unique filename
                String originalFilename = image.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = "product_" + System.currentTimeMillis() + fileExtension;

                // Save file to static/images directory
                String uploadDir = "src/main/resources/static/images/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                java.nio.file.Path filePath = uploadPath.resolve(filename);
                java.nio.file.Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Set image URL for product
                product.setImageUrl("/images/" + filename);
            } catch (Exception e) {
                // Handle file upload error
                model.addAttribute("priceError", "Error uploading image: " + e.getMessage());
                model.addAttribute("categories", categoryService.getAllCategory());
                model.addAttribute("brands", brandService.getAllBrands());
                return "admin/ManageProduct/ProductForm";
            }
        } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            product.setImageUrl(imageUrl.trim());
        }

        // Handle gallery images (file upload)
        List<ProductImage> images = new ArrayList<>();
        if (galleryImages != null && galleryImages.length > 0) {
            for (MultipartFile file : galleryImages) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String filename = "product_gallery_" + System.currentTimeMillis() + "_" + originalFilename.hashCode() + fileExtension;
                        String uploadDir = "src/main/resources/static/images/";
                        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                        if (!java.nio.file.Files.exists(uploadPath)) {
                            java.nio.file.Files.createDirectories(uploadPath);
                        }
                        java.nio.file.Path filePath = uploadPath.resolve(filename);
                        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/images/" + filename);
                        img.setProduct(product);
                        img.setPrimary(false);
                        images.add(img);
                    } catch (Exception e) {
                        // Có thể log lỗi hoặc bỏ qua ảnh lỗi
                    }
                }
            }
        }

        // Handle gallery image links
        String[] galleryImageLinks = request.getParameterValues("galleryImageLinks");
        if (galleryImageLinks != null) {
            for (String link : galleryImageLinks) {
                if (link != null && !link.trim().isEmpty()) {
                    ProductImage img = new ProductImage();
                    img.setImageUrl(link.trim());
                    img.setProduct(product);
                    img.setPrimary(false);
                    images.add(img);
                }
            }
        }
        if (!images.isEmpty()) {
            product.setImages(images);
        }

        // Xử lý thông số kỹ thuật
        if (specKeys != null && specValues != null && specKeys.length == specValues.length) {
            List<ProductSpecification> specs = new ArrayList<>();
            for (int i = 0; i < specKeys.length; i++) {
                if (specKeys[i] != null && !specKeys[i].isBlank() && specValues[i] != null && !specValues[i].isBlank()) {
                    ProductSpecification spec = new ProductSpecification();
                    spec.setSpecKey(specKeys[i]);
                    spec.setSpecValue(specValues[i]);
                    spec.setProduct(product);
                    specs.add(spec);
                }
            }
            product.setSpecifications(specs);
        }

        productService.saveProduct(product);
        return String.format("redirect:/admin/products?page=%d&size=%d&keyword=%s&categoryId=%s&brandId=%s&filterStatus=%s&minPrice=%s&maxPrice=%s&minStock=%s&sortOption=%s",
                page, size,
                keyword != null ? keyword : "",
                categoryId != null ? categoryId : "",
                brandId != null ? brandId : "",
                filterStatus != null ? filterStatus : "",
                minPrice != null ? minPrice : "",
                maxPrice != null ? maxPrice : "",
                minStock != null ? minStock : "",
                sortOption != null ? sortOption : "");
    }
}
