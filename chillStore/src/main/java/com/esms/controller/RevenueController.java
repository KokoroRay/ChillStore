package com.esms.controller;

import com.esms.model.entity.Category;
import com.esms.repository.CategoryRepository;
import com.esms.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String getRevenue(
            @RequestParam(value = "period", defaultValue = "weekly") String period,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startLocalDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endLocalDate,
            @RequestParam(value = "category", required = false) Integer category,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        // Chuyển đổi LocalDate sang Date (nếu cần)
        Date startDate = null;
        Date endDate = null;
        if (startLocalDate != null) {
            startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (endLocalDate != null) {
            endDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Set default dates if not provided
        if (startDate == null) {
            startDate = Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (endDate == null) {
            endDate = new Date();
        }

        // Get revenue data
        BigDecimal grossRevenue = orderRepository.getGrossRevenue(startDate, endDate);
        BigDecimal netRevenue = orderRepository.getNetRevenueBetween(startDate, endDate);
        Long orderCount = orderRepository.getOrderCountBetween(startDate, endDate);
        Double aov = orderCount > 0 ? netRevenue.doubleValue() / orderCount : 0.0;
        Double cancellationRate = orderRepository.getCancellationRate(startDate, endDate);
        Double grossMargin = netRevenue.doubleValue() > 0 ?
                (netRevenue.doubleValue() - getCostOfGoodsSold(startDate, endDate)) / netRevenue.doubleValue() : 0.0;

        // Get chart data
        List<Object[]> revenueTrend = orderRepository.getRevenueTrend(period, startDate, endDate);
        List<Object[]> revenueByCategory = orderRepository.getRevenueByCategory(startDate, endDate);
        List<Object[]> revenueByRegion = orderRepository.getRevenueByRegion(startDate, endDate);
        List<Object[]> paretoData = orderRepository.getParetoDate(startDate, endDate);

        // Add data to model
        model.addAttribute("grossRevenue", grossRevenue);
        model.addAttribute("netRevenue", netRevenue);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("aov", aov);
        model.addAttribute("cancellationRate", cancellationRate);
        model.addAttribute("grossMargin", grossMargin);

        model.addAttribute("revenueTrend", revenueTrend);
        model.addAttribute("revenueByCategory", revenueByCategory);
        model.addAttribute("revenueByRegion", revenueByRegion);
        model.addAttribute("paretoData", paretoData);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("regions", orderRepository.findAllRegions());
        model.addAttribute("statuses", Arrays.asList("Pending", "Paid", "Shipped", "Delivered", "Cancelled"));

        model.addAttribute("period", period);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("status", status);

        // Set active menu for sidebar
        model.addAttribute("activeMenu", "revenue");

        return "admin/revenue/manage-revenue";
    }

    private double getCostOfGoodsSold(Date startDate, Date endDate) {
        // Simplified calculation (60% of net revenue)
        BigDecimal netRevenue = orderRepository.getNetRevenueBetween(startDate, endDate);
        return netRevenue != null ? netRevenue.doubleValue() * 0.6 : 0;
    }
}