package com.esms.controller;

import com.esms.model.dto.StaffDTO;
import com.esms.model.entity.Staff;
import com.esms.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class StaffController {

    @Autowired
    private StaffService staffService;

    // Hiển thị form thêm mới staff
    @GetMapping("/add-form")
    public String addForm(Model model) {
        model.addAttribute("staff", new StaffDTO());
        return "admin/manageStaff/add-staff";
    }

    // Xử lý thêm mới staff
    @PostMapping("/addStaff")
    public String addStaff(@Valid @ModelAttribute("staff") StaffDTO staffDto,
                           BindingResult bindingResult,
                           Model model) {

        if (staffService.isEmailExists(staffDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email already exists");
        }
        if (!staffDto.getPassword().equals(staffDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }
        if (staffService.isNationalIdExists(staffDto.getNationalId())) {
            bindingResult.rejectValue("nationalId", "error.nationalId", "National ID already exists");
        }

        if (bindingResult.hasErrors()) {
            return "admin/manageStaff/add-staff";
        }

        Staff staff = new Staff();
        staff.setName(staffDto.getName());
        staff.setEmail(staffDto.getEmail());
        staff.setPassword(staffDto.getPassword());
        staff.setPhone(staffDto.getPhone());
        staff.setAddress(staffDto.getAddress());
        staff.setNationalId(staffDto.getNationalId());
        staff.setGender(Staff.Gender.valueOf(staffDto.getGender()));

        staffService.addStaff(staff);
        return "redirect:/admin/ManageStaff";
    }

    // Hiển thị form cập nhật staff
    @GetMapping("/update-form")
    public String showUpdateForm(@RequestParam("id") int id, Model model) {
        Staff staff = staffService.getOneStaff(id);
        model.addAttribute("staff", staff);
        return "admin/manageStaff/update-staff";
    }

    // Xử lý cập nhật staff
    @PostMapping("/updateStaff")
    public String updateStaff(@RequestParam("id") int id, @ModelAttribute Staff staff) {
        staffService.updateStaff(id, staff);
        return "redirect:/admin/ManageStaff";
    }

    // Xử lý xoá staff
    @GetMapping("/deleteStaff/{id}")
    public String deleteStaff(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        boolean deleted = staffService.deleteStaff(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhân viên thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa nhân viên (đã có đơn hàng liên quan hoặc không tồn tại).");
        }
        return "redirect:/admin/ManageStaff";
    }



    // Hiển thị danh sách staff
    @GetMapping("/ManageStaff")
    public String showStaffList(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "gender", required = false) String gender,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Staff> staffPage = null;

        Staff.Gender genderEnum = null;
        if (gender != null && !gender.isBlank()) {
            try {
                genderEnum = Staff.Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                genderEnum = null;
            }
        }

        if ((keyword != null && !keyword.isBlank()) || genderEnum != null) {
            staffPage = staffService.searchStaff(keyword, genderEnum, pageable);
        } else {
            staffPage = staffService.getAllStaff(pageable);
        }

        model.addAttribute("staffList", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);

        return "admin/manageStaff/manageStaff";
    }
}
