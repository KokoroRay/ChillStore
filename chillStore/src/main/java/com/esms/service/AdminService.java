package com.esms.service;

import com.esms.model.entity.Admin;

import java.util.Optional;

public interface AdminService {
    Optional<Admin> findByEmail(String email);
}
