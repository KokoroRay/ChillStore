package com.esms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE}) // Áp dụng cho Class và Annotation khác
@Retention(RetentionPolicy.RUNTIME) // Có sẵn trong runtime
@Constraint(validatedBy = PasswordMatchValidator.class) // Validator tương ứng
@Documented

public @interface PasswordMatch {
    String message() default "Passwords don't match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String passwordField() default "newPassword";
    String confirmPasswordField() default "confirmPassword";
}
