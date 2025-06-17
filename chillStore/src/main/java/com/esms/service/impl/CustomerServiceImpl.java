package com.esms.service.impl;

import com.esms.config.CacheConfig;
import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.InvalidOtpException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.*;
import com.esms.model.entity.Customer;
import com.esms.repository.CustomerRepository;
import com.esms.service.CustomerService;
import com.esms.util.MapperUtils.CustomerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final CacheManager cacheManager;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper,
                               PasswordEncoder passwordEncoder, JavaMailSender mailSender,
                               CacheManager cacheManager) {
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
            throw new EmailAlreadyUsedException("Email " + dto.getEmail() + " đã tồn tại chọn cái khác đeeeee");
        });
        Customer entity = customerMapper.toEntity(dto);

        String rawPassword = dto.getPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        entity.setPassword(hashedPassword);

        customerRepository.save(entity);
    }

    @Override
    public void sendResetOtp(ForgotPasswordDto dto) {
        Optional<Customer> userOpt = customerRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Không tìm thấy tài khoản với email này.");
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache != null) {
            otpCache.put(dto.getEmail(), new OtpData(otp, expirationTime));
            System.out.println("DEBUG: OTP '" + otp + "' and expiration '" + expirationTime + "' saved to cache for email: " + dto.getEmail());
        } else {
            System.err.println("Error: OTP cache not found!");
        }

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

        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache == null) {
            System.err.println("CRITICAL ERROR: OTP cache not initialized. Check CacheConfig.");
            throw new IllegalStateException("OTP cache not initialized.");
        }

        OtpData storedOtpData = otpCache.get(dto.getEmail(), OtpData.class);

        System.out.println("--- Debug OTP Verification (From Service) ---");
        System.out.println("Email provided by user: " + dto.getEmail());
        System.out.println("OTP provided by user: '" + dto.getOtp() + "'");

        if (storedOtpData == null) {
            System.out.println("Result: OTP Data NOT found in cache for this email. (Could be expired or never sent)");
            System.out.println("---------------------------------------------");
            throw new InvalidOtpException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        System.out.println("OTP from cache: '" + storedOtpData.getOtp() + "'");
        System.out.println("OTP Expiration from cache: " + storedOtpData.getExpirationTime());
        System.out.println("Current time (LocalDateTime.now()): " + LocalDateTime.now());
        System.out.println("Is OTP provided equal to OTP from cache? " + storedOtpData.getOtp().equals(dto.getOtp()));
        System.out.println("Is current time before expiration? " + LocalDateTime.now().isBefore(storedOtpData.getExpirationTime()));
        System.out.println("---------------------------------------------");

        if (storedOtpData.getOtp().equals(dto.getOtp())) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(storedOtpData.getExpirationTime())) {
                otpCache.evict(dto.getEmail());
                System.out.println("DEBUG: OTP verified successfully and removed from cache for email: " + dto.getEmail());
                return true;
            } else {
                otpCache.evict(dto.getEmail());
                System.out.println("DEBUG: OTP expired for email: " + dto.getEmail() + ". Removed from cache.");
                throw new InvalidOtpException("Mã OTP đã hết hạn.");
            }
        } else {
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

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu không khớp.");
        }

        customer.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        customerRepository.save(customer);
        System.out.println("DEBUG: Password reset successfully for email: " + dto.getEmail());
    }

    @Override
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Page<Customer> searchCustomers(String search, Pageable pageable) {
        return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
    }

    @Transactional
    @Override
    public void createCustomer(Customer customer) {
        customerRepository.findByEmail(customer.getEmail()).ifPresent(c -> {
            throw new EmailAlreadyUsedException("Email " + customer.getEmail() + " đã tồn tại.");
        });

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }

        if (customer.getCreated_at() == null) {
            customer.setCreated_at(LocalDateTime.now());
        }
        if (customer.getUpdated_at() == null) {
            customer.setUpdated_at(LocalDateTime.now());
        }

        customerRepository.save(customer);
    }
    @Override
    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    public void updateCustomer(Integer id, CustomerDto customerDto) {
        Customer existingCustomer = getCustomerById(id);

        // Kiểm tra email trùng (nếu email thay đổi)
        if (!existingCustomer.getEmail().equals(customerDto.getEmail())) {
            customerRepository.findByEmail(customerDto.getEmail()).ifPresent(c -> {
                throw new EmailAlreadyUsedException("Email đã được sử dụng");
            });
        }

        // Cập nhật các trường
        existingCustomer.setName(customerDto.getName());
        existingCustomer.setDisplay_name(customerDto.getDisplayName());
        existingCustomer.setEmail(customerDto.getEmail());
        if (customerDto.getPassword() != null && !customerDto.getPassword().isEmpty()) {
            existingCustomer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        }
        existingCustomer.setPhone(customerDto.getPhone());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setBirth_date(customerDto.getBirthDate());
        existingCustomer.setUpdated_at(LocalDateTime.now());

        customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Integer id) {
        Customer customer = getCustomerById(id);
        customer.setLocked(true);
        customerRepository.save(customer);
    }
}