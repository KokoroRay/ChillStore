package com.esms.service.impl;

import com.esms.model.entity.Staff;
import com.esms.repository.StaffRepository;
import com.esms.service.IStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class StaffServiceImpl implements IStaffService {
    @Autowired
    private StaffRepository staffRepository;


    @Override
    public Staff addStaff(Staff staff) {
        if (staff != null) {
            return staffRepository.save(staff);
        }
        return null;
    }

    @Override
    public Staff updateStaff(int id, Staff staff) {
        if (staff != null) {
            Staff staff1 = staffRepository.getById(id);
            if (staff1 != null) {
                Staff existingStaff = staffRepository.getById(id);

                staff1.setName(staff.getName());
                staff1.setEmail(staff.getEmail());
                if (staff.getPassword() != null && !staff.getPassword().isBlank()) {
                    existingStaff.setPassword(staff.getPassword());
                }                staff1.setPhone(staff.getPhone());
                staff1.setAddress(staff.getAddress());
                staff1.setGender(staff.getGender());
                staff1.setNational_id(staff.getNational_id());
                return staffRepository.save(staff1);
            }
        }
        return null;
    }

    @Override
    public boolean deleteStaff(int id) {
        if(id >= 1) {
            Staff staff = staffRepository.getById(id);
            if(staff != null) {
                staffRepository.delete(staff);
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
            gender = Staff.Gender.valueOf(genderStr);
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





}
