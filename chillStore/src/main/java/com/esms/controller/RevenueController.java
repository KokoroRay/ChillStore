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
import java.util.Calendar;
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

        Date startDate = null;
        Date endDate = null;

        if ("custom".equals(period)) {
            if (startLocalDate != null) {
                startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (endLocalDate != null) {
                endDate = Date.from(endLocalDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            }
            if (startDate == null) {
                startDate = Date.from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (endDate == null) {
                endDate = new Date();
            }
        } else {
            Date[] dateRange = new Date[2];
            adjustDatesForPeriod(period, dateRange);
            startDate = dateRange[0];
            endDate = dateRange[1];
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
        
        // Debug log
        System.out.println("Period: " + period);
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);
        System.out.println("Revenue Trend Size: " + (revenueTrend != null ? revenueTrend.size() : "null"));
        System.out.println("Revenue By Category Size: " + (revenueByCategory != null ? revenueByCategory.size() : "null"));

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

    //cái này dùng để tính ngày
    private void adjustDatesForPeriod(String period, Date[] dates) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);

        if (period.equals("daily")) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dates[0] = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            dates[1] = cal.getTime();
        } else if (period.equals("weekly")) {
            cal.setTime(now);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dates[0] = cal.getTime();
            cal.add(Calendar.DAY_OF_WEEK, 6);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            dates[1] = cal.getTime();
        } else if (period.equals("monthly")) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dates[0] = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            dates[1] = cal.getTime();
        } else if (period.equals("quarterly")) {
            int currenMonth = cal.get(Calendar.MONTH);
            int startMonth = (currenMonth / 3) * 3;
            cal.set(Calendar.MONTH, startMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dates[0] = cal.getTime();
            cal.add(Calendar.MONTH, 2);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            dates[1] = cal.getTime();
        } else if (period.equals("yearly")) {
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dates[0] = cal.getTime();
            cal.set(Calendar.MONTH, 11);
            cal.set(Calendar.DAY_OF_MONTH, 31);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            dates[1] = cal.getTime();
        }
    }
}
