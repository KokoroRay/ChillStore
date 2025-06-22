package com.esms.repository;

import com.esms.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Customer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:locked IS NULL OR c.isLocked = :locked)")
    Page<Customer> findWithFilters(@Param("keyword") String keyword, @Param("locked") Boolean locked, Pageable pageable);

    @Query("SELECT DISTINCT c.name FROM Customer c WHERE c.name LIKE %:keyword%")
    List<String> findTop5ByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:locked IS NULL OR c.isLocked = :locked)")
    Page<Customer> searchCustomersWithFilters(@Param("keyword") String keyword,
                                              @Param("gender") String gender,
                                              @Param("locked") Boolean locked,
                                              Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.display_name) LIKE LOWER(CONCAT('%', :displayName, '%'))")
    Page<Customer> searchByDisplayName(@Param("displayName") String displayName, Pageable pageable);

}
