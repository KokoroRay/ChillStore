package com.esms.controller;

import com.esms.model.entity.Order;
import com.esms.model.entity.Product;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Staff;
import com.esms.service.MaintenanceService;
import com.esms.repository.OrderRepository;
import com.esms.repository.ProductRepository;
import com.esms.repository.CustomerRepository;
import com.esms.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esms.model.dto.MaintenanceDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping({"/admin/maintenance", "/staff/maintenance"})
public class MaintenanceController {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);
    
    @Autowired
    private MaintenanceService maintenanceService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private StaffRepository staffRepository;

    @GetMapping
    public String viewMaintenanceList(Model model, HttpServletRequest request, org.springframework.security.core.Authentication authentication) {
        try {
            String requestUrl = request.getRequestURI();
            boolean isAdmin = requestUrl.startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            List<MaintenanceDto> maintenances;
            if (requestUrl.startsWith("/staff")) {
                // Lấy staffId hiện tại từ authentication
                String email = authentication != null ? authentication.getName() : null;
                Staff staff = (email != null) ? staffRepository.findByEmail(email).orElse(null) : null;
                Integer staffId = (staff != null) ? staff.getId() : null;
                // Chỉ lấy maintenance có staffId đúng, và staffId != null
                maintenances = maintenanceService.getAllMaintenances().stream()
                    .filter(m -> m.getStaffId() != null && m.getStaffId().equals(staffId))
                    .toList();
            } else {
                // Admin: lấy tất cả
                maintenances = maintenanceService.getAllMaintenances();
            }
            logger.info("Retrieved {} maintenance records", maintenances.size());
            model.addAttribute("maintenances", maintenances);
            model.addAttribute("activeMenu", "maintenance");
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/list";
            } else {
                return "admin/maintenance/maintenancelist";
            }
        } catch (Exception e) {
            logger.error("Error retrieving maintenance list", e);
            model.addAttribute("error", "Error loading maintenance list: " + e.getMessage());
            model.addAttribute("maintenances", List.of());
            model.addAttribute("activeMenu", "maintenance");
            String requestUrl = request.getRequestURI();
            boolean isAdmin = requestUrl.startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/list";
            } else {
                return "admin/maintenance/maintenancelist";
            }
        }
    }

    @GetMapping("/detail/{id}")
    public String viewMaintenanceDetail(@PathVariable("id") Integer id, Model model, HttpServletRequest request, org.springframework.security.core.Authentication authentication) {
        try {
            MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
            if (maintenance == null) {
                model.addAttribute("error", "Maintenance not found with ID: " + id);
                return "redirect:/admin/maintenance";
            }
            model.addAttribute("maintenance", maintenance);
            model.addAttribute("activeMenu", "maintenance");
            String requestUrl = request.getRequestURI();
            boolean isAdmin = requestUrl.startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            // Nếu là staff, lấy staffId hiện tại
            if (requestUrl.startsWith("/staff") && authentication != null) {
                String email = authentication.getName();
                Staff staff = staffRepository.findByEmail(email).orElse(null);
                if (staff != null) {
                    model.addAttribute("currentStaffId", staff.getId());
                }
            }
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/detail";
            } else {
                return "admin/maintenance/maintenancedetail";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading maintenance detail: " + e.getMessage());
            String requestUrl = request.getRequestURI();
            if (requestUrl.startsWith("/staff")) {
                return "redirect:/staff/maintenance";
            } else {
                return "redirect:/admin/maintenance";
            }
        }
    }

    @GetMapping("/edit/{id}")
    public String editMaintenanceForm(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        try {
            MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
            if (maintenance == null) {
                model.addAttribute("error", "Maintenance not found with ID: " + id);
                return "redirect:/admin/maintenance";
            }
            logger.info("[GET] Edit Maintenance - requestType: {}", maintenance.getRequestType());
            model.addAttribute("maintenanceDto", maintenance);
            model.addAttribute("activeMenu", "maintenance");
            // Add dropdown data
            List<Order> orders = orderRepository.findAllForMaintenance(); // Sử dụng custom query
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            String requestUrl = request.getRequestURI();
            boolean isAdmin = requestUrl.startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/editmaintenance";
            } else {
                return "admin/maintenance/editmaintenance";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading maintenance: " + e.getMessage());
            String requestUrl = request.getRequestURI();
            if (requestUrl.startsWith("/staff")) {
                return "redirect:/staff/maintenance";
            } else {
                return "redirect:/admin/maintenance";
            }
        }
    }

    @PostMapping("/update/{id}")
    public String updateMaintenance(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("maintenanceDto") MaintenanceDto maintenanceDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        logger.info("[POST] Update Maintenance - requestType: {}", maintenanceDto.getRequestType());
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in maintenance update form");
            model.addAttribute("activeMenu", "maintenance");
            
            // Add dropdown data back for form re-render
            List<Order> orders = orderRepository.findAllForMaintenance(); // Sử dụng custom query
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            boolean isAdmin = request.getRequestURI().startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            
            return "admin/maintenance/editmaintenance";
        }
        try {
            maintenanceDto.setRequestId(id);
            maintenanceService.updateMaintenance(maintenanceDto);
            logger.info("Successfully updated maintenance with ID: {}", id);
            redirectAttributes.addFlashAttribute("success", "Maintenance schedule updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating maintenance with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("activeMenu", "maintenance");
            
            // Add dropdown data back for form re-render
            List<Order> orders = orderRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            boolean isAdmin = request.getRequestURI().startsWith("/admin");
            model.addAttribute("isAdmin", isAdmin);
            
            return "admin/maintenance/editmaintenance";
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "redirect:/staff/maintenance";
        } else {
            return "redirect:/admin/maintenance";
        }
    }

    @GetMapping("/add")
    public String addMaintenanceForm(Model model, HttpServletRequest request) {
        model.addAttribute("maintenanceDto", new MaintenanceDto());
        model.addAttribute("activeMenu", "maintenance");
        // Add dropdown data
        List<Order> orders = orderRepository.findAllForMaintenance(); // Sử dụng custom query
        List<Product> products = productRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<Staff> staffList = staffRepository.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("products", products);
        model.addAttribute("customers", customers);
        model.addAttribute("staffList", staffList);
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "staff/maintenance/add";
        } else {
            return "admin/maintenance/addmaintenance";
        }
    }

    @PostMapping("/save")
    public String addMaintenance(
            @Valid @ModelAttribute("maintenanceDto") MaintenanceDto maintenanceDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in maintenance add form");
            model.addAttribute("activeMenu", "maintenance");
            
            // Add dropdown data back for form re-render
            List<Order> orders = orderRepository.findAllForMaintenance(); // Sử dụng custom query
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            
            return "admin/maintenance/addmaintenance";
        }
        try {
            // Set default values for new maintenance schedule
            if (maintenanceDto.getRequestDate() == null) {
                maintenanceDto.setRequestDate(LocalDateTime.now());
            }
            if (maintenanceDto.getStatus() == null || maintenanceDto.getStatus().isEmpty()) {
                maintenanceDto.setStatus("Pending"); // Default status from database
            }
            
            maintenanceService.addMaintenance(maintenanceDto);
            logger.info("Successfully added new maintenance");
            redirectAttributes.addFlashAttribute("success", "Maintenance schedule created successfully!");
        } catch (Exception e) {
            logger.error("Error adding maintenance", e);
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("activeMenu", "maintenance");
            
            // Add dropdown data back for form re-render
            List<Order> orders = orderRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            
            return "admin/maintenance/addmaintenance";
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "redirect:/staff/maintenance";
        } else {
            return "redirect:/admin/maintenance";
        }
    }

    // Staff update status endpoint
    @PostMapping("/update-status/{id}")
    public String updateMaintenanceStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
            if (maintenance == null) {
                redirectAttributes.addFlashAttribute("error", "Maintenance not found with ID: " + id);
                String requestUrl = request.getRequestURI();
                if (requestUrl.startsWith("/staff")) {
                    return "redirect:/staff/maintenance";
                } else {
                    return "redirect:/admin/maintenance";
                }
            }
            String currentStatus = maintenance.getStatus();
            boolean valid = false;
            if ("Pending".equals(currentStatus) && "In Progress".equals(status)) {
                valid = true;
            } else if ("In Progress".equals(currentStatus) && "Completed".equals(status)) {
                valid = true;
            }
            if (!valid) {
                redirectAttributes.addFlashAttribute("error", "Chỉ được chuyển trạng thái từ Pending → In Progress → Completed!");
            } else {
                maintenance.setStatus(status);
                maintenanceService.updateMaintenance(maintenance);
                logger.info("Updated maintenance status to {} for ID: {}", status, id);
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            }
        } catch (Exception e) {
            logger.error("Error updating maintenance status, ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "redirect:/staff/maintenance";
        } else {
            return "redirect:/admin/maintenance";
        }
    }

    // API: Set schedule (assign staff, date, status)
    @PostMapping("/api/update-schedule/{id}")
    @ResponseBody
    public ResponseEntity<?> setSchedule(@PathVariable("id") Integer id, @RequestBody MaintenanceDto dto) {
        try {
            MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
            if (maintenance == null) {
                return ResponseEntity.badRequest().body("Maintenance not found");
            }
            if (dto.getStaffId() != null) maintenance.setStaffId(dto.getStaffId());
            if (dto.getRequestDate() != null) maintenance.setRequestDate(dto.getRequestDate());
            if (dto.getStatus() != null) maintenance.setStatus(dto.getStatus());
            maintenanceService.updateMaintenance(maintenance);
            return ResponseEntity.ok("Scheduled");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders/by-customer/{customerId}")
    @ResponseBody
    public List<Map<String, Object>> getOrdersByCustomer(@PathVariable Integer customerId) {
        List<Order> orders = orderRepository.findByCustomerCustomerId(customerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", o.getOrderId());
            map.put("orderDate", o.getOrderDate());
            map.put("status", o.getStatus());
            Map<String, Object> customerMap = new HashMap<>();
            if (o.getCustomer() != null) {
                customerMap.put("name", o.getCustomer().getName());
            }
            map.put("customer", customerMap);
            result.add(map);
        }
        return result;
    }

    @GetMapping("/products/by-order/{orderId}")
    @ResponseBody
    public List<Map<String, Object>> getProductsByOrder(@PathVariable Integer orderId) {
        List<Product> products = productRepository.findProductsByOrderId(orderId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", p.getProductId());
            map.put("name", p.getName());
            Map<String, Object> categoryMap = new HashMap<>();
            if (p.getCategory() != null) {
                categoryMap.put("name", p.getCategory().getName());
            }
            map.put("category", categoryMap);
            Map<String, Object> brandMap = new HashMap<>();
            if (p.getBrand() != null) {
                brandMap.put("name", p.getBrand().getName());
            }
            map.put("brand", brandMap);
            result.add(map);
        }
        return result;
    }
}
