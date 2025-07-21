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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esms.model.dto.MaintenanceDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

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
    public String viewMaintenanceList(Model model, HttpServletRequest request) {
        try {
            List<MaintenanceDto> maintenances = maintenanceService.getAllMaintenances();
            logger.info("Retrieved {} maintenance records", maintenances.size());
            model.addAttribute("maintenances", maintenances);
            model.addAttribute("activeMenu", "maintenance");
            String requestUrl = request.getRequestURI();
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
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/list";
            } else {
                return "admin/maintenance/maintenancelist";
            }
        }
    }

    @GetMapping("/detail/{id}")
    public String viewMaintenanceDetail(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        try {
            MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
            if (maintenance == null) {
                model.addAttribute("error", "Maintenance not found with ID: " + id);
                return "redirect:/admin/maintenance";
            }
            model.addAttribute("maintenance", maintenance);
            model.addAttribute("activeMenu", "maintenance");
            String requestUrl = request.getRequestURI();
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/detail";
            } else {
                return "admin/maintenance/maintenancedetail";
            }
        } catch (Exception e) {
            logger.error("Error retrieving maintenance detail for ID: {}", id, e);
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
            model.addAttribute("maintenanceDto", maintenance);
            model.addAttribute("activeMenu", "maintenance");
            
            // Add dropdown data
            List<Order> orders = orderRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            List<Staff> staffList = staffRepository.findAll();
            
            model.addAttribute("orders", orders);
            model.addAttribute("products", products);
            model.addAttribute("customers", customers);
            model.addAttribute("staffList", staffList);
            
            String requestUrl = request.getRequestURI();
            if (requestUrl.startsWith("/staff")) {
                return "staff/maintenance/edit";
            } else {
                return "admin/maintenance/editmaintenance";
            }
        } catch (Exception e) {
            logger.error("Error retrieving maintenance for edit, ID: {}", id, e);
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
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors in maintenance update form");
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
        List<Order> orders = orderRepository.findAll();
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
            
            maintenance.setStatus(status);
            maintenanceService.updateMaintenance(maintenance);
            
            logger.info("Updated maintenance status to {} for ID: {}", status, id);
            redirectAttributes.addFlashAttribute("success", "Maintenance status updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating maintenance status, ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "redirect:/staff/maintenance";
        } else {
            return "redirect:/admin/maintenance";
        }
    }
}
