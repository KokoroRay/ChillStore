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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

        //if (customerId == null) {
      //      return "redirect:/login"; // hoặc hiển thị trang thông báo chưa đăng nhập
      //  }

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
                            HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        cartService.addToCart(customerId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam int cartId,
                             @RequestParam int quantity,
                             HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        cartService.updateQuantity(cartId, quantity);
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
    public String applyVoucher(@RequestParam String voucherCode,
                               HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        return "redirect:/cart?voucher=" + voucherCode;
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
}
