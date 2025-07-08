package com.esms.service;

import com.esms.model.dto.CustomerOAuth2User;
import com.esms.model.entity.Customer;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Service xử lý đăng nhập Google OIDC (OpenID Connect)
 * Google sử dụng OIDC thay vì OAuth2 thuần túy
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final CustomerService customerService;

    public CustomOidcUserService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Xử lý đăng nhập Google OIDC
     * @param userRequest thông tin request từ Google
     * @return OidcUser wrapper với role CUSTOMER
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // Lấy thông tin user từ Google OIDC
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // Chuyển đổi OIDC user thành OAuth2User để xử lý thống nhất
        OAuth2User oAuth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            java.util.Collections.emptyList(),
            java.util.Map.of(
                "email", oidcUser.getEmail(),
                "name", oidcUser.getFullName()
            ),
            "email"
        );

        // Tạo hoặc cập nhật Customer trong database
        Customer customer = customerService.processOAuth2User(oAuth2User, registrationId);
        // Tạo CustomerOAuth2User với role CUSTOMER
        CustomerOAuth2User customerOAuth2User = new CustomerOAuth2User(customer, oAuth2User);
        
        // Tạo wrapper OidcUser để Spring Security nhận diện đúng OIDC flow
        // Nhưng vẫn giữ được role CUSTOMER từ CustomerOAuth2User
        return new OidcUser() {
            // Giữ nguyên thông tin OIDC từ Google
            @Override
            public java.util.Map<String, Object> getClaims() {
                return oidcUser.getClaims();
            }
            
            @Override
            public org.springframework.security.oauth2.core.oidc.OidcUserInfo getUserInfo() {
                return oidcUser.getUserInfo();
            }
            
            @Override
            public org.springframework.security.oauth2.core.oidc.OidcIdToken getIdToken() {
                return oidcUser.getIdToken();
            }
            
            // Sử dụng thông tin từ CustomerOAuth2User
            @Override
            public java.util.Map<String, Object> getAttributes() {
                return customerOAuth2User.getAttributes();
            }
            
            // Quan trọng: Trả về role CUSTOMER
            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return customerOAuth2User.getAuthorities();
            }
            
            @Override
            public String getName() {
                return customerOAuth2User.getName();
            }
        };
    }
}