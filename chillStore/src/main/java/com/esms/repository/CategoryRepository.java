package com.esms.repository;

import com.esms.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCase(String keyword);

    Optional<Category> findById(Integer id);
    @Query("SELECT MAX(c.id) FROM Category c")
    Integer findMaxId();

    Page<Category> findAll(Pageable pageable);
}
