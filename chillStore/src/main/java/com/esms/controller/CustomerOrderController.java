package com.esms.controller;

import com.esms.model.dto.CustomerOrderDetailDTO;
import com.esms.model.dto.OrderDTO;
import com.esms.service.CustomerService;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerOrderController {

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private CustomerService customerService;

    @GetMapping("/order-history")
    public String orderHistory(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer customerId = customerService.getCustomerByEmail(email).getCustomerId();
            
            List<OrderDTO> orders = orderService.getOrderByCustomerId(customerId);
            model.addAttribute("orders", orders);
            
            return "customer/order/order-history";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load order history");
            return "customer/order/order-history";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable Integer orderId, Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            Integer customerId = customerService.getCustomerByEmail(email).getCustomerId();
            
            CustomerOrderDetailDTO orderDetail = orderService.getCustomerOrderDetail(customerId, orderId);
            
            if (orderDetail == null) {
                model.addAttribute("error", "Order not found or access denied");
                return "customer/order/view-order";
            }
            
            model.addAttribute("order", orderDetail);
            return "customer/order/view-order";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load order details");
            return "customer/order/view-order";
        }
    }

    @GetMapping("/order")
    public String currentOrders(Model model, Authentication authentication, 
                               @RequestParam(defaultValue = "all") String status) {
        try {
            String email = authentication.getName();
            Integer customerId = customerService.getCustomerByEmail(email).getCustomerId();
            
            List<OrderDTO> orders = orderService.getOrderByCustomerId(customerId);
            
            // Filter by status if specified
            if (!"all".equals(status)) {
                orders = orders.stream()
                    .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                    .toList();
            }
            
            model.addAttribute("orders", orders);
            model.addAttribute("selectedStatus", status);
            
            return "customer/order/current-orders";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load orders");
            return "customer/order/current-orders";
        }
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<?> getOrdersAjax(Authentication authentication, 
                                          @RequestParam(defaultValue = "all") String status) {
        try {
            String email = authentication.getName();
            Integer customerId = customerService.getCustomerByEmail(email).getCustomerId();
            
            List<OrderDTO> orders = orderService.getOrderByCustomerId(customerId);
            
            // Filter by status if specified
            if (!"all".equals(status)) {
                orders = orders.stream()
                    .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                    .toList();
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Unable to load orders");
        }
    }
}
