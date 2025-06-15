package com.esms.controller;

import com.esms.model.entity.Product;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;

        if (keyword != null && !keyword.isEmpty()) {
            List<Product> searchResults = productService.searchProducts(keyword);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), searchResults.size());
            products = new PageImpl<>(
                searchResults.subList(start, end),
                pageable,
                searchResults.size()
            );
        } else if (status != null) {
            List<Product> filteredResults = productService.filterByStatus(status);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredResults.size());
            products = new PageImpl<>(
                filteredResults.subList(start, end),
                pageable,
                filteredResults.size()
            );
        } else {
            products = productService.getAllProducts(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        
        return "admin/ManageProduct/Product";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/ManageProduct/ProductDetail";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/ManageProduct/ProductForm";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, 
                           @RequestParam("image") MultipartFile image,
                           RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh ở đây
            // product.setImageUrl(...);
            
            productService.addProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/ManageProduct/ProductForm";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Integer id,
                              @ModelAttribute Product product,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh ở đây nếu có
            // if (image != null && !image.isEmpty()) {
            //     product.setImageUrl(...);
            // }
            
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    @GetMapping("/filter/status")
    public List<Product> filterByStatus(@RequestParam boolean status) {
        return productService.filterByStatus(status);
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
    }
}
