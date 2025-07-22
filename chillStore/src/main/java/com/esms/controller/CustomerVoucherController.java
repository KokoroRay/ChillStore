package com.esms.controller;


import com.esms.model.entity.Customer;
import com.esms.model.entity.Voucher;
import com.esms.service.CustomerService;
import com.esms.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/voucher")
public class CustomerVoucherController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CustomerController customerController;
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String ListCustomerVoucher(Model model, Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerService.getCustomerByEmail(email);

        List<Voucher> voucher  = voucherService.getVoucherByCustomerId(customer.getCustomerId());
        model.addAttribute("vouchers", voucher);
        return "customer/vouchers/list";

    }

    @PostMapping("/apply")
    public String applySpecialVoucher(@RequestParam("code") String code, Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Customer customer = customerService.getCustomerByEmail(email);
        try {
            voucherService.applySpecialVoucher(customer.getCustomerId(), code);
            redirectAttributes.addFlashAttribute("success", "Voucher has been applied successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return  "redirect:/customer/voucher";
    }
}
