package com.esms.controller;

import com.esms.model.entity.Staff;
import com.esms.service.IStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class StaffController {

    @Autowired
    private IStaffService iStaffService;

    // Hiển thị form thêm mới staff
    @GetMapping("/add-form")
    public String addForm(Model model) {
        model.addAttribute("staff", new Staff());
        return "admin/manageStaff/add-staff";
    }

    // Xử lý thêm mới staff
    @PostMapping("/addStaff")
    public String addStaff(@ModelAttribute Staff staff, Model model) {
        iStaffService.addStaff(staff);
        return "redirect:/admin/ManageStaff"; // đã cập nhật lại link đúng
    }

    // Hiển thị form cập nhật staff
    @GetMapping("/update-form")
    public String showUpdateForm(@RequestParam("id") int id, Model model) {
        Staff staff = iStaffService.getOneStaff(id);
        model.addAttribute("staff", staff);
        return "admin/manageStaff/update-staff";
    }

    // Xử lý cập nhật staff
    @PostMapping("/updateStaff")
    public String updateStaff(@RequestParam("id") int id, @ModelAttribute Staff staff) {
        iStaffService.updateStaff(id, staff);
        return "redirect:/admin/ManageStaff"; // đã cập nhật lại link đúng
    }

    // Xử lý xoá staff
    @GetMapping("/deleteStaff/{id}")
    public String deleteStaff(@PathVariable("id") int id) {
        iStaffService.deleteStaff(id);
        return "redirect:/admin/ManageStaff"; // đã cập nhật lại link đúng
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
                genderEnum = null; // hoặc log lỗi nếu cần
            }
        }

        if ((keyword != null && !keyword.isBlank()) || genderEnum != null) {
            staffPage = iStaffService.searchStaff(keyword, genderEnum, pageable);
        } else {
            staffPage = iStaffService.getAllStaff(pageable);
        }

        model.addAttribute("staffList", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);

        return "admin/manageStaff/manageStaff";
    }
}
