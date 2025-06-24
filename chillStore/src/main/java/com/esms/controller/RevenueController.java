package com.esms.controller;

import com.esms.repository.OrderRepository;
import com.esms.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class RevenueController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/admin/revenue")
    public String getRevenue(Model model) {
        Long totalRevenue = orderRepository.getTotalRevenue();
        List<Object[]> revenueByDate = orderRepository.getRevenueByDate();

        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Object[] row : revenueByDate) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("revenue", row[1]);
            chartData.add(map);
        }

        long totalOrders = orderRepository.count();

        List<Object[]> productStats = orderItemRepository.getProductSalesStatistics();
        List<Object[]> topStaff = orderItemRepository.getTop10StaffBySales();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        model.addAttribute("revenueByDate", chartData);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("productStats", productStats);
        model.addAttribute("topStaff", topStaff);
        return "admin/revenue/manage-revenue";
    }
} 