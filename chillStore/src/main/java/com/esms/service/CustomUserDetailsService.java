package com.esms.service;

import com.esms.model.entity.Admin;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Staff;
import com.esms.repository.AdminRepository;
import com.esms.repository.CustomerRepository;
import com.esms.repository.StaffRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, StaffRepository staffRepository,
                                    AdminRepository adminRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.adminRepository = adminRepository;
    }

    //kiểm tra coi đứa nào đăng nhập để cấp quyền công dân
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        //kiểm tra tài khoản đăng nhập có phải Admin hông, nếu hông thì thui
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            List<GrantedAuthority> authorities  = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPassword(),
                    authorities
            );
        }

        //kiểm tra tài khoản đăng nhập có phải staff hông, nếu hông phải staff thì cho xuống dưới tiếp
        Staff staff = staffRepository.findByEmail(email).orElse(null);
        if (staff != null) {
            List<GrantedAuthority> authorities  = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
            return new org.springframework.security.core.userdetails.User(
                    staff.getEmail(),
                    staff.getPassword(),
                    authorities
            );
        }

        //hông phải customer thì 1 là chưa có tài khoản trong hệ thốnng 2 là mấy hét cơ =))))
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<GrantedAuthority> authorities  = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        return new org.springframework.security.core.userdetails.User(
                customer.getEmail(),
                customer.getPassword(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !customer.isLocked(), // accountNonLocked - false nếu bị khóa
                authorities
        );


    }
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElse(null);
    }


}