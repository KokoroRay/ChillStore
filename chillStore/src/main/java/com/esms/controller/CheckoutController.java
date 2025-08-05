package com.esms.controller;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.Voucher;
import com.esms.repository.ProductRepository;
import com.esms.service.CartService;
import com.esms.service.CheckoutService;
import com.esms.service.CustomerService;
import com.esms.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("")
    public String showCheckout(@RequestParam(value = "voucher", required = false) String voucherCode,
                              @RequestParam(value = "tempAddress", required = false) String tempAddress,
                              Model model,
                              HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
        if (cartItems.isEmpty()) {
            return "redirect:/cart?error=Cart is empty";
        }

        // Lấy thông tin customer
        var customer = customerService.getCustomerById(customerId);
        model.addAttribute("customer", customer);

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

        // Tính shipping cost
        double shippingCost = calculateShippingCost(customer.getAddress(), tempAddress);

        // Total với shipping
        double total = cartService.calculateTotal(
                cartItems,
                selectedVoucher != null && selectedVoucher.getDiscount_pct() != null
                        ? selectedVoucher.getDiscount_pct().doubleValue() / 100.0
                        : null,
                selectedVoucher != null && selectedVoucher.getDiscount_amount() != null
                        ? selectedVoucher.getDiscount_amount().doubleValue()
                        : null
        ) + shippingCost;

        // Đẩy dữ liệu ra view
        model.addAttribute("customerId", customerId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("vouchers", availableVouchers);
        model.addAttribute("selectedVoucher", selectedVoucher);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("total", total);
        model.addAttribute("tempAddress", tempAddress);

        return "checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(@RequestParam String customerName,
                             @RequestParam String customerPhone,
                             @RequestParam(required = false) String customerEmail,
                             @RequestParam String deliveryAddress,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String orderNotes,
                             @RequestParam(value = "voucherCode", required = false) String voucherCode,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        try {
            // Lấy thông tin customer để có email mặc định
            var customer = customerService.getCustomerById(customerId);
            String finalEmail = customerEmail != null && !customerEmail.trim().isEmpty() 
                ? customerEmail.trim() 
                : customer.getEmail();

            System.out.println("Creating order for customer: " + customerId);
            System.out.println("Customer name: " + customerName);
            System.out.println("Customer phone: " + customerPhone);
            System.out.println("Customer email: " + finalEmail);
            System.out.println("Delivery address: " + deliveryAddress);
            System.out.println("Payment method: " + paymentMethod);
            System.out.println("Voucher code: " + voucherCode);
            
            // Tạo đơn hàng
            Integer orderId = checkoutService.createOrder(
                    customerId,
                    customerName,
                    customerPhone,
                    finalEmail,
                    deliveryAddress,
                    paymentMethod,
                    orderNotes,
                    voucherCode
            );

            System.out.println("Order created successfully with ID: " + orderId);

            // Xóa giỏ hàng sau khi đặt hàng thành công
            cartService.clearCart(customerId);

            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order ID: " + orderId);
            
            if ("VNpay".equals(paymentMethod)) {
                // Redirect to payment gateway
                return "redirect:/payment/vnpay/" + orderId;
            } else {
                // Redirect to order confirmation
                return "redirect:/customer/order-confirmation/" + orderId;
            }

        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String voucherCode,
                              @RequestParam(value = "tempAddress", required = false) String tempAddress,
                              HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        String redirectUrl = "redirect:/checkout?voucher=" + voucherCode;
        if (tempAddress != null && !tempAddress.trim().isEmpty()) {
            redirectUrl += "&tempAddress=" + tempAddress;
        }
        return redirectUrl;
    }

    @PostMapping("/apply-voucher-ajax")
    @ResponseBody
    public ResponseEntity<?> applyVoucherAjax(@RequestParam String voucherCode,
                                             @RequestParam(value = "tempAddress", required = false) String tempAddress,
                                             HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return ResponseEntity.badRequest().body("Not logged in");
        }

        try {
            List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            // Tính subtotal
            double subtotal = cartItems.stream()
                    .mapToDouble(CartItemDTO::getTotalPrice)
                    .sum();

            // Lấy voucher
            Voucher voucher = null;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                voucher = voucherService.getVoucherByCode(voucherCode.trim());
                if (voucher == null || !voucher.isActive() || 
                    subtotal < (voucher.getMin_order_amount() != null ? voucher.getMin_order_amount().doubleValue() : 0.0)) {
                    return ResponseEntity.badRequest().body("Invalid or expired voucher");
                }
            }

            // Tính discount
            double discountAmount = 0.0;
            if (voucher != null) {
                if (voucher.getDiscount_pct() != null) {
                    discountAmount = subtotal * (voucher.getDiscount_pct().doubleValue() / 100.0);
                } else if (voucher.getDiscount_amount() != null) {
                    discountAmount = voucher.getDiscount_amount().doubleValue();
                }
            }

            // Tính shipping cost
            var customer = customerService.getCustomerById(customerId);
            double shippingCost = calculateShippingCost(customer.getAddress(), tempAddress);

            // Tính total
            double total = subtotal - discountAmount + shippingCost;

            return ResponseEntity.ok(Map.of(
                "subtotal", subtotal,
                "discountAmount", discountAmount,
                "shippingCost", shippingCost,
                "total", total,
                "voucherCode", voucherCode,
                "voucherDescription", voucher != null ? voucher.getDescription() : "",
                "formattedSubtotal", String.format("%,.0f VND", subtotal),
                "formattedDiscount", String.format("%,.0f VND", discountAmount),
                "formattedShipping", String.format("%,.0f VND", shippingCost),
                "formattedTotal", String.format("%,.0f VND", total)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error applying voucher: " + e.getMessage());
        }
    }

    @PostMapping("/update-address")
    @ResponseBody
    public ResponseEntity<?> updateAddress(@RequestParam String address,
                                          @RequestParam(value = "tempAddress", required = false) String tempAddress,
                                          HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return ResponseEntity.badRequest().body("Not logged in");
        }

        try {
            var customer = customerService.getCustomerById(customerId);
            double shippingCost = calculateShippingCost(address, tempAddress);
            
            return ResponseEntity.ok(Map.of(
                "shippingCost", shippingCost,
                "formattedShippingCost", String.format("%,.0f VND", shippingCost)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating shipping cost");
        }
    }

    private double calculateShippingCost(String defaultAddress, String tempAddress) {
        String addressToUse = tempAddress != null && !tempAddress.trim().isEmpty() ? tempAddress : defaultAddress;
        
        if (addressToUse == null || addressToUse.trim().isEmpty()) {
            return 20000; // Default shipping cost
        }

        String addressLower = addressToUse.toLowerCase();

        // Free shipping for Cần Thơ
        if (addressLower.contains("cần thơ") || addressLower.contains("can tho")) {
            return 0;
        }

        // List of northern provinces
        String[] northernProvinces = {
            "hà nội", "hanoi", "hải phòng", "haiphong", "bắc ninh", "bắc giang", "lào cai",
            "lao cai", "điện biên", "dien bien", "hòa bình", "hoa binh", "lai châu", "lai chau",
            "sơn la", "son la", "hà giang", "ha giang", "cao bằng", "cao bang", "bắc kạn", "bac kan",
            "lạng sơn", "lang son", "tuyên quang", "tuyen quang", "thái nguyên", "thai nguyen",
            "phú thọ", "phu tho", "vĩnh phúc", "vinh phuc", "quảng ninh", "quang ninh",
            "hải dương", "hai duong", "hưng yên", "hung yen", "thái bình", "thai binh",
            "hà nam", "ha nam", "nam định", "nam dinh", "ninh bình", "ninh binh", "thanh hóa", "thanh hoa"
        };

        // Check if province is in northern list
        for (String province : northernProvinces) {
            if (addressLower.contains(province)) {
                return 40000; // Northern provinces
            }
        }

        return 20000; // Other provinces
    }

    // Test endpoint để kiểm tra
    @GetMapping("/test")
    @ResponseBody
    public String testCheckout(HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "Not logged in";
        }

        try {
            List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
            var customer = customerService.getCustomerById(customerId);
            
            return String.format("Customer: %s, Cart items: %d", 
                customer.getName(), cartItems.size());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Enhanced test endpoint to debug cart items
    @GetMapping("/test-cart")
    @ResponseBody
    public Map<String, Object> testCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        response.put("customerId", customerId);
        
        if (customerId != null) {
            try {
                List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
                response.put("cartItems", cartItems);
                response.put("cartSize", cartItems.size());
                
                // Debug each cart item
                List<Map<String, Object>> debugItems = new ArrayList<>();
                for (CartItemDTO item : cartItems) {
                    Map<String, Object> debugItem = new HashMap<>();
                    debugItem.put("cartItemId", item.getCartItemId());
                    debugItem.put("productId", item.getProductId());
                    debugItem.put("productName", item.getProductName());
                    debugItem.put("quantity", item.getQuantity());
                    debugItem.put("price", item.getPrice());
                    debugItem.put("totalPrice", item.getTotalPrice());
                    debugItems.add(debugItem);
                }
                response.put("debugItems", debugItems);
                
                // Also check if products exist in database
                List<Map<String, Object>> productChecks = new ArrayList<>();
                for (CartItemDTO item : cartItems) {
                    Map<String, Object> productCheck = new HashMap<>();
                    productCheck.put("productId", item.getProductId());
                    productCheck.put("productExists", productRepository.findById(item.getProductId()).isPresent());
                    productChecks.add(productCheck);
                }
                response.put("productChecks", productChecks);
                
            } catch (Exception e) {
                response.put("error", e.getMessage());
                e.printStackTrace();
            }
        } else {
            response.put("error", "No customer ID in session");
        }
        
        return response;
    }
} 