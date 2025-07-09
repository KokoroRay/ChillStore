package com.esms.model.dto;

import com.esms.model.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Wrapper class kết hợp thông tin Customer và OAuth2User
 * Tự động gán role CUSTOMER cho user đăng nhập bằng Google/OAuth2
 */
public class CustomerOAuth2User implements OAuth2User {

    private Customer customer;      // Thông tin customer trong database
    private OAuth2User oauth2User;  // Thông tin từ OAuth2 provider

    public CustomerOAuth2User(Customer customer, OAuth2User oauth2User) {
        this.customer = customer;
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    /**
     * Phương thức quan trọng nhất: Gán role CUSTOMER cho user
     * Spring Security sử dụng để kiểm tra quyền truy cập
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }

    // Các phương thức tiện ích để lấy thông tin customer
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
