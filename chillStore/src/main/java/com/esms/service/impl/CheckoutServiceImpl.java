package com.esms.service.impl;

import com.esms.model.dto.CartItemDTO;
import com.esms.model.entity.*;
import com.esms.repository.*;
import com.esms.service.CartService;
import com.esms.service.CheckoutService;
import com.esms.service.VoucherService;
import com.esms.service.WarehouseService;
import com.esms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Integer createOrder(Integer customerId, 
                             String customerName, 
                             String customerPhone, 
                             String customerEmail,
                             String deliveryAddress, 
                             String paymentMethod, 
                             String orderNotes, 
                             String voucherCode) {
        
        // Lấy thông tin customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Cập nhật email nếu khác với email hiện tại
        if (!customer.getEmail().equals(customerEmail)) {
            customer.setEmail(customerEmail);
            customerRepository.save(customer);
        }

        // Lấy items từ giỏ hàng
        List<CartItemDTO> cartItems = cartService.getCartItems(customerId);
        System.out.println("Retrieved " + cartItems.size() + " cart items for customer " + customerId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Debug: Print cart items
        for (CartItemDTO item : cartItems) {
            System.out.println("Cart Item - ID: " + item.getCartItemId() + 
                             ", Product ID: " + item.getProductId() + 
                             ", Name: " + item.getProductName() + 
                             ", Quantity: " + item.getQuantity());
        }

        // Tính toán tổng tiền
        double subtotal = cartItems.stream()
                .mapToDouble(CartItemDTO::getTotalPrice)
                .sum();

        // Xử lý voucher
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            voucher = voucherService.getVoucherByCode(voucherCode);
            if (voucher != null && voucher.isActive() && 
                subtotal >= (voucher.getMin_order_amount() != null ? voucher.getMin_order_amount().doubleValue() : 0.0)) {
                
                if (voucher.getDiscount_pct() != null) {
                    discountAmount = BigDecimal.valueOf(subtotal * voucher.getDiscount_pct().doubleValue() / 100);
                } else if (voucher.getDiscount_amount() != null) {
                    discountAmount = voucher.getDiscount_amount();
                }
            } else {
                throw new RuntimeException("Invalid or expired voucher code");
            }
        }

        // Tính shipping cost dựa trên địa chỉ
        double shippingCost = calculateShippingCost(deliveryAddress);
        
        // Tính tổng tiền cuối cùng (bao gồm shipping)
        BigDecimal totalAmount = BigDecimal.valueOf(subtotal).subtract(discountAmount).add(BigDecimal.valueOf(shippingCost));
        
        // Kiểm tra giới hạn tổng tiền (decimal(15,2) = tối đa 9,999,999,999,999.99)
        BigDecimal maxAmount = new BigDecimal("9999999999999.99");
        if (totalAmount.compareTo(maxAmount) > 0) {
            throw new RuntimeException("Tổng tiền đơn hàng vượt quá giới hạn cho phép (9,999,999,999,999.99 VND). Vui lòng giảm số lượng sản phẩm.");
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setCustomer(customer);
        order.setVoucher(voucher);
        order.setDiscountAmount(discountAmount);
        order.setOrderDate(new Date());
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");
        order.setPaymentMethod(paymentMethod);
        order.setVoucherAcquisition(false); // Ensure no null assignment

        // Lưu đơn hàng
        System.out.println("Saving order to database...");
        System.out.println("Order before save - Customer: " + order.getCustomer().getCustomerId() + 
                          ", Total: " + order.getTotalAmount() + 
                          ", Status: " + order.getStatus());
        
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved with ID: " + savedOrder.getOrderId());
        System.out.println("Saved order customer ID: " + savedOrder.getCustomer().getCustomerId());
        
        // Verify the order was saved properly
        if (savedOrder.getOrderId() == null) {
            throw new RuntimeException("Order was not saved properly - orderId is null");
        }
        
        // Flush to ensure order is persisted
        orderRepository.flush();

        // Tạo order items
        System.out.println("Creating order items...");
        for (CartItemDTO cartItem : cartItems) {
            System.out.println("Processing cart item - CartItemID: " + cartItem.getCartItemId() + 
                             ", ProductID: " + cartItem.getProductId() + 
                             ", Name: " + cartItem.getProductName() + 
                             ", Quantity: " + cartItem.getQuantity());
            
            if (cartItem.getProductId() <= 0) {
                throw new RuntimeException("Invalid product ID: " + cartItem.getProductId() + " for cart item: " + cartItem.getCartItemId());
            }
            
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            // Kiểm tra stock
            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Tạo warehouse transaction (export) - Service này sẽ tự động cập nhật stock
            warehouseService.exportProduct(product.getProductId(), cartItem.getQuantity(), 
                "Order export - Order ID: " + savedOrder.getOrderId());

            // Tạo order item với embedded ID
            OrderItem orderItem = new OrderItem();
            OrderItemId orderItemId = new OrderItemId(savedOrder.getOrderId(), product.getProductId());
            orderItem.setId(orderItemId);
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceEach(BigDecimal.valueOf(cartItem.getPrice()));

            System.out.println("Creating order item:");
            System.out.println("  - Order ID: " + savedOrder.getOrderId());
            System.out.println("  - Product ID: " + product.getProductId());
            System.out.println("  - Quantity: " + cartItem.getQuantity());
            System.out.println("  - Price: " + cartItem.getPrice());
            System.out.println("  - OrderItem.order is null: " + (orderItem.getOrder() == null));
            System.out.println("  - OrderItem.product is null: " + (orderItem.getProduct() == null));
            
            orderItemRepository.save(orderItem);
            System.out.println("Order item saved successfully");
        }

        // Cập nhật số lượng voucher nếu có sử dụng
        if (voucher != null && voucher.getQuantity_available() > 0) {
            voucher.setQuantity_available(voucher.getQuantity_available() - 1);
            voucherService.updateVoucher(voucher);
        }

        // Gửi email xác nhận đơn hàng
        sendOrderConfirmationEmail(customer, savedOrder, cartItems, totalAmount);

        return savedOrder.getOrderId();
    }
    


    private double calculateShippingCost(String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            return 20000; // Default shipping cost
        }

        String addressLower = deliveryAddress.toLowerCase();

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

    private void sendOrderConfirmationEmail(Customer customer, Order order, List<CartItemDTO> cartItems, BigDecimal totalAmount) {
        try {
            String subject = "Order Confirmation - ChillStore";
            StringBuilder emailContent = new StringBuilder();
            
            emailContent.append("Dear ").append(customer.getName()).append(",\n\n");
            emailContent.append("Thank you for your order! Your order has been successfully placed.\n\n");
            emailContent.append("Order Details:\n");
            emailContent.append("Order ID: ").append(order.getOrderId()).append("\n");
            emailContent.append("Order Date: ").append(order.getOrderDate()).append("\n");
            emailContent.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
            
            emailContent.append("Items Ordered:\n");
            for (CartItemDTO item : cartItems) {
                emailContent.append("- ").append(item.getProductName())
                           .append(" x").append(item.getQuantity())
                           .append(" - $").append(String.format("%.2f", item.getTotalPrice())).append("\n");
            }
            
            emailContent.append("\nTotal Amount: $").append(String.format("%.2f", totalAmount)).append("\n");
            emailContent.append("Status: ").append(order.getStatus()).append("\n\n");
            
            emailContent.append("We will process your order and ship it to you as soon as possible.\n");
            emailContent.append("You will receive another email with tracking information once your order ships.\n\n");
            emailContent.append("Thank you for choosing ChillStore!\n");
            emailContent.append("Best regards,\nChillStore Team");
            
            emailService.sendEmail(customer.getEmail(), subject, emailContent.toString());
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc tạo order
        }
    }
} 