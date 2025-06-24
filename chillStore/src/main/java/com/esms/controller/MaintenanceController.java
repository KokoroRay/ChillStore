package com.esms.controller;

import com.esms.model.dto.MaintenanceDto;
import com.esms.model.entity.Maintenance;
import com.esms.service.MaintenanceService;
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

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/maintenance")
public class MaintenanceController {
    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    public String viewMaintenanceList(Model model) {
        List<MaintenanceDto> maintenances = maintenanceService.getAllMaintenances();
        model.addAttribute("maintenances", maintenances);
        model.addAttribute("activeMenu", "maintenance");
        return "admin/maintenance/maintenancelist";
    }

    @GetMapping("/detail/{id}")
    public String viewMaintenanceDetail(@PathVariable("id") Integer id, Model model) {
        MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
        model.addAttribute("maintenance", maintenance);
        model.addAttribute("activeMenu", "maintenance");
        return "admin/maintenance/maintenancedetail";
    }

    @GetMapping("/edit/{id}")
    public String editMaintenanceForm(@PathVariable("id") Integer id, Model model) {
        MaintenanceDto maintenance = maintenanceService.getMaintenanceById(id);
        model.addAttribute("maintenanceDto", maintenance);
        model.addAttribute("activeMenu", "maintenance");
        return "admin/maintenance/editmaintenance";
    }

    @PostMapping("/update/{id}")
    public String updateMaintenance(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("maintenanceDto") MaintenanceDto maintenanceDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "maintenance");
            return "admin/maintenance/editmaintenance";
        }
        try {
            maintenanceDto.setRequestId(id);
            maintenanceService.updateMaintenance(maintenanceDto);
            redirectAttributes.addFlashAttribute("success", "Maintenance schedule updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("activeMenu", "maintenance");
            return "admin/maintenance/editmaintenance";
        }
        return "redirect:/admin/maintenance";
    }

    @GetMapping("/add")
    public String addMaintenanceForm(Model model) {
        model.addAttribute("maintenanceDto", new MaintenanceDto());
        model.addAttribute("activeMenu", "maintenance");
        return "admin/maintenance/addmaintenance";
    }

    @PostMapping("/save")
    public String addMaintenance(
            @Valid @ModelAttribute("maintenanceDto") MaintenanceDto maintenanceDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "maintenance");
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
            redirectAttributes.addFlashAttribute("success", "Maintenance schedule created successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("activeMenu", "maintenance");
            return "admin/maintenance/addmaintenance";
        }
        return "redirect:/admin/maintenance";
    }
}
