package com.esms.service;

import com.esms.model.dto.RegisterDto;

public interface CustomerService {
    void register(RegisterDto dto);
    // Thêm nếu cần: findByEmail, getProfile, v.v.
}
