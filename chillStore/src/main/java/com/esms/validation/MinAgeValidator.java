package com.esms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        // Nếu birthDate là null, không validate (cho phép optional)
        if (birthDate == null) {
            return true;
        }

        // Kiểm tra xem có phải là ngày trong quá khứ không
        LocalDate currentDate = LocalDate.now();
        if (birthDate.isAfter(currentDate)) {
            return false;
        }

        // Tính tuổi hiện tại
        Period period = Period.between(birthDate, currentDate);
        int age = period.getYears();

        // Kiểm tra tuổi tối thiểu
        return age >= minAge;
    }
} 