package com.esms.controller;

import com.esms.model.dto.WarehouseDto;
import com.esms.model.entity.Warehouse;
import com.esms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;

    // Hiển thị tất cả các giao dịch kho
    @GetMapping
    public String viewWarehouse(Model model) {
        List<Warehouse> warehouses = warehouseService.getAllWarehouseTransactions();
        List<WarehouseDto> transactions = warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        model.addAttribute("transactions", transactions);
        return "warehouse/view";
    }

    // Tìm kiếm theo tên sản phẩm
    @GetMapping("/search")
    public String searchByProductName(@RequestParam(required = false) String keyword, Model model) {
        List<Warehouse> warehouses;
        if (keyword != null && !keyword.trim().isEmpty()) {
            warehouses = warehouseService.searchWarehouseByProductName(keyword);
        } else {
            warehouses = warehouseService.getAllWarehouseTransactions();
        }
        
        List<WarehouseDto> transactions = warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);
        return "warehouse/view";
    }

    // Chuyển đổi từ Entity sang DTO
    private WarehouseDto convertToDTO(Warehouse warehouse) {
        WarehouseDto dto = new WarehouseDto();
        dto.setTransId(warehouse.getTransId());
        
        if (warehouse.getProduct() != null) {
            dto.setProductId(warehouse.getProduct().getProductId());
            dto.setProductName(warehouse.getProduct().getName());
        }
        
        dto.setQuantityChange(warehouse.getQuantityChange());
        dto.setStockAfter(warehouse.getStockAfter());
        dto.setType(warehouse.getType());
        dto.setTransDate(warehouse.getTransDate());
        
        if (warehouse.getAdmin() != null) {
            dto.setAdminId(warehouse.getAdmin().getAdminId());
            dto.setAdminName(warehouse.getAdmin().getName());
        }
        
        dto.setNotes(warehouse.getNotes());
        return dto;
    }
}
