package com.esms.repository;

import com.esms.model.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Integer> {
    // Có thể bổ sung custom query nếu cần
    boolean existsByStaffId(int staffId);

}
