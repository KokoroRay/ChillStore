package com.esms.repository;

import com.esms.model.entity.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StaffRepository extends JpaRepository<Staff, Integer> {

    @Query("SELECT s FROM Staff s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:gender IS NULL OR s.gender = :gender)")
    List<Staff> findByKeywordAndGender(@Param("keyword") String keyword,
                                       @Param("gender") Staff.Gender gender);

    @Query("SELECT s FROM Staff s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:gender IS NULL OR s.gender = :gender)")
    Page<Staff> searchStaff(@Param("keyword") String keyword,
                            @Param("gender") Staff.Gender gender,
                            Pageable pageable);


    Optional<Staff> findByEmail(String email);
}
