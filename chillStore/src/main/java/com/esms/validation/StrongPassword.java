package com.esms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;


//password phải strong rùi nhiêu đó cái pattern mà =))))
@NotEmpty(message = "Mật khẩu không được để trống.")
@Size(min = 6, max = 16, message = "Mật khẩu có độ dài tối thiểu là 6 và tối đa là 16.")
@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Mật khẩu phải chứa ít nhất 1 số, 1 chữ thường, 1 chữ hoa, 1 ký tự đặc biệt và không có khoảng trắng.")
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {}) // Không cần validator riêng vì nó là meta-annotation
public @interface StrongPassword {
    String message() default "Mật khẩu không đáp ứng yêu cầu độ mạnh.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}