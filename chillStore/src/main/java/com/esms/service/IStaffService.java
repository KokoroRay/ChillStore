package com.esms.service;

import com.esms.model.entity.Staff;

import java.util.List;

public interface IStaffService {
    // add
    Staff addStaff(Staff staff);

    //update
    Staff updateStaff(int id, Staff staff);

    //delete
    boolean deleteStaff(int id);

    //show list
    List<Staff> getAllStaff();
    // get 1 staff
    Staff getOneStaff(int id);

    List<Staff> searchStaff(String keyword, String gender);


}
