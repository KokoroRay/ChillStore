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
    
    List<Warehouse> findByProductId(Integer productId);
    
    List<Warehouse> findByTransactionType(String transactionType);
    
    List<Warehouse> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT w FROM Warehouse w JOIN w.product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Warehouse> searchByProductName(@Param("productName") String productName);
}
