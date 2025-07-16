package com.esms.service;

import com.esms.model.entity.Customer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EditProfileTest {

    // Giả lập CustomerService, bạn hãy thay bằng bean thực tế nếu có
    private final CustomerServiceMock customerService = new CustomerServiceMock();

    @Test
    void TC001_VerifyUpdateProfileWithValidData() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("0988123456");
        String result = customerService.updateProfile(customer);
        assertEquals("Profile updated successfully.", result);
    }

    @Test
    void TC002_VerifyErrorWhenFullNameEmpty() {
        Customer customer = new Customer();
        customer.setName("");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("0988123456");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Please enter your full name.", ex.getMessage());
    }

    @Test
    void TC003_VerifyErrorWhenEmailEmpty() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("");
        customer.setPhone("0988123456");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Please enter your email.", ex.getMessage());
    }

    @Test
    void TC004_VerifyErrorForInvalidEmailFormat() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("abc@");
        customer.setPhone("0988123456");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Please enter a valid email.", ex.getMessage());
    }

    @Test
    void TC006_VerifyErrorWhenPhoneEmpty() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Please enter your phone number.", ex.getMessage());
    }

    @Test
    void TC007_VerifyErrorWhenPhoneInvalidChars() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("09781abc@#!");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Phone number must be 10 to 11 digits.", ex.getMessage());
    }

    @Test
    void TC008_VerifyErrorWhenPhoneTooShortOrLong() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("012345");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Phone number must be 10 to 11 digits.", ex.getMessage());
    }

    @Test
    void TC009_VerifyUpdateWithValidPhone() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyenvana@example.com");
        customer.setPhone("0988123456");
        String result = customerService.updateProfile(customer);
        assertEquals("Profile updated successfully.", result);
    }

    @Test
    void TC010_VerifyErrorWhenEmailContainsWhitespace() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("nguyen van a@example.com");
        customer.setPhone("0988123456");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Please enter a valid email.", ex.getMessage());
    }

    @Test
    void TC011_VerifyErrorWhenEmailExceedsLimit() {
        Customer customer = new Customer();
        customer.setName("Nguyen Van A");
        customer.setEmail("averyveryverylongemailaddress123456789012345678901234567890@example.com");
        customer.setPhone("0988123456");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(customer));
        assertEquals("Email exceeds allowed length.", ex.getMessage());
    }

    // Mock service cho mục đích unit test
    private static class CustomerServiceMock {
        public String updateProfile(Customer customer) {
            if (customer.getName() == null || customer.getName().trim().isEmpty())
                throw new IllegalArgumentException("Please enter your full name.");
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty())
                throw new IllegalArgumentException("Please enter your email.");
            if (!customer.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$"))
                throw new IllegalArgumentException("Please enter a valid email.");
            if (customer.getEmail().length() > 50)
                throw new IllegalArgumentException("Email exceeds allowed length.");
            if (customer.getEmail().contains(" "))
                throw new IllegalArgumentException("Please enter a valid email.");
            if (customer.getPhone() == null || customer.getPhone().trim().isEmpty())
                throw new IllegalArgumentException("Please enter your phone number.");
            if (!customer.getPhone().matches("^[0-9]{10,11}$"))
                throw new IllegalArgumentException("Phone number must be 10 to 11 digits.");
            return "Profile updated successfully.";
        }
    }
}