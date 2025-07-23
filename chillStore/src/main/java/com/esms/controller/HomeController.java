package com.esms.controller;


import com.esms.model.dto.ProductDTO;
import com.esms.service.DiscountService;
import com.esms.service.ProductService;
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

@Controller
public class HomeController {

    // ----Product----
    @Autowired
    private ProductService productService;
    // Thêm dòng này để lấy sản phẩm discount
    @Autowired
    private DiscountService discountService;
    @GetMapping({"/", "/home"}) // Cả / và /home đều dẫn đến trang chủ

    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       @RequestParam(value = "category", required = false) String category) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu người dùng đã xác thực (không phải "anonymousUser")
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            // Người dùng đã đăng nhập
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", authentication.getName()); // Lấy username của người dùng đã đăng nhập
            // Bạn có thể thêm các thuộc tính khác cho view của khách hàng
        } else {
            // Người dùng chưa đăng nhập (khách)
            model.addAttribute("isLoggedIn", false);
            // Bạn có thể thêm các thuộc tính khác cho view của khách
        }

        // Random toàn bộ sản phẩm rồi chia trang
        var products = productService.getRandomProductsPaged(page, size);
        int totalPages = productService.getRandomProductsTotalPages(size);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("nextPage", Math.min(page + 1, totalPages - 1));
        model.addAttribute("prevPage", Math.max(page - 1, 0));
        // Lấy tối đa 10 sản phẩm giảm giá nổi bật (discount)
        var discountPage = productService.getDiscountProducts(org.springframework.data.domain.PageRequest.of(0, 10));
        var highlightDiscountProducts = discountPage.getContent();
        model.addAttribute("highlightDiscountProducts", highlightDiscountProducts);
        // Chuẩn bị map productId -> discount cho các sản phẩm giảm giá nổi bật
        java.util.Map<Integer, com.esms.model.entity.Discount> productDiscountMap = new java.util.HashMap<>();
        for (var product : highlightDiscountProducts) {
            var discount = productService.getActiveDiscountForProduct(product);
            if (discount != null) {
                productDiscountMap.put(product.getProductId(), discount);
            }
        }
        model.addAttribute("productDiscountMap", productDiscountMap);
        return "home";
    }

    @GetMapping ("/about-us")
        public String aboutUs(){
        return"about-us";
        }

}