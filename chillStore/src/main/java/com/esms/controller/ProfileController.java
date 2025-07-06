package com.esms.controller;

import com.esms.model.dto.CustomerDto;
import com.esms.model.entity.Customer;
import com.esms.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.esms.model.dto.ChangePasswordDto;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.Period;

@Controller
@RequestMapping("/customer/profile")
public class  ProfileController {
    private final CustomerService customerService;

    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Hiển thị trang profile cho customer hiện tại
    @GetMapping("")
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Customer customer = customerService.getCustomerByEmail(email);
        CustomerDto customerDto = convertToDto(customer);
        model.addAttribute("customerDto", customerDto);
        return "customer/profile";
    }

    // Hiển thị form chỉnh sửa profile cho customer hiện tại
    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Customer customer = customerService.getCustomerByEmail(email);
        CustomerDto customerDto = convertToDto(customer);
        
        // Debug logging
        System.out.println("DEBUG: Edit Form - Customer Birth Date: " + customer.getBirth_date());
        System.out.println("DEBUG: Edit Form - DTO Birth Date: " + customerDto.getBirthDate());
        
        model.addAttribute("customerDto", customerDto);
        return "customer/editProfile";
    }

    // Xử lý cập nhật profile cho customer hiện tại
    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("customerDto") CustomerDto customerDto, 
                               BindingResult bindingResult,
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        
        // Debug logging
        System.out.println("DEBUG: Birth Date from form: " + customerDto.getBirthDate());
        
        // Manual validation cho các fields
        if (customerDto.getName() == null || customerDto.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "error.name", "Name cannot be empty");
        }
        
        if (customerDto.getDisplayName() == null || customerDto.getDisplayName().trim().isEmpty()) {
            bindingResult.rejectValue("displayName", "error.displayName", "Display name cannot be empty");
        }
        
        if (customerDto.getEmail() == null || customerDto.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "error.email", "Email cannot be empty");
        } else if (!customerDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            bindingResult.rejectValue("email", "error.email", "Invalid email format");
        }
        
        if (customerDto.getPhone() != null && !customerDto.getPhone().trim().isEmpty()) {
            String phone = customerDto.getPhone().replaceAll("\\D", "");
            if (phone.length() < 10 || phone.length() > 11) {
                bindingResult.rejectValue("phone", "error.phone", "Phone number must be 10 or 11 digits");
            }
        }
        
        // Validation cho avatarUrl
        if (customerDto.getAvatarUrl() != null && !customerDto.getAvatarUrl().trim().isEmpty()) {
            if (!customerDto.getAvatarUrl().matches("^https?://.*")) {
                bindingResult.rejectValue("avatarUrl", "error.avatarUrl", "Avatar URL must be a valid HTTP/HTTPS URL");
            }
        }
        
        // Manual validation cho birthDate
        if (customerDto.getBirthDate() != null) {
            LocalDate currentDate = LocalDate.now();
            if (customerDto.getBirthDate().isAfter(currentDate)) {
                bindingResult.rejectValue("birthDate", "error.birthDate", "Date of birth must be in the past");
            } else {
                Period period = Period.between(customerDto.getBirthDate(), currentDate);
                int age = period.getYears();
                if (age < 18) {
                    bindingResult.rejectValue("birthDate", "error.birthDate", "User must be at least 18 years old");
                }
            }
        }
        
        if (bindingResult.hasErrors()) {
            // Log validation errors
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("DEBUG: Validation Error - " + error.getDefaultMessage());
            });
            // Nếu có lỗi validation, trả về form edit với lỗi
            model.addAttribute("customerDto", customerDto);
            return "customer/editProfile";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Customer customer = customerService.getCustomerByEmail(email);
        
        // Set các trường không được thay đổi
        customerDto.setCustomerId(customer.getCustomerId());
        customerDto.setEmail(customer.getEmail());
        customerDto.setPassword(customer.getPassword()); // Giữ nguyên password hiện tại
        
        System.out.println("DEBUG: Birth Date before update: " + customerDto.getBirthDate());
        
        customerService.updateCustomer(customer.getCustomerId(), customerDto);
        
        // Verify update
        Customer updatedCustomer = customerService.getCustomerByEmail(email);
        System.out.println("DEBUG: Birth Date after update: " + updatedCustomer.getBirth_date());
        
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/customer/profile";
    }

    // Hiển thị form đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "customer/changePassword";
    }

    // Xử lý đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("changePasswordDto") @Valid ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/changePassword";
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            customerService.changePassword(email, changePasswordDto);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            return "redirect:/customer/profile";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "customer/changePassword";
        }
    }

    // Hàm convert giống CustomerController
    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setName(customer.getName());
        dto.setDisplayName(customer.getDisplay_name());
        dto.setEmail(customer.getEmail());
        dto.setPassword(customer.getPassword());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setBirthDate(customer.getBirth_date());
        dto.setCreatedAt(customer.getCreated_at());
        dto.setUpdatedAt(customer.getUpdated_at());
        dto.setLocked(customer.isLocked());
        dto.setAvatarUrl(customer.getAvatar_url());
        dto.setGender(customer.getGender());
        return dto;
    }
} 