package com.esms.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/home",
                                "/css/**", "/js/**", "/videos/**", "/img/**", "/images/**",
                                "/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password",
                                "/auth/login", "/auth/register", "/auth/resend-otp"
                        ).permitAll()

                        // Cho phép truy cập các trang công khai (Guest Home, CSS, JS, Auth flows)
                        .requestMatchers("/", "/home", "/videos/**", "/css/**", "/js/**", "/img/**", // Thêm /img/** nếu bạn có ảnh
                                "/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password",
                                "/auth/login", "/auth/register", "/auth/resend-otp").permitAll()
                        // Phân quyền công dân cao nhất
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN") //role cao nhất
                        // giai cấp bị bóc lộ
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN") //có thể qua lại giữ 2 giai cấp
                        //nguồn tiền duy trì hệ thống
                        .requestMatchers("/customer/**", "/profile", "/cart", "/checkout")
                        .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/auth/login") // Trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/auth/login") // URL mà form đăng nhập POST đến
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/auth/login?error=true") // Khi đăng nhập thất bại
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/auth/login")
                        .successHandler(authenticationSuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customerOAuth2UserService))
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL để kích hoạt logout
                        .logoutSuccessUrl("/") // <-- CHỈNH SỬA: Đưa về trang chủ (guest view)
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên
                        .permitAll() // Cho phép tất cả truy cập URL logout
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/staff/orders/*/update-status", "/staff/orders/*/confirm-refund")
                );
        return http.build();
    }

    //đã chuyển passwordEncoder qua AppConfig để trách phụ thuộc


    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                // Chuyển hướng về trang mặt định
                String redirectUrl = "/";
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    redirectUrl = "/admin/category";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                    redirectUrl = "/staff/category";
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                    redirectUrl = "/home";
                }
                response.sendRedirect(redirectUrl);
            }

        };
    }

}