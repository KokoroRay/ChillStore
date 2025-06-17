package com.esms.service.impl;

import com.esms.model.entity.Warehouse;
import com.esms.model.entity.Product;
import com.esms.model.entity.Admin;
import com.esms.repository.WarehouseRepository;
import com.esms.repository.ProductRepository;
import com.esms.repository.AdminRepository;
import com.esms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdminRepository adminRepository;

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
    public Page<Warehouse> getAllWarehouseTransactions(Pageable pageable) {
        return warehouseRepository.findAll(pageable);
    }

    @Override
    public List<Warehouse> getAllWarehouseTransactions() {
        return warehouseRepository.findAll();
    }

    @Override
    public Page<Warehouse> searchWarehouseByProductName(String keyword, Pageable pageable) {
        return warehouseRepository.findByProductNameContaining(keyword, pageable);
    }

    @Override
    public List<Warehouse> searchWarehouseByProductName(String keyword) {
        return warehouseRepository.searchByProductName(keyword);
    }

    @Override
    public List<Warehouse> findByProductId(Integer productId) {
        return warehouseRepository.findByProductId(productId);
    }

    @Override
    public List<Warehouse> findByType(String type) {
        return warehouseRepository.findByType(type);
    }

    @Override
    public List<Warehouse> findByAdminId(Integer adminId) {
        return warehouseRepository.findByAdminId(adminId);
    }

    @Override
    @Transactional
    public Warehouse importProduct(Integer productId, Integer quantity, String notes) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Tạo giao dịch warehouse mới
        Warehouse warehouse = new Warehouse();
        warehouse.setProduct(product);
        warehouse.setQuantityChange(quantity);
        warehouse.setType("IMPORT");
        warehouse.setTransDate(LocalDateTime.now());
        warehouse.setNotes(notes);

        // Cập nhật số lượng tồn kho
        int newStock = product.getStockQty() + quantity;
        product.setStockQty(newStock);
        warehouse.setStockAfter(newStock);

        // Lưu giao dịch và cập nhật sản phẩm
        productRepository.save(product);
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse exportProduct(Integer productId, Integer quantity, String notes) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra số lượng tồn kho
        if (product.getStockQty() < quantity) {
            throw new RuntimeException("Insufficient stock. Current stock: " + product.getStockQty());
        }

        // Tạo giao dịch warehouse mới
        Warehouse warehouse = new Warehouse();
        warehouse.setProduct(product);
        warehouse.setQuantityChange(-quantity); // Số lượng âm vì là xuất kho
        warehouse.setType("EXPORT");
        warehouse.setTransDate(LocalDateTime.now());
        warehouse.setNotes(notes);

        // Cập nhật số lượng tồn kho
        int newStock = product.getStockQty() - quantity;
        product.setStockQty(newStock);
        warehouse.setStockAfter(newStock);

        // Lưu giao dịch và cập nhật sản phẩm
        productRepository.save(product);
        return warehouseRepository.save(warehouse);
    }
} 