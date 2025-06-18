package com.esms.service;

import com.esms.model.dto.*;
import com.esms.model.entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    void register(RegisterDto dto);

    void sendResetOtp(ForgotPasswordDto dto);
    boolean verifyOtp(OtpDto dto);
    void resetPassword(ResetPasswordDto dto);
    // Quản lý người dùng
    Page<Customer> getAllCustomers(Pageable pageable);
    Page<Customer> searchCustomers(String search, Pageable pageable);
    Customer getCustomerById(Integer id);
    void updateCustomer(Integer id, CustomerDto customerDto);
    void deleteCustomer(Integer id);

    @Transactional
    void createCustomer(Customer customer);

    Page<Customer> searchCustomersWithFilters(String keyword, String gender, Boolean locked, Pageable pageable);

    List<String> suggestCustomer(String keyword, int limit);

    List<String> suggestCustomerByType(String keyword, String type, int limit);

    Page<Customer> searchCustomersByName(String name, Pageable pageable);
    Page<Customer> searchCustomersByEmail(String email, Pageable pageable);
}
