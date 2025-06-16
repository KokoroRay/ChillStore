package com.esms.repository;

import com.esms.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByNationalId(String nationalId);
    Optional<Admin> findByPhone(String phone);

    //Sao the
} 