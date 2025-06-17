package com.esms.controller;

import com.esms.model.dto.WarehouseDto;
import com.esms.model.entity.Warehouse;
import com.esms.model.entity.Product;
import com.esms.service.WarehouseService;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/warehouse")
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;
    
    @Autowired
    private ProductService productService;

    // Hiển thị tất cả các giao dịch kho với phân trang
    @GetMapping
    public String viewWarehouse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transDate").descending());
        Page<Warehouse> warehousePage = warehouseService.getAllWarehouseTransactions(pageable);
        List<WarehouseDto> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "warehouse/view";
    }

    // Tìm kiếm theo tên sản phẩm với phân trang
    @GetMapping("/search")
    public String searchByProductName(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transDate").descending());
        Page<Warehouse> warehousePage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            warehousePage = warehouseService.searchWarehouseByProductName(keyword, pageable);
        } else {
            warehousePage = warehouseService.getAllWarehouseTransactions(pageable);
        }
        
        List<WarehouseDto> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "warehouse/view";
    }

    // Hiển thị form nhập kho
    @GetMapping("/import")
    public String showImportForm(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "warehouse/import";
    }

    // Xử lý nhập kho
    @PostMapping("/import")
    public String importWarehouse(
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {
        try {
            warehouseService.importProduct(productId, quantity, notes);
            redirectAttributes.addFlashAttribute("success", "Import successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/admin/warehouse";
    }

    // Hiển thị form xuất kho
    @GetMapping("/export")
    public String showExportForm(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "warehouse/export";
    }

    // Xử lý xuất kho
    @PostMapping("/export")
    public String exportWarehouse(
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {
        try {
            warehouseService.exportProduct(productId, quantity, notes);
            redirectAttributes.addFlashAttribute("success", "Export successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Export failed: " + e.getMessage());
        }
        return "redirect:/admin/warehouse";
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

    // Xem log với phân trang
    @GetMapping("/log")
    public String viewLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transDate").descending());
        Page<Warehouse> warehousePage = warehouseService.getAllWarehouseTransactions(pageable);
        List<WarehouseDto> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "warehouse/log";
    }
}
