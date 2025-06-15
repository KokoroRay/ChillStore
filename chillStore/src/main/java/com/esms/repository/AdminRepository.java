package com.esms.repository;

import com.esms.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    Optional<Admin> findByEmail(String email);

}
