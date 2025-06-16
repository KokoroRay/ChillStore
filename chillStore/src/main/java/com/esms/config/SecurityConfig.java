package com.esms.config;

import com.esms.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


//cấu hình security để tạo hash password theo dạng BCrypt
//đoạn này người viết node hông phải máy viết =))))
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép truy cập các trang công khai (Guest Home, CSS, JS, Auth flows)
                        .requestMatchers( "/admin/**","/", "/home", "/css/**", "/js/**",
                                "/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password",
                                "/auth/login", "/auth/register", "/auth/resend-otp").permitAll() // Thêm /auth/resend-otp vào đây
                        // Bất kỳ request nào khác đều yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/auth/login") // Trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/auth/login") // URL mà form đăng nhập POST đến
                        .defaultSuccessUrl("/home", true) // Khi đăng nhập thành công, chuyển hướng đến /home
                        .failureUrl("/auth/login?error=true") // Khi đăng nhập thất bại
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL để kích hoạt logout
                        .logoutSuccessUrl("/") // <-- CHỈNH SỬA: Đưa về trang chủ (guest view)
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên
                        .permitAll() // Cho phép tất cả truy cập URL logout
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}