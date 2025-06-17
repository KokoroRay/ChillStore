package com.esms.controller;

import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.CustomerDto;
import com.esms.model.entity.Customer;
import com.esms.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String search,
            Model model) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<Customer> customerPage = search.isEmpty()
                ? customerService.getAllCustomers(pageable)
                : customerService.searchCustomers(search, pageable);
        System.out.println("📢 Controller đã được gọi!");
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        return "admin/customer/list";
    }

    @GetMapping("/addform")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "admin/customer/addform";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customerDto", convertToDto(customer));
        return "admin/customer/editform";
    }

    @PostMapping("/save")
    public String createCustomer(
            @Valid @ModelAttribute("customerDto") CustomerDto customerDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/customer/addform";
        }
        try {
            customerService.createCustomer(convertToEntity(customerDto));
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
        } catch (EmailAlreadyUsedException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/customer/addform";
        }
        return "redirect:/admin/customer";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(
            @PathVariable Integer id,
            @Valid @ModelAttribute("customerDto") CustomerDto customerDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/customer/editform";
        }
        try {
            Customer customer = convertToEntity(customerDto);
            customer.setCustomerId(id);
            customerService.updateCustomer(id, customerDto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (EmailAlreadyUsedException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/customer/editform";
        }
        return "redirect:/admin/customer";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Khóa tài khoản thành công!");
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customer";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "admin/customer/addform";
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setName(customer.getName());
        dto.setDisplayName(customer.getDisplay_name());
        dto.setEmail(customer.getEmail());
        dto.setPassword(customer.getPassword());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setBirthDate(customer.getBirth_date());
        dto.setCreatedAt(customer.getCreated_at());
        dto.setUpdatedAt(customer.getUpdated_at());
        dto.setLocked(customer.isLocked());
        return dto;
    }

    private Customer convertToEntity(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDisplay_name(dto.getDisplayName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(dto.getPassword());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setBirth_date(dto.getBirthDate());
        customer.setCreated_at(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        customer.setUpdated_at(LocalDateTime.now());
        customer.setLocked(dto.isLocked());
        return customer;
    }
}
