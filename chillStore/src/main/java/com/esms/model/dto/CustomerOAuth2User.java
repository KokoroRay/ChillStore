package com.esms.model.dto;

import com.esms.model.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomerOAuth2User implements OAuth2User {

    private Customer customer;
    private OAuth2User oauth2User;

    public CustomerOAuth2User(Customer customer, OAuth2User oauth2User) {
        this.customer = customer;
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }

    public Customer getCustomer() {
        return customer;
    }

    public Integer getCustomerId() {
        return customer.getCustomerId();
    }

    public String getCustomerName() {
        return customer.getName();
    }

    public String getEmail() {
        return customer.getEmail();
    }

    public String getDisplayName() {
        return customer.getDisplay_name();
    }

    public String getProvider() {
        return customer.getProvider();
    }
}
