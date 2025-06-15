package com.esms.repository;

import com.esms.model.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    
    @Query("SELECT w FROM Warehouse w WHERE w.product.productId = :productId")
    List<Warehouse> findByProductId(@Param("productId") Integer productId);
    
    List<Warehouse> findByType(String type);
    
    List<Warehouse> findByTransDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT w FROM Warehouse w JOIN w.product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Warehouse> searchByProductName(@Param("productName") String productName);
}
