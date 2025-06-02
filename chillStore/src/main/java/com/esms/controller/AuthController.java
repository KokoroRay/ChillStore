package com.esms.controller;



import com.esms.exception.EmailAlreadyUsedException;
import com.esms.model.dto.RegisterDto;
import com.esms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final CustomerService customerService;

    @Autowired
    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }


    //form đăng kí đây nhá trả về register.html
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }


    //xử lý post khi cờ lai ờn sụp mít form register
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult, // <--- Cái này quan trọng!
            Model model) {
        if (bindingResult.hasErrors()) { // <--- Nếu có lỗi validation...
            return "register"; // <--- ...thì trả về trang đăng ký
        }
        try {
            customerService.register(registerDto);
            return "redirect:/auth/login?register=true";
        } catch (EmailAlreadyUsedException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

}
