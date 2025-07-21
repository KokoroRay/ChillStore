package com.esms.validation;

import jakarta.validation.ConstraintValidator;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;
    private String message;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
        this.message = constraintAnnotation.message();
    }
    @Override
    public boolean isValid(Object value, jakarta.validation.ConstraintValidatorContext context) {
        try {
            String password = (String) value.getClass().getMethod("get" + passwordField.substring(0, 1).toUpperCase() + passwordField.substring(1)).invoke(value);
            String confirmPassword = (String) value.getClass().getMethod("get" + confirmPasswordField.substring(0, 1).toUpperCase() + confirmPasswordField.substring(1)).invoke(value);

            return password != null && password.equals(confirmPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
