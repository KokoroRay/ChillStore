package com.esms.controller;

import com.esms.model.dto.DiscountDTO;
import com.esms.model.entity.Brand;
import com.esms.model.entity.Category;
import com.esms.model.entity.Product;
import com.esms.service.BrandService;
import com.esms.service.CategoryService;
import com.esms.service.DiscountService;
import com.esms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller cho Discount
 * Xử lý các request HTTP liên quan đến quản lý discount
 */
@Controller
@RequestMapping("/admin/discount")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Display the discount list page with search and filter functionality
     *
     * @param model         Spring MVC model
     * @param search        Search term for code or description
     * @param status        Filter by active status
     * @param applyType     Filter by apply type
     * @param startDate     Filter by start date
     * @param endDate       Filter by end date
     * @param categoryId    Filter by category
     * @param brandId       Filter by brand
     * @param productId     Filter by product
     * @param discountRange Filter by discount percentage range
     * @param page          Page number for pagination (default: 0)
     * @param size          Page size for pagination (default: 3)
     * @return view name
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String listDiscounts(Model model,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "applyType", required = false) String applyType,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                @RequestParam(value = "brandId", required = false) Integer brandId,
                                @RequestParam(value = "productId", required = false) Integer productId,
                                @RequestParam(value = "discountRange", required = false) String discountRange,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "3") int size) {
        try {
            // Get filtered discounts from service
            List<DiscountDTO> allDiscounts = discountService.searchAndFilterDiscounts(
                    search, status, applyType, startDate, endDate,
                    categoryId, brandId, productId, discountRange
            );

            // Calculate statistics from all discounts
            long totalDiscounts = allDiscounts.size();
            long activeDiscounts = allDiscounts.stream().filter(d -> Boolean.TRUE.equals(d.getActive())).count();
            long productDiscounts = allDiscounts.stream().filter(d -> "product".equals(d.getApplyType())).count();
            long brandDiscounts = allDiscounts.stream().filter(d -> "brand".equals(d.getApplyType())).count();
            long categoryDiscounts = allDiscounts.stream().filter(d -> "category".equals(d.getApplyType())).count();

            // Implement pagination
            int totalPages = (int) Math.ceil((double) totalDiscounts / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, allDiscounts.size());

            List<DiscountDTO> paginatedDiscounts = allDiscounts.subList(startIndex, endIndex);

            // Get data for filter dropdowns
            List<Product> products = productService.findAll();
            List<Brand> brands = brandService.getAllBrands();
            List<Category> categories = categoryService.getAllCategory();

            // Add data to model
            model.addAttribute("discounts", paginatedDiscounts);
            model.addAttribute("totalDiscounts", totalDiscounts);
            model.addAttribute("activeDiscounts", activeDiscounts);
            model.addAttribute("productDiscounts", productDiscounts);
            model.addAttribute("brandDiscounts", brandDiscounts);
            model.addAttribute("categoryDiscounts", categoryDiscounts);
            model.addAttribute("products", products);
            model.addAttribute("brands", brands);
            model.addAttribute("categories", categories);
            model.addAttribute("activeMenu", "discount");

            // Pagination attributes
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasNext", page < totalPages - 1);
            model.addAttribute("hasPrevious", page > 0);
            model.addAttribute("startIndex", totalDiscounts > 0 ? startIndex + 1 : 0);
            model.addAttribute("endIndex", endIndex);

            // Search parameters for pagination links
            model.addAttribute("search", search);
            model.addAttribute("status", status);
            model.addAttribute("applyType", applyType);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("brandId", brandId);
            model.addAttribute("productId", productId);
            model.addAttribute("discountRange", discountRange);

            return "admin/discount/list";
        } catch (Exception e) {
            // Log error and redirect with error message
            model.addAttribute("error", "Failed to load discounts: " + e.getMessage());
            return "redirect:/admin/discount/list?error=Failed to load discounts";
        }
    }

    /**
     * Default mapping for discount management
     *
     * @param model Spring MVC model
     * @return redirect to list page
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String defaultDiscountPage(Model model) {
        return "redirect:/admin/discount/list";
    }

    /**
     * Hiển thị trang chi tiết discount
     *
     * @param promoId ID của discount
     * @param model   Model để truyền dữ liệu đến view
     * @return String tên view
     */
    @GetMapping("/{promoId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String viewDiscountDetail(@PathVariable Integer promoId, Model model) {
        // Lấy thông tin chi tiết discount theo ID
        DiscountDTO discount = discountService.getDiscountById(promoId);

        // Thêm thông tin discount vào model
        model.addAttribute("discount", discount);
        model.addAttribute("activeMenu", "discount");

        // Trả về view hiển thị chi tiết discount
        return "admin/discount/detail";
    }

    /**
     * Hiển thị form tạo discount mới
     *
     * @param model Spring MVC model
     * @return view name
     */
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showCreateForm(Model model) {
        try {
            // Tạo DTO mới cho discount
            DiscountDTO discountDto = new DiscountDTO();
            discountDto.setActive(true); // Mặc định là active

            // Lấy danh sách sản phẩm, brands, categories để hiển thị trong form
            List<Product> products = productService.findAll();
            List<Brand> brands = brandService.getAllBrands();
            List<Category> categories = categoryService.getAllCategory();

            // Thêm vào model
            model.addAttribute("discountDto", discountDto);
            model.addAttribute("products", products);
            model.addAttribute("brands", brands);
            model.addAttribute("categories", categories);
            model.addAttribute("activeMenu", "discount");

            return "admin/discount/form";
        } catch (Exception e) {
            return "redirect:/admin/discount/list?error=Failed to load form: " + e.getMessage();
        }
    }

    /**
     * Xử lý lưu discount (tạo mới hoặc cập nhật)
     *
     * @param discountDto        DTO chứa thông tin discount
     * @param productIds         Danh sách ID sản phẩm (nếu applyType là product)
     * @param brandId            ID brand (nếu applyType là brand)
     * @param categoryId         ID category (nếu applyType là category)
     * @param redirectAttributes Redirect attributes
     * @return redirect URL
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String saveDiscount(@ModelAttribute DiscountDTO discountDto,
                               @RequestParam(value = "productIds", required = false) List<Integer> productIds,
                               @RequestParam(value = "brandId", required = false) Integer brandId,
                               @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate dữ liệu cơ bản
            if (discountDto.getCode() == null || discountDto.getCode().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Discount code is required");
                return "redirect:/admin/discount/create";
            }

            if (discountDto.getDiscountPct() == null || discountDto.getDiscountPct().compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Discount percentage must be greater than 0");
                return "redirect:/admin/discount/create";
            }

            if (discountDto.getStartDate() == null || discountDto.getEndDate() == null) {
                redirectAttributes.addFlashAttribute("error", "Start date and end date are required");
                return "redirect:/admin/discount/create";
            }

            if (discountDto.getStartDate().isAfter(discountDto.getEndDate())) {
                redirectAttributes.addFlashAttribute("error", "End date must be after start date");
                return "redirect:/admin/discount/create";
            }

            if (discountDto.getApplyType() == null || discountDto.getApplyType().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Apply type is required");
                return "redirect:/admin/discount/create";
            }

            // Xử lý theo applyType
            if ("product".equals(discountDto.getApplyType())) {
                if (productIds == null || productIds.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Please select at least one product");
                    return "redirect:/admin/discount/create";
                }
                discountDto.setProductIds(productIds);
            } else if ("brand".equals(discountDto.getApplyType())) {
                if (brandId == null) {
                    redirectAttributes.addFlashAttribute("error", "Please select a brand");
                    return "redirect:/admin/discount/create";
                }
                discountDto.setBrandId(brandId);
            } else if ("category".equals(discountDto.getApplyType())) {
                if (categoryId == null) {
                    redirectAttributes.addFlashAttribute("error", "Please select a category");
                    return "redirect:/admin/discount/create";
                }
                discountDto.setCategoryId(categoryId);
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid apply type");
                return "redirect:/admin/discount/create";
            }

            // Thiết lập createdBy nếu là tạo mới (tạm thời set = 1 cho admin)
            if (discountDto.getPromoId() == null) {
                discountDto.setCreatedBy(1); // TODO: Lấy từ session user
            }

            // Thiết lập active mặc định nếu null
            if (discountDto.getActive() == null) {
                discountDto.setActive(true);
            }

            // Lưu discount
            DiscountDTO savedDiscount;
            if (discountDto.getPromoId() == null) {
                // Tạo mới
                savedDiscount = discountService.createDiscount(discountDto);
            } else {
                // Cập nhật
                savedDiscount = discountService.updateDiscount(discountDto.getPromoId(), discountDto);
            }

            // Redirect với thông báo thành công
            String successMessage = discountDto.getPromoId() == null ? "created" : "updated";
            redirectAttributes.addFlashAttribute("success", "Discount " + successMessage + " successfully!");

            return "redirect:/admin/discount/list?success=" + successMessage;

        } catch (IllegalArgumentException e) {
            // Lỗi validation từ service (ví dụ: code đã tồn tại)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/discount/create";
        } catch (Exception e) {
            // Lỗi khác
            redirectAttributes.addFlashAttribute("error", "Failed to save discount: " + e.getMessage());
            return "redirect:/admin/discount/create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa discount
     *
     * @param id    ID của discount cần chỉnh sửa
     * @param model Spring MVC model
     * @return view name
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        try {
            // Lấy thông tin discount
            DiscountDTO discount = discountService.getDiscountById(id);
            if (discount == null) {
                return "redirect:/admin/discount/list?error=Discount not found";
            }

            // Lấy danh sách sản phẩm, brands, categories
            List<Product> products = productService.findAll();
            List<Brand> brands = brandService.getAllBrands();
            List<Category> categories = categoryService.getAllCategory();

            // Thêm vào model
            model.addAttribute("discountDto", discount);
            model.addAttribute("discount", discount); // Để template biết đây là edit mode
            model.addAttribute("products", products);
            model.addAttribute("brands", brands);
            model.addAttribute("categories", categories);
            model.addAttribute("activeMenu", "discount");

            return "admin/discount/form";
        } catch (Exception e) {
            return "redirect:/admin/discount/list?error=Failed to load discount: " + e.getMessage();
        }
    }

    /**
     * Xử lý xóa discount
     *
     * @param id                 ID của discount cần xóa
     * @param redirectAttributes Redirect attributes
     * @return redirect URL
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteDiscount(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Xóa discount
            discountService.deleteDiscount(id);

            // Redirect với thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Discount deleted successfully!");
            return "redirect:/admin/discount/list?success=deleted";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete discount: " + e.getMessage());
            return "redirect:/admin/discount/list?error=Failed to delete discount";
        }
    }

    /**
     * Xử lý kích hoạt/vô hiệu hóa discount
     *
     * @param promoId ID của discount
     * @param active  trạng thái mới
     * @return String redirect URL
     */
    @PostMapping("/{promoId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String toggleDiscountStatus(@PathVariable Integer promoId,
                                       @RequestParam Boolean active) {
        try {
            // Thay đổi trạng thái discount thông qua service
            discountService.toggleDiscountStatus(promoId, active);

            // Redirect về trang danh sách với thông báo thành công
            String message = active ? "activated" : "deactivated";
            return "redirect:/admin/discount?success=" + message;
        } catch (Exception e) {
            // Nếu có lỗi, redirect về trang danh sách với thông báo lỗi
            return "redirect:/admin/discount?error=" + e.getMessage();
        }
    }

    /**
     * API endpoint để lấy danh sách discount dưới dạng JSON
     *
     * @return List<DiscountDTO>
     */
    @GetMapping("/api/list")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public List<DiscountDTO> getDiscountsApi() {
        // Trả về danh sách discount dưới dạng JSON
        return discountService.getAllDiscounts();
    }

    /**
     * API endpoint để lấy discount theo ID dưới dạng JSON
     *
     * @param promoId ID của discount
     * @return DiscountDTO
     */
    @GetMapping("/api/{promoId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public DiscountDTO getDiscountApi(@PathVariable Integer promoId) {
        // Trả về thông tin discount dưới dạng JSON
        return discountService.getDiscountById(promoId);
    }
} 