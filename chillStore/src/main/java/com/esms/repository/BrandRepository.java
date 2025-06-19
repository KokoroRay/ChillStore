package com.esms.repository;

import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query("SELECT b FROM Brand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Brand> findAllByOrderByNameAsc(Pageable pageable);

    List<Brand> findAllByIdIn(List<Integer> ids);

    boolean existsByName(String name);

    List<Brand> findAllById(List<Long> brandIds);
} 