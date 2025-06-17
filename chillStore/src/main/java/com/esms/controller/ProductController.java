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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;

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
            @RequestParam(value = "minStock", required = false) Integer minStock,
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
        return "admin/ManageProduct/Product";
    }

    @GetMapping("/{id}")
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
            Model model) {
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
        return "admin/ManageProduct/ProductDetail";
    }

    @GetMapping("/{id}/edit")
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
    public String updateProduct(
            @PathVariable("id") Integer id,
            @ModelAttribute("product") Product product,
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
}
