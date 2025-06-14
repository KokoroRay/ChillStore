package com.esms.controller;

import com.esms.model.entity.Warehouse;
import com.esms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    // View all warehouse transactions
    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouseTransactions() {
        return ResponseEntity.ok(warehouseService.getAllWarehouseTransactions());
    }

    // View single warehouse transaction
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseTransactionById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(warehouseService.getWarehouseTransactionById(id));
    }

    // View warehouse transactions by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Warehouse>> getWarehouseTransactionsByProduct(@PathVariable("productId") Integer productId) {
        return ResponseEntity.ok(warehouseService.getWarehouseTransactionsByProductId(productId));
    }

    // Add new warehouse transaction
    @PostMapping
    public ResponseEntity<Warehouse> addWarehouseTransaction(@RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.addWarehouseTransaction(warehouse));
    }

    // Update warehouse transaction
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouseTransaction(
            @PathVariable("id") Integer id,
            @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.updateWarehouseTransaction(id, warehouse));
    }

    // Delete warehouse transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouseTransaction(@PathVariable("id") Integer id) {
        warehouseService.deleteWarehouseTransaction(id);
        return ResponseEntity.ok().build();
    }

    // Search warehouse transactions by product name
    @GetMapping("/search")
    public ResponseEntity<List<Warehouse>> searchWarehouseByProductName(@RequestParam String productName) {
        return ResponseEntity.ok(warehouseService.searchWarehouseByProductName(productName));
    }
}