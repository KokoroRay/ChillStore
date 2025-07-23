package com.esms.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordTest {

    private final CustomerServiceMock customerService = new CustomerServiceMock();

    @Test
    void TC01_VerifyChangePasswordWithValidCurrentAndNewPassword() {
        String result = customerService.changePassword("OldPass123", "NewPass456", "NewPass456");
        assertEquals("Password changed successfully.", result);
    }

    @Test
    void TC02_VerifyErrorWhenCurrentPasswordIncorrect() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                customerService.changePassword("WrongOldPass", "NewPass456", "NewPass456"));
        assertEquals("Current password is incorrect.", ex.getMessage());
    }

    @Test
    void TC03_VerifyErrorWhenNewPasswordTooShort() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                customerService.changePassword("OldPass123", "123", "123"));
        assertEquals("Password must be at least 6 characters.", ex.getMessage());
    }

    @Test
    void TC04_VerifyErrorWhenConfirmPasswordMismatch() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                customerService.changePassword("OldPass123", "NewPass456", "Mismatch456"));
        assertEquals("Confirm password does not match.", ex.getMessage());
    }

    // Mock service cho mục đích unit test
    private static class CustomerServiceMock {
        public String changePassword(String currentPassword, String newPassword, String confirmPassword) {
            if (!"OldPass123".equals(currentPassword))
                throw new IllegalArgumentException("Current password is incorrect.");
            if (newPassword == null || newPassword.length() < 6)
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            if (!newPassword.equals(confirmPassword))
                throw new IllegalArgumentException("Confirm password does not match.");
            return "Password changed successfully.";
        }
    }
}