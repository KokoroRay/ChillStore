package com.esms.service;

import com.esms.model.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    // add
    Staff addStaff(Staff staff);

    boolean isNationalIdExists(String nationalId);

    boolean isEmailExists(String email);

    //update
    Staff updateStaff(int id, Staff staff);

    //delete
    boolean deleteStaff(int id);

    //show list
    List<Staff> getAllStaff();

    // get 1 staff
    Staff getOneStaff(int id);

    List<Staff> searchStaff(String keyword, String gender);

    Page<Staff> getAllStaff(Pageable pageable);

    Page<Staff> searchStaff(String keyword, Staff.Gender gender, Pageable pageable);


}
