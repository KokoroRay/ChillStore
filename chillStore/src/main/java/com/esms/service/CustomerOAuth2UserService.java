package com.esms.service;

import com.esms.model.dto.CustomerOAuth2User;
import com.esms.model.entity.Customer;
import com.esms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Service xử lý đăng nhập OAuth2 (Facebook, GitHub, v.v.)
 * Tạo hoặc cập nhật thông tin Customer và gán role CUSTOMER
 */
@Service
public class CustomerOAuth2UserService extends DefaultOAuth2UserService {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public CustomerOAuth2UserService(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    /**
     * Xử lý đăng nhập OAuth2 từ các provider khác (không phải Google)
     * @param userRequest thông tin request từ OAuth2 provider
     * @return CustomerOAuth2User với role CUSTOMER
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Lấy thông tin user từ OAuth2 provider
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // Tạo hoặc cập nhật Customer trong database
        Customer customer = customerService.processOAuth2User(oAuth2User, registrationId);
        
        // Trả về CustomerOAuth2User với role CUSTOMER
        return new CustomerOAuth2User(customer, oAuth2User);
    }
}
