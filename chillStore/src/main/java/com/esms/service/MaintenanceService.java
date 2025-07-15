package com.esms.service;

import java.util.List;

import com.esms.model.dto.MaintenanceDto;

public interface MaintenanceService {
    List<MaintenanceDto> getAllMaintenances();

    MaintenanceDto getMaintenanceById(Integer id);

    void updateMaintenance(MaintenanceDto dto);

    void addMaintenance(MaintenanceDto dto);
    // Có thể bổ sung các phương thức khác (add, update, delete) sau
}
