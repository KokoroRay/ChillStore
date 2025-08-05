package com.esms.service.impl;

import com.esms.model.entity.Staff;
import com.esms.repository.OrderRepository;
import com.esms.repository.StaffRepository;
import com.esms.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderRepository orderRepository;


    @Override
    public Staff addStaff(Staff staff) {
        if (staff != null) {
            // Mã hóa mật khẩu trước khi lưu vào DB
            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
            return staffRepository.save(staff);
        }
        return null;
    }

    @Override
    public Staff updateStaff(int id, Staff staff) {
        if (staff != null) {
            Staff existingStaff = staffRepository.getById(id);
            if (existingStaff != null) {
                existingStaff.setName(staff.getName());
                existingStaff.setEmail(staff.getEmail());
                // Nếu người dùng nhập mật khẩu mới, mã hóa rồi cập nhật
                if (staff.getPassword() != null && !staff.getPassword().isBlank()) {
                    existingStaff.setPassword(passwordEncoder.encode(staff.getPassword()));
                }
                existingStaff.setPhone(staff.getPhone());
                existingStaff.setAddress(staff.getAddress());
                existingStaff.setGender(staff.getGender());
                existingStaff.setNationalId(staff.getNationalId());
                return staffRepository.save(existingStaff);
            }
        }
        return null;
    }

    @Override
    public boolean deleteStaff(int id) {
        if (id >= 1) {
            Optional<Staff> staffOpt = staffRepository.findById(id);
            if (staffOpt.isPresent()) {
                boolean hasOrders = orderRepository.existsByStaffId(id);
                if (hasOrders) {
                    return false;
                }
                staffRepository.delete(staffOpt.get());
                return true;
            }
        }
        return false;
    }


    @Override
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @Override
    public Staff getOneStaff(int id) {
        return staffRepository.getById(id);
    }

    @Override
    public List<Staff> searchStaff(String keyword, String genderStr) {
        Staff.Gender gender = null;
        if (genderStr != null && !genderStr.isBlank()) {
            gender = Staff.Gender.valueOf(genderStr);// Chuyển String về enum Gender
        }
        return staffRepository.findByKeywordAndGender(keyword, gender);
    }
    @Override
    public Page<Staff> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable);
    }
    @Override
    public Page<Staff> searchStaff(String keyword, Staff.Gender gender, Pageable pageable) {
        return staffRepository.searchStaff(keyword, gender, pageable);
    }
    @Override
    public boolean isNationalIdExists(String nationalId) {
        return staffRepository.existsByNationalId(nationalId);
    }
    @Override
    public boolean isEmailExists(String email) {
        return staffRepository.existsByEmail(email);
    }







}
