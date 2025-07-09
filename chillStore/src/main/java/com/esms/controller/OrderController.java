package com.esms.controller;

import com.esms.model.dto.OrderDTO;
import com.esms.model.dto.OrderItemDetailDTO;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping({"/admin/orders", "/staff/orders"})
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String listOrders(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "fromDate", required = false) String fromDate,
                             @RequestParam(value = "toDate", required = false) String toDate,
                             @RequestParam(value = "sortBy", defaultValue = "orderDate") String sortBy,
                             @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                             Model model) {
        List<OrderDTO> orders;

        // Check if the keyword is a negative integer
        if (keyword != null && keyword.matches("^-\\d+$")) {
            orders = new java.util.ArrayList<>();
            model.addAttribute("searchError", "Mã đơn hàng không thể là số âm.");
        } else {
            orders = orderService.searchOrdersWithFilter(keyword, status, fromDate, toDate, sortBy, sortDir);
        }

        List<String> statuses = Arrays.asList("Pending", "Paid", "Shipped", "Delivered", "Cancelled");

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", statuses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "staff/order/manage-order";
    }

    @PostMapping("/confirm/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String confirmOrder(@PathVariable Integer orderId) {
        orderService.confirmOrder(orderId);
        return "redirect:/staff/orders";
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ResponseBody
    public java.util.List<OrderItemDetailDTO> getOrderItems(@PathVariable Integer orderId) {
        return orderService.getOrderItemsDetail(orderId);
    }

    @PostMapping("/{orderId}/update-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ResponseBody
    public String updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status,
            @RequestParam(required = false) String refundReason) {
        try {
            orderService.updateOrderStatus(orderId, status, refundReason);
            return "success";
        } catch (Exception e) {
            throw new RuntimeException("Error updating order status: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/confirm-refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ResponseBody
    public String confirmRefund(@PathVariable Integer orderId) {
        try {
            orderService.confirmRefund(orderId);
            return "success";
        } catch (Exception e) {
            throw new RuntimeException("Error confirming refund: " + e.getMessage());
        }
    }
} 