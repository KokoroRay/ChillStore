package com.esms.service.impl;

import com.esms.model.entity.Warehouse;
import com.esms.repository.WarehouseRepository;
import com.esms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public List<Warehouse> getAllWarehouseTransactions() {
        return warehouseRepository.findAll();
    }

    @Override
    public Warehouse getWarehouseTransactionById(Integer id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse transaction not found with id: " + id));
    }

    @Override
    public List<Warehouse> getWarehouseTransactionsByProductId(Integer productId) {
        return warehouseRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public Warehouse addWarehouseTransaction(Warehouse warehouse) {
        if (warehouse.getTransDate() == null) {
            warehouse.setTransDate(LocalDateTime.now());
        }
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse updateWarehouseTransaction(Integer id, Warehouse warehouse) {
        Warehouse existingWarehouse = getWarehouseTransactionById(id);
        existingWarehouse.setProduct(warehouse.getProduct());
        existingWarehouse.setQuantityChange(warehouse.getQuantityChange());
        existingWarehouse.setStockAfter(warehouse.getStockAfter());
        existingWarehouse.setType(warehouse.getType());
        existingWarehouse.setAdmin(warehouse.getAdmin());
        existingWarehouse.setNotes(warehouse.getNotes());
        return warehouseRepository.save(existingWarehouse);
    }

    @Override
    @Transactional
    public void deleteWarehouseTransaction(Integer id) {
        if (!warehouseRepository.existsById(id)) {
            throw new RuntimeException("Warehouse transaction not found with id: " + id);
        }
        warehouseRepository.deleteById(id);
    }

    @Override
    public List<Warehouse> searchWarehouseByProductName(String productName) {
        return warehouseRepository.searchByProductName(productName);
    }

    @Override
    public List<Warehouse> findByTransactionType(String type) {
        return warehouseRepository.findByType(type);
    }

    @Override
    public List<Warehouse> findByDateRange(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            return warehouseRepository.findByTransDateBetween(start, end);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Please use yyyy-MM-dd HH:mm:ss format", e);
        }
    }
}