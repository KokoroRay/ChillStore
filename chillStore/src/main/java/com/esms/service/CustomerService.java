package com.esms.service;

import com.esms.model.dto.*;
import com.esms.model.entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Optional;

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

    Page<Customer> findWithFilters(String keyword, Boolean locked, Pageable pageable);

    List<String> suggestCustomer(String keyword, int limit);

    List<String> suggestCustomerByType(String keyword, String type, int limit);

    Page<Customer> searchCustomersByName(String name, Pageable pageable);
    Page<Customer> searchCustomersByEmail(String email, Pageable pageable);


    //xử lý liên quan đến đăng kí tài khoản bằng gg
    Optional<Customer> findCustomerByEmail(String email);
    Optional<Customer> findCustomerByProviderAndProviderId(String provider, String providerId);
    Customer processOAuth2User (OAuth2User oAuth2User, String provider);

    Customer getCustomerByEmail(String email);

    void changePassword(String email, ChangePasswordDto dto);
}