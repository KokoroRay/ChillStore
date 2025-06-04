package com.esms.controller;



import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.InvalidOtpException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.ForgotPasswordDto;
import com.esms.model.dto.RegisterDto;
import com.esms.model.dto.OtpDto;
import com.esms.model.dto.ResetPasswordDto;
import com.esms.model.entity.Customer;
import com.esms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
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

    //hiểu thị form để forget
    @GetMapping("/forgot-password")
    public String showForgotForm(Model model) {
        // Sửa lỗi: Phải là ForgotPasswordDto, không phải RegisterDto
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgot(
            // Sửa lỗi: Tên model attribute phải khớp với tên trong Model
            @Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordDto dto,
            BindingResult bindingResult,
            Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        try {
            customerService.sendResetOtp(dto);
            // Sửa lỗi: Dùng addAttribute để email nằm trong URL
            redirectAttributes.addAttribute("email", dto.getEmail());
            return "redirect:/auth/verify-otp";
        } catch (UserNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    // --- Form xác thực OTP ---
    @GetMapping("/verify-otp")
    public String showOtpForm(@RequestParam(value = "email", required = false) String email, Model model) {
        OtpDto otpDto = new OtpDto();
        if (email != null) {
            otpDto.setEmail(email);
        } else {
            System.err.println("DEBUG: Email parameter is missing when displaying verify-otp form!");
        }
        model.addAttribute("otpDto", otpDto);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @Valid @ModelAttribute("otpDto") OtpDto otpDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("--- Debug OTP Verification (From Controller) ---");
        System.out.println("Received OTP DTO: Email=" + otpDto.getEmail() + ", OTP=" + otpDto.getOtp());

        if (bindingResult.hasErrors()) {
            System.out.println("Controller Debug: BindingResult has errors. Returning to verify-otp.");
            bindingResult.getAllErrors().forEach(error -> System.out.println("Validation Error: " + error.getDefaultMessage()));
            // Quan trọng: Thêm lại otpDto vào model khi có lỗi để dữ liệu (bao gồm email) không bị mất
            model.addAttribute("otpDto", otpDto);
            return "verify-otp";
        }

        try {
            System.out.println("Controller Debug: Calling customerService.verifyOtp()...");
            boolean isOtpValid = customerService.verifyOtp(otpDto);

            if (isOtpValid) {
                System.out.println("Controller Debug: OTP is VALID. Redirecting to reset-password.");
                redirectAttributes.addAttribute("email", otpDto.getEmail());
                return "redirect:/auth/reset-password";
            } else {
                System.out.println("Controller Debug: OTP is NOT valid (Service returned false). Displaying error.");
                model.addAttribute("error", "Mã OTP không hợp lệ.");
                model.addAttribute("otpDto", otpDto); // Giữ lại otpDto
                return "verify-otp";
            }

        } catch (UserNotFoundException e) {
            System.out.println("Controller Debug: Caught UserNotFoundException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("otpDto", otpDto); // Giữ lại otpDto
            return "verify-otp";
        } catch (InvalidOtpException e) {
            System.out.println("Controller Debug: Caught InvalidOtpException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("otpDto", otpDto); // Giữ lại otpDto
            return "verify-otp";
        } catch (Exception e) {
            System.err.println("Controller Debug: Caught UNEXPECTED Exception!");
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi không mong muốn trong quá trình xác thực: " + e.getMessage());
            model.addAttribute("otpDto", otpDto); // Giữ lại otpDto
            return "verify-otp";
        } finally {
            System.out.println("--- End Debug OTP Verification (From Controller) ---");
        }
    }

    // --- Gửi lại OTP ---
    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email, Model model) {
        try {
            ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto();
            forgotPasswordDto.setEmail(email);
            customerService.sendResetOtp(forgotPasswordDto);

            OtpDto otpDto = new OtpDto();
            otpDto.setEmail(email);
            model.addAttribute("otpDto", otpDto);
            model.addAttribute("message", "Mã OTP mới đã được gửi lại. Vui lòng kiểm tra email của bạn.");
            return "verify-otp";
        } catch (UserNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
            return "forgot-password";
        }
    }

    // --- Form đặt lại mật khẩu ---
    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("email") String email, // Only need email
                                Model model) {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setEmail(email);
        // Ensure the DTO is added to the model
        model.addAttribute("resetPasswordDto", resetPasswordDto);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processReset(@Valid @ModelAttribute("resetPasswordDto") ResetPasswordDto dto,
                               BindingResult bindingResult,
                               Model model) {
        System.out.println("--- Debug Reset Password (From Controller) ---");
        System.out.println("Received ResetPassword DTO: Email=" + dto.getEmail());
        System.out.println("New Password: " + (dto.getNewPassword() != null ? "Present" : "Null")); // Avoid logging actual password
        System.out.println("Confirm Password: " + (dto.getConfirmPassword() != null ? "Present" : "Null"));

        if (bindingResult.hasErrors()) {
            System.out.println("Controller Debug: BindingResult has errors. Returning to reset-password.");
            bindingResult.getAllErrors().forEach(error -> System.out.println("Validation Error: " + error.getDefaultMessage()));
            model.addAttribute("resetPasswordDto", dto); // Keep DTO in model to retain data
            return "reset-password";
        }
        try {
            System.out.println("Controller Debug: Calling customerService.resetPassword()...");
            customerService.resetPassword(dto);
            System.out.println("Controller Debug: Password reset successful. Redirecting to login.");
            return "redirect:/auth/login?resetSuccess=true";
        } catch (UserNotFoundException | IllegalArgumentException e) {
            System.out.println("Controller Debug: Caught Exception: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("resetPasswordDto", dto); // Keep DTO in model if error occurs
            return "reset-password";
        } finally {
            System.out.println("--- End Debug Reset Password (From Controller) ---");
        }
    }
}