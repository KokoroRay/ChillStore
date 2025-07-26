package com.esms.controller;

import com.esms.model.dto.ProductDTO;
import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.repository.BrandRepository;
import com.esms.repository.CategoryRepository;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
public class SearchController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @GetMapping("/searchProduct")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<ProductDTO> results;

        Optional<Category> categoryOpt = categoryRepository.findByNameIgnoreCase(keyword);
        Optional<Brand> brandOpt = brandRepository.findByNameIgnoreCase(keyword);

        if (categoryOpt.isPresent() && brandOpt.isPresent()) {
            // Nếu từ khóa trùng tên cả category và brand
            results = productService.searchByCategoryAndBrand(
                    categoryOpt.get().getId().longValue(),
                    brandOpt.get().getId().longValue()
            );
        } else if (categoryOpt.isPresent()) {
            // Nếu từ khóa là danh mục
            results = productService.getProductsByCategoryId(categoryOpt.get().getId().longValue());
        } else if (brandOpt.isPresent()) {
            // Nếu từ khóa là hãng
            results = productService.getProductsByBrandId(brandOpt.get().getId().longValue());
        } else {
            // Nếu là từ khóa sản phẩm gần đúng
            results = productService.searchProductsByKeyword(keyword);
        }

        model.addAttribute("searchResults", results);
        model.addAttribute("keyword", keyword);
        return "searchList";
    }


    // Gợi ý sản phẩm khi người dùng đang gõ
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam("term") String term) {
        return productService.suggestProductNames(term);
    }
}
