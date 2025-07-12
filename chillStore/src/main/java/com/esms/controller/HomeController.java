package com.esms.controller;


import com.esms.model.dto.ProductDTO;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    // ----Product----
    @Autowired
    private ProductService productService;
    @GetMapping({"/", "/home"}) // Cả / và /home đều dẫn đến trang chủ

    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
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


        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage = productService.getProductDTOsPaginated(pageable);

        if (category != null && !category.equalsIgnoreCase("tất cả")) {
            productPage = productService.getProductsByCategory(category, pageable);
            model.addAttribute("currentCategory", category);
        } else {
            productPage = productService.getProductDTOsPaginated(pageable);
            model.addAttribute("currentCategory", "Tất cả");
        }
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("nextPage", page + 1);
        model.addAttribute("prevPage", page - 1);

        return "home";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "about-us";
    }
}