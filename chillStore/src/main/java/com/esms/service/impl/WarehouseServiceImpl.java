package com.esms.service.impl;

import com.esms.model.entity.Warehouse;
import com.esms.repository.WarehouseRepository;
import com.esms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Override
    public Warehouse findById(Integer id) {
        return warehouseRepository.findById(id).orElse(null);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        warehouseRepository.deleteById(id);
    }

    @Override
    public List<Warehouse> getAllWarehouseTransactions() {
        return warehouseRepository.findAll();
    }

    @Override
    public List<Warehouse> searchWarehouseByProductName(String productName) {
        return warehouseRepository.searchByProductName(productName);
    }
} 