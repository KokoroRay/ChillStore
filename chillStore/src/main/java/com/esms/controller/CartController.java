package com.esms.controller;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Voucher;
import com.esms.service.CartService;
import com.esms.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private VoucherService voucherService;

    @GetMapping("")
    public String viewCart(@RequestParam(value = "voucher", required = false) String voucherCode,
                           Model model,
                           HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");

        if (customerId == null) {
          return "redirect:/login";
        }

        List<CartItemDTO> cartItems = cartService.getCartItems(customerId);

        // Lấy voucher còn hiệu lực
        List<Voucher> allVouchers = voucherService.getAllVouchers();
        LocalDate today = LocalDate.now();
        List<Voucher> availableVouchers = allVouchers.stream()
                .filter(Voucher::isActive)
                .filter(v -> v.getQuantity_available() > 0)
                .filter(v -> !today.isBefore(v.getStart_date()) && !today.isAfter(v.getEnd_date()))
                .collect(Collectors.toList());

        // Subtotal
        double subtotal = cartItems.stream()
                .mapToDouble(CartItemDTO::getTotalPrice)
                .sum();

        // Xử lý voucher được chọn
        Voucher selectedVoucher = null;
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            selectedVoucher = availableVouchers.stream()
                    .filter(v -> v.getCode().equalsIgnoreCase(voucherCode.trim()))
                    .filter(v -> subtotal >= (v.getMin_order_amount() != null ? v.getMin_order_amount().doubleValue() : 0.0))
                    .findFirst()
                    .orElse(null);
        }

        // Total
        double total = cartService.calculateTotal(
                cartItems,
                selectedVoucher != null && selectedVoucher.getDiscount_pct() != null
                        ? selectedVoucher.getDiscount_pct().doubleValue()
                        : null,
                selectedVoucher != null && selectedVoucher.getDiscount_amount() != null
                        ? selectedVoucher.getDiscount_amount().doubleValue()
                        : null
        );

        // Đẩy dữ liệu ra view
        model.addAttribute("customerId", customerId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("vouchers", availableVouchers);
        model.addAttribute("selectedVoucher", selectedVoucher);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", total);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam int productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }
        try {
            cartService.addToCart(customerId, productId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/cart";
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam int cartId,
                             @RequestParam int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }
        try {
            cartService.updateQuantity(cartId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/cart";
        }
        return "redirect:/cart";
    }

    @PostMapping("/delete")
    public String deleteItem(@RequestParam int cartId,
                             HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        cartService.deleteCartItem(cartId);
        return "redirect:/cart";
    }

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam(required = false) String voucherCode,
                               @RequestParam(required = false) String manualVoucherCode,
                               HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        // Ưu tiên voucher được chọn từ dropdown, nếu không có thì dùng voucher nhập thủ công
        String finalVoucherCode = voucherCode;
        if ((finalVoucherCode == null || finalVoucherCode.trim().isEmpty()) && 
            manualVoucherCode != null && !manualVoucherCode.trim().isEmpty()) {
            finalVoucherCode = manualVoucherCode.trim();
        }

        return "redirect:/cart?voucher=" + finalVoucherCode;
    }

    @PostMapping("/checkout")
    public String proceedToCheckout(@RequestParam(value = "voucher", required = false) String voucherCode,
                                   HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        String redirectUrl = "/checkout";
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            redirectUrl += "?voucher=" + voucherCode;
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        
        if (customerId != null) {
            try {
                List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
                int totalItems = cartItems.stream().mapToInt(CartItemDTO::getQuantity).sum();
                response.put("count", totalItems);
                response.put("success", true);
            } catch (Exception e) {
                response.put("count", 0);
                response.put("success", false);
                response.put("error", e.getMessage());
            }
        } else {
            response.put("count", 0);
            response.put("success", false);
            response.put("error", "Not logged in");
        }
        
        return response;
    }

    @PostMapping("/update-ajax")
    @ResponseBody
    public Map<String, Object> updateCartAjax(@RequestParam int cartId,
                                             @RequestParam int quantity,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        
        if (customerId == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        try {
            cartService.updateQuantity(cartId, quantity);
            
            // Recalculate totals
            List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
            double subtotal = cartItems.stream()
                    .mapToDouble(CartItemDTO::getTotalPrice)
                    .sum();
            
            // Find the updated item to get its new total
            CartItemDTO updatedItem = cartItems.stream()
                    .filter(item -> item.getCartItemId() == cartId)
                    .findFirst()
                    .orElse(null);
            
            response.put("success", true);
            response.put("subtotal", String.format("%,.0f đ", subtotal));
            response.put("total", String.format("%,.0f đ", subtotal));
            if (updatedItem != null) {
                response.put("formattedTotal", String.format("%,.0f đ", updatedItem.getTotalPrice()));
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/delete-ajax")
    @ResponseBody
    public Map<String, Object> deleteCartAjax(@RequestParam int cartId,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        
        if (customerId == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        try {
            cartService.deleteCartItem(cartId);
            
            // Recalculate totals
            List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
            double subtotal = cartItems.stream()
                    .mapToDouble(CartItemDTO::getTotalPrice)
                    .sum();
            
            response.put("success", true);
            response.put("subtotal", String.format("%,.0f đ", subtotal));
            response.put("total", String.format("%,.0f đ", subtotal));
            response.put("cartEmpty", cartItems.isEmpty());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
