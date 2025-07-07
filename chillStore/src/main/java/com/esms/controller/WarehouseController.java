package com.esms.controller;

import com.esms.model.dto.WarehouseDTO;
import com.esms.model.entity.Warehouse;
import com.esms.model.entity.Product;
import com.esms.service.WarehouseService;
import com.esms.service.ProductService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        List<WarehouseDTO> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "admin/warehouse/view";
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

        List<WarehouseDTO> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "admin/warehouse/view";
    }

    // Hiển thị form nhập kho
    @GetMapping("/import")
    public String showImportForm(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/warehouse/import";
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

    // Chuyển đổi từ Entity sang DTO
    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
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

    // Xem log với phân trang và search
    @GetMapping("/log")
    public String viewLog(
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
        List<WarehouseDTO> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        return "admin/warehouse/log";
    }

    // Export log warehouse ra file Excel
    @GetMapping("/log/export-excel")
    public void exportLogToExcel(@RequestParam(value = "range", required = false, defaultValue = "all") String range, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=warehouse_log.xlsx");
        List<Warehouse> warehouseList = warehouseService.findAll();
        LocalDateTime now = LocalDateTime.now();
        if (!"all".equals(range)) {
            LocalDateTime from = switch (range) {
                case "week" -> now.minus(1, ChronoUnit.WEEKS);
                case "month" -> now.minus(1, ChronoUnit.MONTHS);
                case "year" -> now.minus(1, ChronoUnit.YEARS);
                default -> null;
            };
            if (from != null) {
                warehouseList = warehouseList.stream()
                        .filter(w -> w.getTransDate() != null && w.getTransDate().isAfter(from))
                        .toList();
            }
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Warehouse Log");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            String[] columns = {"ID", "Product Name", "Quantity Change", "Stock After", "Type", "Transaction Date", "Handled By", "Note"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }
            for (Warehouse w : warehouseList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(w.getTransId());
                row.createCell(1).setCellValue(w.getProduct() != null ? w.getProduct().getName() : "");
                row.createCell(2).setCellValue(w.getQuantityChange());
                row.createCell(3).setCellValue(w.getStockAfter());
                row.createCell(4).setCellValue(w.getType());
                row.createCell(5).setCellValue(w.getTransDate() != null ? w.getTransDate().toString() : "");
                row.createCell(6).setCellValue(w.getAdmin() != null ? w.getAdmin().getName() : "");
                row.createCell(7).setCellValue(w.getNotes() != null ? w.getNotes() : "");
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
        }
    }
}
