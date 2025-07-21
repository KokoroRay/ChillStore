package com.esms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");
    
    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        // Không cần khởi tạo gì đặc biệt
    }
    
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // Nếu phoneNumber là null hoặc empty, không validate (để @NotBlank xử lý)
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true;
        }
        
        // Kiểm tra format: chỉ chứa số và có độ dài 10-11 ký tự
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
} 