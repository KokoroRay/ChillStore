package com.esms.repository;

import com.esms.model.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    // Tìm kiếm warehouse theo tên sản phẩm (không phân biệt hoa thường)
    @Query("SELECT w FROM Warehouse w JOIN w.product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Warehouse> searchByProductName(@Param("productName") String productName);
}
