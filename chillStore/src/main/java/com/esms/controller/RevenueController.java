package com.esms.controller;

import com.esms.model.entity.Order;
import com.esms.repository.OrderRepository;
import com.esms.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping
    public String getRevenue(
            @RequestParam(value = "period", defaultValue = "daily") String period,
            @RequestParam(value = "startDate", required = false) String starDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "status", required = false) String status,

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = starDateStr != null ? sdf.parse(starDateStr) : null;
            Date endDate = endDateStr != null ? sdf.parse(endDateStr) : null;

            String periodFormat;
            switch (period) {
                case "weekly":
                    periodFormat = "yyyy-'W'ww";
                    break;
                case "monthly":
                    periodFormat = "yyyy-MM";
                    break;
                case "yearly":
                    periodFormat = "yyyy";
                    break;
                default:
                    periodFormat = "yyyy-MM-dd";
            }
            Long grossRevenue = orderRepository.getGrossRevenue();
            Long netRevenue = orderRepository.getNetRevenue();
            Long orderCount = orderRepository.getOrderCount();
            Double aov = orderRepository.getAverageOrderValue();

            List<Object[]> revenueTrend = orderRepository.getRevenueTrend(periodFormat, startDate, endDate);
            List<Object[]> revenueByCategory = orderRepository.getRevenueByCategory(startDate, endDate);
            List<Object[]> revenueByCustomer = orderRepository.getRevenueByCustomer(startDate, endDate);
            List<Object[]> productStats = orderItemRepository.getProductSalesStatistics(startDate, endDate);
            List<Object[]> orderStats = orderRepository.getParetoDate(startDate, endDate);
            Double cancellationRate = orderRepository.getCancellationRate(startDate, endDate);
            List<Object[]> revenueByRegion = orderRepository.getRevenueByRegion(startDate, endDate);

            //phan trang
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
            Page<Order> orderStatsPage = orderRepository.findOrdersByFilters(startDate, endDate, status, pageable);

            model.addAttribute("grossRevenue", grossRevenue);
            model.addAttribute("netRevenue", netRevenue);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("aov", aov);
            model.addAttribute("cancellationRate", cancellationRate);

            model.addAttribute("revenueTrend", revenueTrend);
            model.addAttribute("revenueByCategory", revenueByCategory);
            model.addAttribute("revenueByCustomer", revenueByCustomer);
            model.addAttribute("revenueByRegion", revenueByRegion);
            model.addAttribute("productStats", productStats);
            model.addAttribute("currentPage", page);
            model.addAttribute("period", period);
            model.addAttribute("endDate", endDateStr);
            return "admin/revenue/manage-revenue";

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


} 