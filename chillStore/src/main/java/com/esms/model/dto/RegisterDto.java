package com.esms.model.dto;

import com.esms.validation.StrongPassword;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Date;

public class RegisterDto {
    private Integer customerId;

    @NotBlank(message = "tên đăng nhập không chứa khoản trắng nhá")
    @NotEmpty @Size(min = 6, max = 12)
    @Pattern(regexp = "(?=.*[a-zA-Z])[a-zA-Z0-9]+$",
            message = "Chỉ được chứa chữ cái và số, không có khoảng trắng hoặc ký tự đặc biệt, chuỗi phải có chữ không được chỉ số")
    private String name;

//    @NotBlank(message = "tên không được có khoảng trắng")
//    @Size(min = 6, max = 12)
    private String display_name;

    @Email(message = "email invalid format")
    private String email;

    //kiểm tra format code
    @StrongPassword
    private String password;

    @NotBlank(message = "số điện thoại không được rổng")
    @Pattern(regexp = "^[0-9]{10}$", message = "số điện thoại đúng 10 số và từ 0 đến 9")
    private String phone;
    private String address;
    private Date birth_date;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;


    public RegisterDto() {
    }

    public RegisterDto(Integer customerId, String name, String display_name, String email, String password, String phone, String address, Date birth_date, LocalDateTime created_at, LocalDateTime updated_at) {
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
