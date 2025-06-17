package com.esms.controller;
import com.esms.model.entity.Staff;
import com.esms.service.IStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manageStaff")
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
        return "admin/manageStaff/add-staff";
    }
    //API add staff
    @PostMapping("/addStaff")
    public String addStaff(@ModelAttribute Staff staff, Model  model) {
        iStaffService.addStaff(staff);
        return "redirect:/manageStaff/listStaff";
    }
    @GetMapping("/update-form")
    public String showUpdateForm(@RequestParam("id") int id, Model model) {
        Staff staff = iStaffService.getOneStaff(id);
        model.addAttribute("staff", staff);
        return "admin/manageStaff/update-staff";
    }


    // API update staff
    @PostMapping("/updateStaff")
    public String updateStaff(@RequestParam("id") int id, @ModelAttribute Staff staff) {
        iStaffService.updateStaff(id, staff);
        return "redirect:/manageStaff/listStaff";
    }
    //API delete staff
    @GetMapping("/deleteStaff/{id}")
    public String deleteStaff(@PathVariable("id") int id) {
        iStaffService.deleteStaff(id);
        return "redirect:/manageStaff/listStaff";    }
    //API get list
    @GetMapping("/listStaff")
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
        return "admin/manageStaff/manageStaff";
        }
    }

