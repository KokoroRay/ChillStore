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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // TẠM THỜI tắt CSRF để test
                .csrf(csrf -> csrf.disable()) // Vô hiệu hóa bảo vệ CSRF

                // Cho phép HTTP Basic Authentication (nếu cần)
                // Điều này hữu ích cho việc kiểm tra bằng Postman hoặc các client API
                .httpBasic(Customizer.withDefaults()) // Sử dụng Customizer để cấu hình mặc định


                // 1. Cấu hình ủy quyền HTTP
                .authorizeHttpRequests(authorize -> authorize // Sử dụng lambda expression để cấu hình
                        // Cho phép truy cập tĩnh và trang auth (login, register) mà không cần authenticate
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/auth/**", "/").permitAll() // Thêm "/" (trang home) vào permitAll
                        // Ví dụ: Cho phép truy cập "/home" nếu bạn muốn nó công khai ngay cả khi chưa đăng nhập
                        .requestMatchers("/home").permitAll() // Đảm bảo trang home được truy cập tự do
                        .anyRequest().authenticated() // Tất cả các request khác yêu cầu xác thực
                )
                // 2. Cấu hình Form Login
                .formLogin(form -> form // Sử dụng lambda expression để cấu hình formLogin
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")  // URL Spring Security tự xử lý POST
                        .defaultSuccessUrl("/home", true) // Chuyển hướng đến /home sau khi đăng nhập thành công
                        .failureUrl("/auth/login?error=true") // Chuyển hướng đến /auth/login?error=true nếu đăng nhập thất bại
                        .permitAll() // Cho phép tất cả mọi người truy cập trang login
                )
                // 3. Cấu hình Logout
                .logout(logout -> logout // Sử dụng lambda expression để cấu hình logout
                        .logoutUrl("/auth/logout") // URL xử lý đăng xuất (nên dùng POST)
                        .logoutSuccessUrl("/auth/login?logout=true") // Chuyển hướng sau khi đăng xuất thành công
                        .permitAll() // Cho phép tất cả mọi người truy cập URL logout
                )
                .httpBasic(httpBasicConfigurer -> httpBasicConfigurer.disable());
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
