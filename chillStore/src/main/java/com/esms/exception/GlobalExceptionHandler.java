package com.esms.exception;

import com.esms.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
class GlobalControllerAdvice {

  @Autowired
  private CartService cartService;

  @ModelAttribute("cartCount")
  public Integer populateCartCount(HttpSession session) {
    Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
    if (customerId != null) {
      return cartService.getCartItems(customerId).stream().mapToInt(item -> item.getQuantity()).sum();
    }
    return 0;
  }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "forgot-password";  // quay lại form nhập email
    }

    @ExceptionHandler(InvalidOtpException.class)
    public String handleInvalidOtp(InvalidOtpException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "verify-otp";  // quay lại form nhập OTP
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArg(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "reset-password";  // quay lại form reset password
    }

}