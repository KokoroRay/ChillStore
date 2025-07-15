package com.esms.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

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