package com.esms.controller;


import com.esms.model.dto.CategoryDto;
import com.esms.model.entity.Category;
import com.esms.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategory(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "error", required = false) String error, Model model, Pageable pa) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        Page<Category> categories = categoryService.searchCategory(keyword, pa);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        return "admin/category/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        CategoryDto categoryDto = new CategoryDto();
        List<Category> parenOptions = categoryService.getAllParentOptions();
        model.addAttribute("categoryDto", categoryDto);
        model.addAttribute("parenOptions", parenOptions);
        return "admin/category/form";
    }

    @PostMapping("/add")
    public String addCategory(
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            Model model, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<Category> parenOptions = categoryService.getAllParentOptions();
            model.addAttribute("parenOptions", parenOptions);
            return "admin/category/form";
        }
        try {
            categoryService.createCategory(categoryDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("parentOptions", categoryService.getAllParentOptions());
            return "admin/category/form";
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") Integer id,
            Model model) {
        Category category = categoryService.getCategoryById(id).orElseThrow(() -> new RuntimeException("Category not fount"));
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        categoryDto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        List<Category> parenOptions = categoryService.getAllParentOptions();
        parenOptions.removeIf(c -> c.getId().equals(id));
        model.addAttribute("categoryDto", categoryDto);
        model.addAttribute("parentOptions", categoryService.getAllParentOptions());
        return "admin/category/form";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            List<Category> parenOptions = categoryService.getAllParentOptions();
            parenOptions.removeIf(c -> c.getId().equals(id));
            model.addAttribute("parenOptions", parenOptions);
            return "admin/category/form";
        }
        try {
            categoryService.updateCategory(id, categoryDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<Category> parenOptions = categoryService.getAllParentOptions();
            parenOptions.removeIf(c -> c.getId().equals(id));
            model.addAttribute("parentOptions", categoryService.getAllParentOptions());
            return "admin/category/form";
        }
        return "redirect:/admin/category";
    }


    @GetMapping("/delete/{id}")
    public String deleteCategory(
            @PathVariable("id") Integer id) {
        try {
            categoryService.deleteCategory(id);
        } catch (Exception e) {
            return "redirect:/admin/category?error=" + e.getMessage();
        }
        return "redirect:/admin/category";
    }

}
