package com.esms.service.impl;

import com.esms.model.dto.MaintenanceDto;
import com.esms.model.entity.Maintenance;
import com.esms.repository.MaintenanceRepository;
import com.esms.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceImpl implements MaintenanceService {
    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Override
    public List<MaintenanceDto> getAllMaintenances() {
        List<Maintenance> maintenances = maintenanceRepository.findAll();
        return maintenances.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public MaintenanceDto getMaintenanceById(Integer id) {
        Maintenance entity = maintenanceRepository.findById(id).orElse(null);
        if (entity == null) return null;
        return toDto(entity);
    }

    @Override
    public void updateMaintenance(MaintenanceDto dto) {
        Maintenance entity = maintenanceRepository.findById(dto.getRequestId()).orElse(null);
        if (entity != null) {
            entity.setOrderId(dto.getOrderId());
            entity.setProductId(dto.getProductId());
            entity.setCustomerId(dto.getCustomerId());
            entity.setRequestType(dto.getRequestType());
            entity.setRequestDate(dto.getRequestDate());
            entity.setReason(dto.getReason());
            entity.setStatus(dto.getStatus());
            maintenanceRepository.save(entity);
        }
    }

    @Override
    public void addMaintenance(MaintenanceDto dto) {
        Maintenance entity = new Maintenance();
        entity.setOrderId(dto.getOrderId());
        entity.setProductId(dto.getProductId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setRequestType(dto.getRequestType());
        entity.setRequestDate(dto.getRequestDate());
        entity.setReason(dto.getReason());
        entity.setStatus(dto.getStatus());
        maintenanceRepository.save(entity);
    }

    private MaintenanceDto toDto(Maintenance entity) {
        return new MaintenanceDto(
                entity.getRequestId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getCustomerId(),
                entity.getRequestType(),
                entity.getRequestDate(),
                entity.getReason(),
                entity.getStatus()
        );
    }
}
