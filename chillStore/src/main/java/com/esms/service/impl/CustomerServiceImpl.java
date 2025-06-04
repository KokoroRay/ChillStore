package com.esms.service.impl;


import com.esms.config.CacheConfig;
import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.InvalidOtpException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.ForgotPasswordDto;
import com.esms.model.dto.RegisterDto;
import com.esms.model.dto.ResetPasswordDto;
import com.esms.model.dto.OtpDto;
import com.esms.model.entity.Customer;
import com.esms.repository.CustomerRepository;
import com.esms.service.CustomerService;
import com.esms.util.MapperUtils.CustomerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final CacheManager cacheManager;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper, PasswordEncoder passwordEncoder, JavaMailSender mailSender, CacheManager cacheManager) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.cacheManager = cacheManager;
    }

    public static class OtpData {
        private String otp;
        private LocalDateTime expirationTime;

        public OtpData(String otp, LocalDateTime expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }

        // Cần có constructor không đối số và getters cho Caffeine
        public OtpData() {}

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpirationTime() {
            return expirationTime;
        }
    }

    @Override
    @Transactional
    public void register(RegisterDto dto) {
        customerRepository.findByEmail(dto.getEmail()).ifPresent(customer -> {
            throw new EmailAlreadyUsedException("Email " + dto.getEmail() + "đã tồn tại chọn cái khác đeeeee");
        });
//    if (dto.getPassword().equals(passwordEncoder.encode(dto.getPassword()))) {
//        throw new IllegalArgumentException("mật khẩu méo chinh xác!");
//    }
        Customer entity = customerMapper.toEntity(dto);

        String rawPassword = dto.getPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        entity.setPassword(hashedPassword);

        customerRepository.save(entity);
    }

    @Override
    public void sendResetOtp(ForgotPasswordDto dto) {
        // Vẫn cần kiểm tra xem email có tồn tại không
        Optional<Customer> userOpt = customerRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Không tìm thấy tài khoản với email này.");
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5); // OTP có hiệu lực 5 phút

        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache != null) {
            otpCache.put(dto.getEmail(), new OtpData(otp, expirationTime)); // Lưu OTPData vào cache
            System.out.println("DEBUG: OTP '" + otp + "' and expiration '" + expirationTime + "' saved to cache for email: " + dto.getEmail());
        } else {
            System.err.println("Error: OTP cache not found!"); // Log lỗi nếu cache không tồn tại
        }

        // Gửi mã OTP qua email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getEmail());
        message.setSubject("Chill Store xin gửi mã otp");
        message.setText("Mã OTP của bạn là: " + otp + "\n" +
                "Nhớ nhập nha mã có 5p thôi nha quý khách ơi!");
        mailSender.send(message);
    }


    @Override
    public boolean verifyOtp(OtpDto dto) {
        Optional<Customer> userOpt = customerRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Không tìm thấy tài khoản với email này.");
        }

        // KHÔNG cần dòng này nếu bạn đã loại bỏ setResetOtp/setOtpExpiration khỏi Customer entity
        // Customer customer = userOpt.get();

        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache == null) {
            System.err.println("CRITICAL ERROR: OTP cache not initialized. Check CacheConfig.");
            throw new IllegalStateException("OTP cache not initialized.");
        }

        OtpData storedOtpData = otpCache.get(dto.getEmail(), OtpData.class);

        // --- Debug logs chi tiết hơn ---
        System.out.println("--- Debug OTP Verification (From Service) ---");
        System.out.println("Email provided by user: " + dto.getEmail());
        System.out.println("OTP provided by user: '" + dto.getOtp() + "'");

        if (storedOtpData == null) {
            System.out.println("Result: OTP Data NOT found in cache for this email. (Could be expired or never sent)");
            System.out.println("---------------------------------------------");
            throw new InvalidOtpException("Mã OTP không hợp lệ hoặc đã hết hạn."); // Ném lỗi nếu không tìm thấy OTP trong cache
        }

        System.out.println("OTP from cache: '" + storedOtpData.getOtp() + "'");
        System.out.println("OTP Expiration from cache: " + storedOtpData.getExpirationTime());
        System.out.println("Current time (LocalDateTime.now()): " + LocalDateTime.now());
        System.out.println("Is OTP provided equal to OTP from cache? " + storedOtpData.getOtp().equals(dto.getOtp()));
        System.out.println("Is current time before expiration? " + LocalDateTime.now().isBefore(storedOtpData.getExpirationTime()));
        System.out.println("---------------------------------------------");

        // Kiểm tra OTP và thời gian hết hạn
        if (storedOtpData.getOtp().equals(dto.getOtp())) { // So sánh OTP
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(storedOtpData.getExpirationTime())) { // Kiểm tra thời gian hết hạn
                otpCache.evict(dto.getEmail()); // Xóa OTP khỏi cache sau khi xác thực thành công
                System.out.println("DEBUG: OTP verified successfully and removed from cache for email: " + dto.getEmail());
                return true;
            } else {
                // OTP đã hết hạn
                otpCache.evict(dto.getEmail()); // Xóa OTP hết hạn
                System.out.println("DEBUG: OTP expired for email: " + dto.getEmail() + ". Removed from cache.");
                throw new InvalidOtpException("Mã OTP đã hết hạn.");
            }
        } else {
            // OTP không khớp
            System.out.println("DEBUG: OTP mismatch for email: " + dto.getEmail());
            throw new InvalidOtpException("Mã OTP không hợp lệ.");
        }
    }

    @Override
    public void resetPassword(ResetPasswordDto dto) {
        Optional<Customer> userOpt = customerRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Không tìm thấy tài khoản với email này.");
        }

        Customer customer = userOpt.get();

        // Check if new password and confirm password match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu không khớp.");
        }

        // REMOVE THESE OTP-RELATED CHECKS
        // if(customer.getPassword() == null
        //         || !customer.getResetOtp().equals(dto.getOtp()) // This line is problematic as OTP is not on Customer entity and is cleared from cache
        //         || customer.getOtpExpiration().isBefore(LocalDateTime.now())){ // This line is also problematic
        //     throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        // }

        // Encode and save the new password
        customer.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        // REMOVE THESE LINES, as OTP is handled by cache and not stored on Customer entity
        // customer.setResetOtp(null);
        // customer.setOtpExpiration(null);
        customerRepository.save(customer); // Save changes to the database
        System.out.println("DEBUG: Password reset successfully for email: " + dto.getEmail());
    }
}
