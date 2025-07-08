package com.esms.model.dto;

import com.esms.model.entity.Staff;

public class StaffDto {
    private Integer id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String nationalId;
    private Staff.Gender gender;

    public StaffDto() {
    }

    public StaffDto(Integer id, String name, String email, String password, String phone, String address, String nationalId, Staff.Gender gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.nationalId = nationalId;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Staff.Gender getGender() {
        return gender;
    }

    public void setGender(Staff.Gender gender) {
        this.gender = gender;
    }
}

