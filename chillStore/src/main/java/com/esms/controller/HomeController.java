package com.esms.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"}) // Cả / và /home đều dẫn đến trang chủ
    public String home(Model model) {
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
        return "home"; // Trả về tên file template home.html
    }
}