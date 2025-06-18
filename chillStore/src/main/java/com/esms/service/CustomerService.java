package com.esms.service;

import com.esms.model.dto.*;
import com.esms.model.entity.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.user.OAuth2User;

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

    Optional<Customer> findCustomerByEmail(String email);
    Optional<Customer> findCustomerByProviderAndProviderId(String provider, String providerId);
    Customer processOAuth2User(String provider, OAuth2User oAuth2User);
}
