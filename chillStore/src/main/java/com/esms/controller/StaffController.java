package com.esms.controller;
import com.esms.model.entity.Staff;
import com.esms.service.IStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    private IStaffService iStaffService;

    // test
    @GetMapping("/")
    public String test() {
        return "test";
    }
    @GetMapping("/add-form")
    public String addForm(Model model) {
        model.addAttribute("staff", new Staff());
        return "add-staff";
    }
    //API add staff
    @PostMapping("/add")
    public String addStaff(@ModelAttribute Staff staff, Model  model) {
        iStaffService.addStaff(staff);
        return "redirect:/staff/list";
    }
    @GetMapping("/update-form")
    public String showUpdateForm(@RequestParam("id") int id, Model model) {
        Staff staff = iStaffService.getOneStaff(id);
        model.addAttribute("staff", staff);
        return "update-staff";
    }


    // API update staff
    @PostMapping("/update")
    public String updateStaff(@RequestParam("id") int id, @ModelAttribute Staff staff) {
        iStaffService.updateStaff(id, staff);
        return "redirect:/staff/list";
    }
    //API delete staff
    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable("id") int id) {
        iStaffService.deleteStaff(id);
        return "redirect:/staff/list";    }
    //API get list
    @GetMapping("/list")
    public String showStaffList(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "gender", required = false) String gender,
                                Model model) {
        List<Staff> staffList;

        if ((keyword != null && !keyword.isBlank()) || (gender != null && !gender.isBlank())) {
            staffList = iStaffService.searchStaff(keyword, gender);
        } else {
            staffList = iStaffService.getAllStaff();
        }

        model.addAttribute("staffList", staffList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);
        return "manageStaff";
        }
    }

