package com.esms.service.impl;


import com.esms.exception.EmailAlreadyUsedException;
import com.esms.model.dto.RegisterDto;
import com.esms.model.entity.Customer;
import com.esms.repository.CustomerRepository;
import com.esms.service.CustomerService;
import com.esms.util.MapperUtils.CustomerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
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
}
