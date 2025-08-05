package com.esms.controller;

import com.esms.exception.EmailAlreadyUsedException;
import com.esms.exception.UserNotFoundException;
import com.esms.model.dto.CustomerDTO;
import com.esms.model.entity.Customer;
import com.esms.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import com.esms.repository.OrderRepository;
import com.esms.model.entity.Order;

@Controller
@RequestMapping({"/admin/customer", "/staff/customer"})
public class CustomerController {

    private final CustomerService customerService;
    private final OrderRepository orderRepository;

    public CustomerController(CustomerService customerService, OrderRepository orderRepository) {
        this.customerService = customerService;
        this.orderRepository = orderRepository;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(value = "error", required = false) String error,
            Model model, HttpServletRequest request) {
        if (error != null) {
            model.addAttribute("error", error);
        }

        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<Customer> customerPage = customerService.findWithFilters(search, locked, pageable);

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("locked", locked);
        model.addAttribute("currentPage", customerPage.getNumber());
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalItems", customerPage.getTotalElements());
        model.addAttribute("pageSize", customerPage.getSize());

        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "staff/customer/list";
        } else {
            return "admin/customer/list";
        }
    }

    @GetMapping("/addform")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("customerDto", new CustomerDTO());
        return "admin/customer/addform";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.equals("null") || id.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid customer ID");
                return "redirect:/admin/customer";
            }
            Integer customerId = Integer.parseInt(id);
            Customer customer = customerService.getCustomerById(customerId);
            model.addAttribute("customerDto", convertToDto(customer));
            return "admin/customer/editform";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid customer ID format");
            return "redirect:/admin/customer";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Customer not found");
            return "redirect:/admin/customer";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String createCustomer(
            @Valid @ModelAttribute("customerDto") CustomerDTO customerDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/customer/addform";
        }
        try {
            customerService.createCustomer(convertToEntity(customerDto));
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
        } catch (EmailAlreadyUsedException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/customer/addform";
        }
        return "redirect:/admin/customer";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String updateCustomer(
            @PathVariable String id,
            @Valid @ModelAttribute("customerDto") CustomerDTO customerDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.equals("null") || id.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid customer ID");
                return "redirect:/admin/customer";
            }
            Integer customerId = Integer.parseInt(id);
            
            if (result.hasErrors()) {
                return "admin/customer/editform";
            }
            
            Customer customer = convertToEntity(customerDto);
            customer.setCustomerId(customerId);
            customerService.updateCustomer(customerId, customerDto);
            redirectAttributes.addFlashAttribute("success", "Update successful!");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid customer ID format");
            return "redirect:/admin/customer";
        } catch (EmailAlreadyUsedException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/customer/editform";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Customer not found");
            return "redirect:/admin/customer";
        }
        return "redirect:/admin/customer";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteCustomer(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.equals("null") || id.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid customer ID");
                return "redirect:/admin/customer";
            }
            Integer customerId = Integer.parseInt(id);
            customerService.deleteCustomer(customerId);
            redirectAttributes.addFlashAttribute("success", "Account locked successfully!");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid customer ID format");
            return "redirect:/admin/customer";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customer";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("customerDto", new CustomerDTO());
        return "admin/customer/addform";
    }

    @GetMapping("/suggest")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public List<String> suggestCustomer(@RequestParam("keyword") String keyword,
                                        @RequestParam("type") String type) {
        return customerService.suggestCustomerByType(keyword, type, 5);
    }

    @GetMapping("/unlock/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String unlockCustomer(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.equals("null") || id.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid customer ID");
                return "redirect:/admin/customer";
            }
            Integer customerId = Integer.parseInt(id);
            Customer customer = customerService.getCustomerById(customerId);
            customer.setLocked(false);
            customerService.updateCustomer(customerId, convertToDto(customer));
            redirectAttributes.addFlashAttribute("success", "Account unlocked successfully!");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid customer ID format");
            return "redirect:/admin/customer";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customer";
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String viewCustomerOrders(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (id == null || id.equals("null") || id.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid customer ID");
                return "redirect:/admin/customer";
            }
            Integer customerId = Integer.parseInt(id);
            List<com.esms.model.entity.Order> orders = orderRepository.findByCustomerCustomerId(customerId);
            model.addAttribute("orders", orders);
            model.addAttribute("customer", customerService.getCustomerById(customerId));
            return "staff/customer/order-history";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid customer ID format");
            return "redirect:/admin/customer";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Customer not found");
            return "redirect:/admin/customer";
        }
    }

    private CustomerDTO convertToDto(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
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
        dto.setAvatarUrl(customer.getAvatar_url());
        return dto;
    }

    private Customer convertToEntity(CustomerDTO dto) {
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
        customer.setAvatar_url(dto.getAvatarUrl());
        return customer;
    }
}