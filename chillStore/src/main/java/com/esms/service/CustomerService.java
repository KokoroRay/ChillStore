package com.esms.service;

import com.esms.model.dto.ForgotPasswordDto;
import com.esms.model.dto.RegisterDto;
import com.esms.model.dto.ResetPasswordDto;
import com.esms.model.dto.OtpDto;

public interface CustomerService {
    void register(RegisterDto dto);

    void sendResetOtp(ForgotPasswordDto dto);
    boolean verifyOtp(OtpDto dto);
    void resetPassword(ResetPasswordDto dto);
}
