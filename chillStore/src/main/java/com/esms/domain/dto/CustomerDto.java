package com.esms.domain.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class CustomerDto {
    private Integer customerId;
    private String name;
    private String display_name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private Date birth_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;


    public CustomerDto() {
    }

    public CustomerDto(Integer customerId, String name, String display_name, String email, String password, String phone, String address, Date birth_date, LocalDateTime created_at, LocalDateTime updated_at) {
        this.customerId = customerId;
        this.name = name;
        this.display_name = display_name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
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

    public Date getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }


}
