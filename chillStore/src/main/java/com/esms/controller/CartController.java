package com.esms.controller;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Voucher;
import com.esms.service.CartService;
import com.esms.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String viewCart(@RequestParam("customerId") int customerId,
                           @RequestParam(value = "voucher", required = false) String voucherCode,
                           Model model) {
        List<CartItemDTO> cartItems = cartService.getCartItems(customerId);

        List<Voucher> allVouchers = voucherService.getAllVouchers();
        LocalDate today = LocalDate.now();
        List<Voucher> availableVouchers = allVouchers.stream()
                .filter(v -> v.isActive())
                .filter(v -> v.getQuantity_available() > 0)
                .filter(v -> !today.isBefore(v.getStart_date()) && !today.isAfter(v.getEnd_date()))
                .collect(Collectors.toList());

        // Subtotal
        double subtotal = cartItems.stream()
                .mapToDouble(CartItemDTO::getTotalPrice)
                .sum();

        // Voucher
        Voucher selectedVoucher = null;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            selectedVoucher = availableVouchers.stream()
                    .filter(v -> v.getCode().equalsIgnoreCase(voucherCode))
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

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("vouchers", availableVouchers);
        model.addAttribute("selectedVoucher", selectedVoucher);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("total", total);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam int customerId,
                            @RequestParam int productId,
                            @RequestParam(defaultValue = "1") int quantity) {
        cartService.addToCart(customerId, productId, quantity);
        return "redirect:/cart?customerId=" + customerId;
    }



    @PostMapping("/update")
    public String updateCart(@RequestParam int cartId, @RequestParam int quantity) {
        cartService.updateQuantity(cartId, quantity);
        return "redirect:/cart?customerId=1";
    }

    @PostMapping("/delete")
    public String deleteItem(@RequestParam int cartId) {
        cartService.deleteCartItem(cartId);
        return "redirect:/cart?customerId=1";
    }

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String voucherCode, Model model) {

        return "redirect:/cart?customerId=1&voucher=" + voucherCode;
    }
}
