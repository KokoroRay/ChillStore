package com.esms.controller;

import com.esms.model.dto.VoucherDTO;
import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Voucher;
import com.esms.repository.BrandRepository;
import com.esms.repository.CategoryRepository;
import com.esms.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping({"/admin/vouchers", "/staff/vouchers"})
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String listVouchers(
            @RequestParam(name = "keyword", required = false)
            String keyword,
            @RequestParam(name = "error", required = false)
            String error, Model model, HttpServletRequest request
    ) {
        List<Voucher> vouchers = voucherService.searchVouchers(keyword);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", keyword);
        if (error != null) {
            model.addAttribute("error", error);
        }
        String requestUrl = request.getRequestURI();
        if (requestUrl.startsWith("/staff")) {
            return "staff/vouchers/list";
        } else {
            return "admin/vouchers/list";
        }
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showAddForm(Model model) {
        VoucherDTO voucherDto = new VoucherDTO();
        model.addAttribute("voucherDto", voucherDto);
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("allBrands", brandRepository.findAll());
        model.addAttribute("selectedCategories", new ArrayList<Category>());
        model.addAttribute("selectedBrands", new ArrayList<Brand>());
        model.addAttribute("activeMenu", "vouchers");
        model.addAttribute("currentSection", "voucher");
        model.addAttribute("keyword", "");
        return "admin/vouchers/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String addVoucher(
            @Valid @ModelAttribute("voucherDto")
            VoucherDTO voucherDto, BindingResult bindingResult,
            Model model, Authentication  authentication) {
        if (bindingResult.hasErrors()) {
            List<Category> selCats = voucherDto.getCategoryIds() != null ? categoryRepository.findAllById(voucherDto.getCategoryIds()) : List.of();
            List<Brand> selBrands = voucherDto.getBrandIds() != null  ? brandRepository.findAllById(voucherDto.getBrandIds()) : List.of();
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("allBrands", brandRepository.findAll());
            model.addAttribute("selectedCategories", selCats);
            model.addAttribute("selectedBrands",selBrands);
            model.addAttribute("voucherDto", voucherDto);
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("currentSection", "voucher");
            model.addAttribute("keyword", "");
            return "admin/vouchers/form";
        }
        try {
            String adminEmail = authentication.getName();
            voucherService.createVoucher(voucherDto, adminEmail);
        } catch (Exception e) {
            List<Category> selCats = voucherDto.getCategoryIds() != null ? categoryRepository.findAllById(voucherDto.getCategoryIds()) : List.of();
            List<Brand> selBrands = voucherDto.getBrandIds() != null  ? brandRepository.findAllById(voucherDto.getBrandIds()) : List.of();
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("allBrands", brandRepository.findAll());
            model.addAttribute("selectedCategories", selCats);
            model.addAttribute("selectedBrands",selBrands);
            model.addAttribute("voucherDto", voucherDto);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("currentSection", "voucher");
            model.addAttribute("keyword", "");
            return "admin/vouchers/form";
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voucher Id:" + id));

        VoucherDTO voucherDto = new VoucherDTO();
        voucherDto.setVoucher_id(voucher.getVoucher_id());
        voucherDto.setCode(voucher.getCode());
        voucherDto.setDescription(voucher.getDescription());
        voucherDto.setDiscount_amount(voucher.getDiscount_amount());
        voucherDto.setDiscount_pct(voucher.getDiscount_pct());
        voucherDto.setMin_order_amount(voucher.getMin_order_amount());
        voucherDto.setQuantity_available(voucher.getQuantity_available());
        voucherDto.setStart_date(voucher.getStart_date());
        voucherDto.setEnd_date(voucher.getEnd_date());
        voucherDto.setActive(voucher.isActive());
        List<Category> selCats = voucher.getCategories() != null ? new ArrayList<>(voucher.getCategories()) : List.of();
        List<Brand> selBrands = voucher.getBrands() != null ? new ArrayList<>(voucher.getBrands()) : List.of();

        voucherDto.setCategoryIds(selCats.stream().map(Category::getId).toList());
        voucherDto.setBrandIds(selBrands.stream().map(Brand::getId).toList());

        model.addAttribute("voucherDto", voucherDto);
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("allBrands", brandRepository.findAll());
        model.addAttribute("selectedCategories", selCats);
        model.addAttribute("selectedBrands", selBrands);
        model.addAttribute("activeMenu", "vouchers");
        model.addAttribute("currentSection", "voucher");
        model.addAttribute("keyword", "");
        return "admin/vouchers/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editVoucher(@PathVariable("id") Integer id,
                              @Valid @ModelAttribute("voucherDto") VoucherDTO voucherDto,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<Category> selCats = voucherDto.getCategoryIds() != null ? categoryRepository.findAllById(voucherDto.getCategoryIds()) : List.of();
            List<Brand> selBrands = voucherDto.getBrandIds() != null  ? brandRepository.findAllById(voucherDto.getBrandIds()) : List.of();
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("allBrands", brandRepository.findAll());
            model.addAttribute("selectedCategories", selCats);
            model.addAttribute("selectedBrands", selBrands);
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("currentSection", "voucher");
            model.addAttribute("keyword", "");
            return "admin/vouchers/form";
        }
        try {
            voucherService.updateVoucher(id, voucherDto);
        } catch (Exception e) {
            List<Category> selCats = voucherDto.getCategoryIds() != null ? categoryRepository.findAllById(voucherDto.getCategoryIds()) : List.of();
            List<Brand> selBrands = voucherDto.getBrandIds() != null  ? brandRepository.findAllById(voucherDto.getBrandIds()) : List.of();
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("allBrands", brandRepository.findAll());
            model.addAttribute("selectedCategories", selCats);
            model.addAttribute("selectedBrands", selBrands);
            model.addAttribute("voucherDto", voucherDto);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activeMenu", "vouchers");
            model.addAttribute("currentSection", "voucher");
            model.addAttribute("keyword", "");
            return "admin/vouchers/form";
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteVoucher(@PathVariable("id") Integer id) {
        try {
            voucherService.deleteVoucher(id);
        } catch (Exception e) {
            return "redirect:/admin/vouchers?error=" + e.getMessage();
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/categories/search")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> searchCategories(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "1") int page) {
            int pageSize = 50;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("name").ascending());
        Page<Category> pg;
        if (q == null || q.isBlank()) {
            pg = categoryRepository.findAll(pageable);
        } else {
            pg = categoryRepository.findByNameContainingIgnoreCase(q, pageable);
        }
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> items = pg.getContent().stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("text", c.getName());
                    return m;
                })
                .collect(Collectors.toList());
        result.put("results", items);
        result.put("pagination", Map.of("more", pg.hasNext()));
        return result;
    }

    @GetMapping("/brands/search")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> searchBrands(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        int pageSize = 50;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("name").ascending());
        Page<Brand> pageResult;
        if (q == null || q.isBlank()) {
            pageResult = brandRepository.findAll(pageable);
        } else {
            pageResult = brandRepository.findByNameContainingIgnoreCase(q, pageable);
        }
        List<Map<String, Object>> items = pageResult.getContent().stream()
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    m.put("text", b.getName());
                    return m;
                })
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("results", items);
        result.put("pagination", Map.of("more", pageResult.hasNext()));
        return result;
    }

    private boolean isEmpty(Iterable<?> it) {
        List<?> list = new ArrayList<>();
        if (it != null) return true;
        return !it.iterator().hasNext();
    }

    private <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        if (it != null) {
            it.forEach(list::add);
        }
        return list;
    }

}
