package com.esms.model.dto;

import com.esms.validation.PasswordMatch;
import com.esms.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

@PasswordMatch
public class ResetPasswordDTO {

    @NotBlank(message = "email không được để trống")
    private String email;

//    @NotBlank(message = "otp không được để trống")
//    @Pattern(regexp = "\\d{6}", message = "OTP phải gồm 6 số nhá")
//    private String otp;

    //kiểm tra mật khẩu mới người dùng nhập vào có đúng format không
    //hông đúng thì nhập nào đúng nghỉ
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public String getOtp() {
//        return otp;
//    }
//
//    public void setOtp(String otp) {
//        this.otp = otp;
//    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
