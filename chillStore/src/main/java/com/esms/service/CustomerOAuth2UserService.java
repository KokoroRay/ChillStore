package com.esms.service;

import com.esms.model.dto.CustomerOAuth2User;
import com.esms.model.entity.Customer;
import com.esms.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomerOAuth2UserService extends DefaultOAuth2UserService {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public CustomerOAuth2UserService(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Customer customer = customerService.processOAuth2User(oAuth2User, registrationId);
        
        // Create OAuth2User with CUSTOMER role
        return new CustomerOAuth2User(customer, oAuth2User);
    }
}
