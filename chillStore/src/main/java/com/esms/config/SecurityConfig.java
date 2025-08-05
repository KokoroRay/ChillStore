package com.esms.config;

import com.esms.service.CustomOidcUserService;
import com.esms.service.CustomUserDetailsService;
import com.esms.service.CustomerOAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;

import java.io.IOException;
import java.util.Collection;


//cấu hình security để tạo hash password theo dạng BCrypt
//đoạn này người viết node hông phải máy viết =))))
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private CustomerOAuth2UserService customerOAuth2UserService;

    @Autowired
    private CustomOidcUserService customOidcUserService;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/home",
                                "/Product", "/Product/**", "/DiscountProducts",
                                "/api/product/*/feedbacks", "/api/feedback/*/reply",
                                "/product/view/**", "/customer/product/view/**","/searchProduct",
                                "/css/**", "/js/**", "/videos/**", "/img/**", "/images/**",
                                "/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password",
                                "/auth/login", "/auth/register", "/auth/resend-otp"
                        ).permitAll()
                        // Wishlist API endpoints - allow CUSTOMER access
                        .requestMatchers("/api/wishlist/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                        // Phân quyền công dân cao nhất
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN") //role cao nhất
                        // giai cấp bị bóc lộ
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN") //có thể qua lại giữ 2 giai cấp
                        // Cho phép STAFF, ADMIN, CUSTOMER truy cập API feedback reply
                        .requestMatchers("/api/feedback/*/reply").hasAnyRole("ADMIN", "STAFF", "CUSTOMER")
                        //nguồn tiền duy trì hệ thống
                        .requestMatchers("/customer/**", "/profile", "/cart","/order","/order-history", "/checkout")
                        .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/auth/login") // Trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/auth/login") // URL mà form đăng nhập POST đến
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            String errorUrl = "/auth/login?error=true";
                            if (exception instanceof LockedException) {
                                errorUrl = "/auth/login?locked=true";
                            }
                            if (exception instanceof UsernameNotFoundException) {
                                errorUrl = "/auth/login?error=true";
                            }
                            response.sendRedirect(errorUrl);
                        })
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                // Cấu hình đăng nhập OAuth2/Google
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/auth/login")  // Trang đăng nhập tùy chỉnh
                        .successHandler(authenticationSuccessHandler())  // Xử lý sau khi đăng nhập thành công
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customerOAuth2UserService)      // Xử lý OAuth2 thông thường
                                .oidcUserService(customOidcUserService))     // Xử lý Google OIDC
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL để kích hoạt logout
                        .logoutSuccessUrl("/") // <-- CHỈNH SỬA: Đưa về trang chủ (guest view)
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên
                        .permitAll() // Cho phép tất cả truy cập URL logout
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/staff/orders/*/update-status",
                                "/staff/orders/*/confirm-refund",
                                "/api/feedback/*/reply",
                                "/api/wishlist/**",
                                "/customer/api/maintenance",
                                "/admin/maintenance/**",
                                "/staff/maintenance/**"
                        )
                );
        return http.build();
    }

    //đã chuyển passwordEncoder qua AppConfig để trách phụ thuộc




    /**
     * Xử lý chuyển hướng sau khi đăng nhập thành công
     * Dựa vào role để chuyển đến trang phù hợp
     */
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                // Lấy danh sách quyền của user
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                String redirectUrl = "/";

                // Chuyển hướng dựa trên role
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    redirectUrl = "/admin/category";   // Admin dashboard
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                    redirectUrl = "/staff/category";   // Staff dashboard
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                    redirectUrl = "/home";             // Customer home page
                }


                // Lưu thông tin người dùng vào session
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                    // Dùng username để lấy thông tin customer
                    String email = userDetails.getUsername();

                    // Gọi CustomUserDetailsService để lấy thông tin chi tiết từ DB
                    com.esms.model.entity.Customer customer = userDetailsService.getCustomerByEmail(email);

                    if (customer != null) {
                        request.getSession().setAttribute("loggedInCustomerId", customer.getCustomerId());
                        request.getSession().setAttribute("loggedInUserEmail", customer.getEmail());
                        request.getSession().setAttribute("loggedInUserName", customer.getName());
                    }
                } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
                    // Đăng nhập Google
                    String email = oauthUser.getAttribute("email");

                    com.esms.model.entity.Customer customer = userDetailsService.getCustomerByEmail(email);
                    if (customer != null) {
                        request.getSession().setAttribute("loggedInCustomerId", customer.getCustomerId());
                        request.getSession().setAttribute("loggedInUserEmail", customer.getEmail());
                        request.getSession().setAttribute("loggedInUserName", customer.getName());
                    }
                }

                response.sendRedirect(redirectUrl);
            }
        };
    }

}