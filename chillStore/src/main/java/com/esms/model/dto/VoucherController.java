package com.esms.model.dto;

import com.esms.model.entity.Voucher;
import com.esms.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public String listVouchers(
            @RequestParam(name = "keyword", required = false)
            String keyword,
            @RequestParam(name = "error", required = false)
            String error, Model model
    ) {
        List<Voucher> vouchers = voucherService.searchVouchers(keyword);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", keyword);
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/vouchers/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        VoucherDto voucherDto = new VoucherDto();
        model.addAttribute("voucher", voucherDto);
        return "admin/vouchers/add";
    }

    @PostMapping("/add")
    public String addVoucher(
            @Valid @ModelAttribute("voucherDto")
            VoucherDto voucherDto, BindingResult bindingResult,
            Model model, Authentication  authentication) {
        if (bindingResult.hasErrors()) {
            return "admin/vouchers/form";
        }
        try {
            String adminEmail = authentication.getName();
            voucherService.createVoucher(voucherDto, adminEmail);
        } catch (Exception e) {
            model.addAttribute("error Message", e.getMessage());
            return "admin/vouchers/form";
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voucher Id:" + id));

        VoucherDto voucherDto = new VoucherDto();
        voucherDto.setCode(voucher.getCode());
        voucherDto.setDescription(voucher.getDescription());
        voucherDto.setDiscount_amount(voucher.getDiscount_amount());
        voucherDto.setDiscount_pct(voucher.getDiscount_pct());
        voucherDto.setMin_order_amount(voucher.getMin_order_amount());
        voucherDto.setQuantity_available(voucherDto.getQuantity_available());
        voucherDto.setStart_date(voucher.getStart_date());
        voucherDto.setEnd_date(voucherDto.getEnd_date());
        voucherDto.setActive(voucher.isActive());
        model.addAttribute("voucherDto", voucher);
        return "admin/vouchers/edit";
    }

    @PostMapping("/edit/{id}")
    public String editVoucher(@PathVariable("id") Integer id,
                              @Valid @ModelAttribute("voucherDto") VoucherDto voucherDto,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/vouchers/form";
        }
        try {
            voucherService.updateVoucher(id, voucherDto);
        } catch (Exception e) {
            model.addAttribute("error Message", e.getMessage());
            return "admin/vouchers/form";
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Integer id) {
        try {
            voucherService.deleteVoucher(id);
        } catch (Exception e) {
            return "redirect:/admin/vouchers?error=" + e.getMessage();
        }
        return "redirect:/admin/vouchers";
    }

}
