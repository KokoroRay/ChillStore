package com.esms.controller;


import com.esms.model.dto.ProductDTO;
import com.esms.service.ProductService;
import com.esms.service.CategoryService;
import com.esms.service.BrandService;
import com.esms.service.DiscountService;
import com.esms.model.entity.Category;
import com.esms.model.entity.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class HomeController {

    // ----Product----
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private DiscountService discountService;

    @GetMapping({"/", "/home"})
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "categoryId", required = false) Integer categoryId,
                       @RequestParam(value = "brandId", required = false) Integer brandId,
                       @RequestParam(value = "minPrice", required = false) Double minPrice,
                       @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                       @RequestParam(value = "sortOption", required = false, defaultValue = "default") String sortOption) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", authentication.getName());
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        // Lấy danh mục và thương hiệu cho filter
        var categories = categoryService.getAllCategory();
        var brands = brandService.getAllBrands();
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);

        // --- Thêm logic lấy sản phẩm giảm giá nổi bật ---
        // Lấy tối đa 10 sản phẩm giảm giá nổi bật
        var discountPage = productService.getDiscountProducts(org.springframework.data.domain.PageRequest.of(0, 10));
        var highlightDiscountProducts = discountPage.getContent();
        java.util.Map<Integer, com.esms.model.entity.Discount> productDiscountMap = new java.util.HashMap<>();
        for (var product : highlightDiscountProducts) {
            var discount = productService.getActiveDiscountForProduct(product);
            if (discount != null) {
                productDiscountMap.put(product.getProductId(), discount);
            }
        }
        model.addAttribute("highlightDiscountProducts", highlightDiscountProducts);
        model.addAttribute("productDiscountMap", productDiscountMap);
        // --- End logic sản phẩm giảm giá ---

        // Nếu có filter thì search, không thì random
        boolean hasFilter = keyword != null || categoryId != null || brandId != null || minPrice != null || maxPrice != null || (sortOption != null && !sortOption.equals("default"));
        org.springframework.data.domain.Page<com.esms.model.entity.Product> productsPage;
        if (hasFilter) {
            // Parse sortBy/sortDir từ sortOption
            String sortBy = null, sortDir = null;
            if (sortOption != null && !sortOption.equals("default")) {
                switch (sortOption) {
                    case "name_asc": sortBy = "name"; sortDir = "asc"; break;
                    case "name_desc": sortBy = "name"; sortDir = "desc"; break;
                    case "price_asc": sortBy = "price"; sortDir = "asc"; break;
                    case "price_desc": sortBy = "price"; sortDir = "desc"; break;
                    default: break;
                }
            }
            org.springframework.data.domain.Pageable pageable;
            if (sortBy != null && sortDir != null) {
                pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortDir.equalsIgnoreCase("asc") ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, sortBy));
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(page, size);
            }
            productsPage = productService.searchProductsWithFilters(keyword, categoryId, brandId, minPrice, maxPrice, null, sortBy, sortDir, pageable, null);
        } else {
            // Random sản phẩm
            var products = productService.getRandomProductsPaged(page, size);
            int total = productService.getRandomProductsTotalPages(size);
            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", total);
            model.addAttribute("nextPage", Math.min(page + 1, total - 1));
            model.addAttribute("prevPage", Math.max(page - 1, 0));
            // Truyền filter để giữ UI
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("brandId", brandId);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("sortOption", sortOption);
            model.addAttribute("size", size);
            return "home";
        }
        // Nếu có filter, truyền Page<Product> ra view
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("nextPage", Math.min(page + 1, Math.max(productsPage.getTotalPages() - 1, 0)));
        model.addAttribute("prevPage", Math.max(page - 1, 0));
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("size", size);
        return "home";
    }

    @GetMapping(value = "/api/search/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> autocompleteSearch(@RequestParam("keyword") String keyword) {
        Map<String, Object> result = new HashMap<>();
        // Từ khóa gợi ý (cứng hoặc lấy từ DB)
        List<String> suggestedKeywords = List.of(
            "máy lạnh", "tủ lạnh", "robot hút bụi", "LG", "Samsung", "Sharp", "máy giặt", "quạt điều hòa", "bếp từ", "máy lọc nước"
        );
        String kwLower = keyword.toLowerCase();
        // Ưu tiên từ bắt đầu bằng ký tự nhập vào
        List<String> startsWith = suggestedKeywords.stream()
            .filter(k -> k.toLowerCase().startsWith(kwLower))
            .toList();
        List<String> contains = suggestedKeywords.stream()
            .filter(k -> !k.toLowerCase().startsWith(kwLower) && k.toLowerCase().contains(kwLower))
            .toList();
        List<String> filteredKeywords = new java.util.ArrayList<>();
        filteredKeywords.addAll(startsWith);
        filteredKeywords.addAll(contains);
        if (filteredKeywords.size() > 5) filteredKeywords = filteredKeywords.subList(0, 5);
        // Sản phẩm gợi ý
        List<com.esms.model.entity.Product> products = productService.searchProducts(keyword);
        List<com.esms.model.entity.Product> startsWithProd = products.stream()
            .filter(p -> p.getName() != null && p.getName().toLowerCase().startsWith(kwLower))
            .toList();
        List<com.esms.model.entity.Product> containsProd = products.stream()
            .filter(p -> p.getName() != null && !p.getName().toLowerCase().startsWith(kwLower) && p.getName().toLowerCase().contains(kwLower))
            .toList();
        List<com.esms.model.entity.Product> finalProducts = new java.util.ArrayList<>();
        finalProducts.addAll(startsWithProd);
        finalProducts.addAll(containsProd);
        if (finalProducts.size() > 5) finalProducts = finalProducts.subList(0, 5);
        List<Map<String, Object>> productList = finalProducts.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", p.getProductId());
            m.put("name", p.getName());
            m.put("imageUrl", p.getImageUrl());
            m.put("priceFormatted", String.format("%,.0f₫", p.getPrice() != null ? p.getPrice() : 0.0));
            return m;
        }).toList();
        result.put("suggestedKeywords", filteredKeywords);
        result.put("products", productList);
        return result;
    }

    @GetMapping ("/about-us")
        public String aboutUs(){
        return"about-us";
        }

}