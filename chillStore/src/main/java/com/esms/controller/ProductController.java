package com.esms.controller;

import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Product;
import com.esms.service.BrandService;
import com.esms.service.CategoryService;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model) {

        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        List<Integer> availablePageSizes = List.of(5, 9, 15, 30);
        // Define sort options for the dropdown
        List<Map<String, String>> sortOptions = List.of(
            Map.of("value", "default", "label", "Mặc định"),
            Map.of("value", "name_asc", "label", "Tên (A-Z)"),
            Map.of("value", "name_desc", "label", "Tên (Z-A)"),
            Map.of("value", "price_asc", "label", "Giá tăng dần"),
            Map.of("value", "price_desc", "label", "Giá giảm dần")
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
                keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortDir, pageable, filterStatus);
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
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("sortOptions", sortOptions);
        model.addAttribute("size", size);
        model.addAttribute("availablePageSizes", availablePageSizes);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "admin/ManageProduct/Product";
    }

    @GetMapping("/{id}")
    public String productDetail(@org.springframework.web.bind.annotation.PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/ManageProduct/ProductDetail";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@org.springframework.web.bind.annotation.PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategory();
        List<Brand> brands = brandService.getAllBrands();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        return "admin/ManageProduct/ProductForm";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@org.springframework.web.bind.annotation.PathVariable("id") Integer id,
                                @org.springframework.web.bind.annotation.ModelAttribute("product") Product product,
                                @org.springframework.web.bind.annotation.RequestParam(value = "filterStatus", required = false) Boolean filterStatus,
                                @org.springframework.web.bind.annotation.RequestParam(value = "keyword", required = false) String keyword,
                                @org.springframework.web.bind.annotation.RequestParam(value = "categoryId", required = false) Integer categoryId,
                                @org.springframework.web.bind.annotation.RequestParam(value = "brandId", required = false) Integer brandId,
                                @org.springframework.web.bind.annotation.RequestParam(value = "page", required = false) Integer page,
                                @org.springframework.web.bind.annotation.RequestParam(value = "size", required = false) Integer size,
                                org.springframework.ui.Model model) {
        productService.updateProduct(id, product);
        String redirectUrl = String.format("redirect:/admin/products?filterStatus=%s&keyword=%s&categoryId=%s&brandId=%s&page=%s&size=%s",
                filterStatus != null ? filterStatus : "",
                keyword != null ? keyword : "",
                categoryId != null ? categoryId : "",
                brandId != null ? brandId : "",
                page != null ? page : "",
                size != null ? size : "");
        return redirectUrl;
    }
}
