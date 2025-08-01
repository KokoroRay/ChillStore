
package com.esms.service.impl;

import com.esms.config.CacheConfig;
import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.InvalidOtpException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.*;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Order;
import com.esms.model.entity.Voucher;
import com.esms.model.entity.Wishlist;
import com.esms.repository.CustomerRepository;
import com.esms.repository.OrderRepository;
import com.esms.repository.VoucherRepository;
import com.esms.repository.WishlistRepository;
import com.esms.repository.ProductRepository;
import com.esms.model.entity.Product;
import com.esms.service.CustomerService;
import com.esms.util.MapperUtils.CustomerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final CacheManager cacheManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

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
    public void register(RegisterDTO dto) {
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
    public void sendResetOtp(ForgotPasswordDTO dto) {
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
    public boolean verifyOtp(OtpDTO dto) {
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
    public void resetPassword(ResetPasswordDTO dto) {
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

    @Override
    public Page<Customer> searchCustomersWithFilters(String keyword, Boolean locked, Pageable pageable) {
        return customerRepository.searchCustomersWithFilters(
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                locked,
                pageable
        );
    }

    @Override
    public Page<Customer> findWithFilters(String keyword, Boolean locked, Pageable pageable) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return customerRepository.findWithFilters(effectiveKeyword, locked, pageable);
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
    public void updateCustomer(Integer id, CustomerDTO customerDto) {
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
        existingCustomer.setGender(customerDto.getGender());
        existingCustomer.setAvatar_url(customerDto.getAvatarUrl());
        existingCustomer.setUpdated_at(LocalDateTime.now());

        customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Integer id) {
        Customer customer = getCustomerById(id);
        customer.setLocked(true);
        customerRepository.save(customer);
    }

    @Override
    public List<String> suggestCustomer(String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Customer> page = customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        return page.getContent().stream()
                .map(c -> c.getDisplay_name() + " (" + c.getEmail() + ")")
                .collect(Collectors.toList());
    }

    @Override
    public List<String> suggestCustomerByType(String keyword, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Customer> page;
        if ("email".equalsIgnoreCase(type)) {
            page = customerRepository.findByEmailContainingIgnoreCase(keyword, pageable);
            return page.getContent().stream().map(Customer::getEmail).collect(Collectors.toList());
        } else {
            page = customerRepository.findByNameContainingIgnoreCase(keyword, pageable);
            return page.getContent().stream().map(Customer::getDisplay_name).collect(Collectors.toList());
        }
    }

    @Override
    public Page<Customer> searchCustomersByName(String name, Pageable pageable) {
        return customerRepository.searchByDisplayName(name, pageable);
    }

    @Override
    public Page<Customer> searchCustomersByEmail(String email, Pageable pageable) {
        return customerRepository.findByEmailContainingIgnoreCase(email, pageable);
    }

    //add voucher cho customer
    @Override
    public void addVoucherToCustomer(Integer customerId, Integer voucherId) {
        Customer customer = getCustomerById(customerId);
        Voucher voucher = voucherRepository.findById(voucherId).orElseThrow(() -> new RuntimeException("Voucher not found"));

        Order voucherOrder = new Order();
        voucherOrder.setCustomer(customer);
        voucherOrder.setVoucher(voucher);
        voucherOrder.setVoucherAcquisition(true);
        voucherOrder.setOrderDate(new Date());
        voucherOrder.setStatus("Pending"); // Must be one of: Pending, Paid, Shipped, Delivered, Cancelled
        voucherOrder.setTotalAmount(BigDecimal.ZERO);

        orderRepository.save(voucherOrder);
    }

    @Override
    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public Optional<Customer> findCustomerByProviderAndProviderId(String provider, String providerId) {
        return customerRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * Xử lý đăng ký/đăng nhập bằng tài khoản Google/OAuth2
     * Tự động tạo Customer mới hoặc cập nhật thông tin nếu đã tồn tại
     */
    @Override
    public Customer processOAuth2User(OAuth2User oAuth2User, String provider) {
        // Lấy thông tin cơ bản từ OAuth2 provider
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getName();

        // Kiểm tra xem email đã tồn tại trong hệ thống chưa
        Optional<Customer> existingCustomer = findCustomerByEmail(email);
        Customer customer;

        if (existingCustomer.isPresent()) {
            // Nếu đã tồn tại: cập nhật thông tin provider
            customer = existingCustomer.get();
            customer.setName(name);
            customer.setDisplay_name(name);
            customer.setProvider(provider);
            customer.setProviderId(providerId);
            customer.setUpdated_at(LocalDateTime.now());
        } else {
            // Nếu chưa tồn tại: tạo Customer mới
            customer = new Customer(email, name, provider, providerId);
        }

        // Lưu vào database và trả về
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Override
    public void changePassword(String email, ChangePasswordDTO dto) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email: " + email));
        if (!passwordEncoder.matches(dto.getOldPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận không khớp.");
        }
        customer.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        customer.setUpdated_at(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public BigDecimal calculateTotalSpending(Integer customerId) {
        List<Order> deliveredOrders = orderRepository.findByCustomer_CustomerIdAndStatus(customerId, "Delivered");
        List<Order> paidOrders = orderRepository.findByCustomer_CustomerIdAndStatus(customerId, "Paid");

        BigDecimal totalSpending = BigDecimal.ZERO;
        for (Order order : deliveredOrders) {
            if (order.getTotalAmount() != null) {
                totalSpending = totalSpending.add(order.getTotalAmount());
            }
        }
        for (Order order : paidOrders) {
            if (order.getTotalAmount() != null) {
                totalSpending = totalSpending.add(order.getTotalAmount());
            }
        }
        return totalSpending;
    }

    // Wishlist methods
    @Override
    public List<Integer> getWishlistProductIds(Integer customerId) {
        return wishlistRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(w -> w.getProduct().getProductId())
                .toList();
    }

    @Override
    @Transactional
    public void addProductToWishlist(Integer customerId, Integer productId) {
        if (!wishlistRepository.existsByCustomerCustomerIdAndProductProductId(customerId, productId)) {
            Wishlist wishlist = new Wishlist();
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new UserNotFoundException("Customer not found"));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            wishlist.setCustomer(customer);
            wishlist.setProduct(product);
            wishlist.setCreatedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    @Transactional
    public void removeProductFromWishlist(Integer customerId, Integer productId) {
        wishlistRepository.deleteByCustomerCustomerIdAndProductProductId(customerId, productId);
    }

    @Override
    public boolean isProductInWishlist(Integer customerId, Integer productId) {
        return wishlistRepository.existsByCustomerCustomerIdAndProductProductId(customerId, productId);
    }
}