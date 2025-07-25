package com.esms.controller;

import com.esms.model.dto.WarehouseDTO;
import com.esms.model.entity.Warehouse;
import com.esms.model.entity.Product;
import com.esms.service.WarehouseService;
import com.esms.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping({"/admin/warehouse", "/staff/warehouse"})
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ProductService productService;

    // Hiển thị tất cả các giao dịch kho với phân trang
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String viewWarehouse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpServletRequest response) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transDate").descending());
        Page<Warehouse> warehousePage = warehouseService.getAllWarehouseTransactions(pageable);
        List<WarehouseDTO> transactions = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehousePage.getTotalPages());
        model.addAttribute("totalItems", warehousePage.getTotalElements());
        String requestUrl = response.getRequestURI();
        if (requestUrl.startsWith("/staff") ){
            return "staff/warehouse/view";
        } else {
            return "admin/warehouse/view";
        }
    }

    // Tìm kiếm theo tên sản phẩm với phân trang
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String searchByProductName(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpServletRequest response) {
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
        String requestUrl = response.getRequestURI();
        if(requestUrl.startsWith("/staff")) {
            return "staff/warehouse/view";
        } else {
            return "admin/warehouse/view";
        }
    }

    // Hiển thị form nhập kho
    @GetMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showImportForm(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/warehouse/import";
    }

    // Xử lý nhập kho
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN')")
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

    /**
     * Xử lý import warehouse bằng file Excel (theo Product Name)
     * File mẫu: ProductName | Quantity | Notes
     */
    @PostMapping("/import-excel")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String importWarehouseExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                       RedirectAttributes redirectAttributes) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMsg = new StringBuilder();
            // Duyệt từng dòng, bỏ qua header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String productName = row.getCell(0) != null ? row.getCell(0).getStringCellValue().trim() : null;
                Integer quantity = null;
                if (row.getCell(1) != null) {
                    if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                        quantity = (int) row.getCell(1).getNumericCellValue();
                    } else if (row.getCell(1).getCellType() == CellType.STRING) {
                        try { quantity = Integer.parseInt(row.getCell(1).getStringCellValue().trim()); } catch (Exception ignored) {}
                    }
                }
                String notes = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;
                if (productName == null || quantity == null || quantity <= 0) {
                    failCount++;
                    errorMsg.append("Dòng ").append(i+1).append(": Thiếu hoặc sai định dạng Product Name/Quantity. ");
                    continue;
                }
                // Tìm sản phẩm theo tên (không phân biệt hoa thường)
                Product product = productService.getProductByName(productName);
                if (product == null) {
                    failCount++;
                    errorMsg.append("Dòng ").append(i+1).append(": Không tìm thấy sản phẩm với tên '").append(productName).append("'. ");
                    continue;
                }
                try {
                    warehouseService.importProduct(product.getProductId(), quantity, notes);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errorMsg.append("Dòng ").append(i+1).append(": Lỗi nhập kho: ").append(e.getMessage()).append(". ");
                }
            }
            if (successCount > 0) {
                redirectAttributes.addFlashAttribute("success", "Import thành công " + successCount + " dòng. " + (failCount > 0 ? (failCount + " dòng lỗi.") : ""));
            }
            if (failCount > 0) {
                redirectAttributes.addFlashAttribute("error", errorMsg.toString());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import Excel thất bại: " + e.getMessage());
        }
        return "redirect:/admin/warehouse";
    }

    /**
     * Endpoint trả về file Excel mẫu cho import warehouse (ProductName, Quantity, Notes)
     */
    @GetMapping("/import-sample")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void downloadImportSample(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=warehouse_import_sample.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ImportSample");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ProductName");
            header.createCell(1).setCellValue("Quantity");
            header.createCell(2).setCellValue("Notes");
            // Thêm 1 dòng ví dụ
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("Tủ lạnh Samsung");
            example.createCell(1).setCellValue(10);
            example.createCell(2).setCellValue("Nhập mới");
            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
            workbook.write(response.getOutputStream());
        }
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
    @PreAuthorize("hasAnyRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN')")
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
