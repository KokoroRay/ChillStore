package com.esms.controller;

import com.esms.model.dto.OrderDto;
import com.esms.model.dto.OrderItemDetailDto;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping({"/admin/order", "/staff/order"})
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) String status,
                             Model model) {
        List<OrderDto> orders;

        // Check if the keyword is a negative integer
        if (keyword != null && keyword.matches("^-\\d+$")) {
            orders = new java.util.ArrayList<>();
            model.addAttribute("searchError", "Mã đơn hàng (Receipt) không thể là số âm.");
        } else {
            orders = orderService.searchOrders(keyword, status);
        }
        
        List<String> statuses = Arrays.asList("Pending", "Paid", "Shipped", "Delivered", "Cancelled");

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", statuses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);

        return "staff/manage-order";
    }

    @PostMapping("/confirm/{orderId}")
    public String confirmOrder(@PathVariable Integer orderId) {
        orderService.confirmOrder(orderId);
        return "redirect:/staff/orders";
    }

    @GetMapping("/{orderId}/items")
    @ResponseBody
    public java.util.List<OrderItemDetailDto> getOrderItems(@PathVariable Integer orderId) {
        return orderService.getOrderItemsDetail(orderId);
    }
} 