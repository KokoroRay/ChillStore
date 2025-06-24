package com.esms.service;

import com.esms.model.dto.MaintenanceDto;
import java.util.List;

public interface MaintenanceService {
    List<MaintenanceDto> getAllMaintenances();
    MaintenanceDto getMaintenanceById(Integer id);
    void updateMaintenance(MaintenanceDto dto);
    void addMaintenance(MaintenanceDto dto);
    // Có thể bổ sung các phương thức khác (add, update, delete) sau
}
