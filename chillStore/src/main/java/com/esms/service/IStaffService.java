package com.esms.service;

import com.esms.model.entity.Staff;

import java.util.List;

public interface IStaffService {
    // add
    public Staff addStaff(Staff staff);

    //update
    public Staff updateStaff(int id, Staff staff);

    //delete
    public boolean deleteStaff(int id);

    //show list
    public List<Staff> getAllStaff();
    // get 1 staff
    public Staff getOneStaff(int id);

    List<Staff> searchStaff(String keyword, String gender);


}
