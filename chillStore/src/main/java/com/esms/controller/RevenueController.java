package com.esms.controller;

import com.esms.model.entity.Category;
import com.esms.repository.CategoryRepository;
import com.esms.repository.OrderRepository;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import jakarta.persistence.criteria.CriteriaBuilder;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

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
            @RequestParam(value = "comparePeriod", required = false, defaultValue = "false") boolean comparePeriod,
            @RequestParam(value = "compareYoY", required = false, defaultValue = "false") boolean compareYoY,
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
        BigDecimal grossRevenue = orderRepository.getGrossRevenue(startDate, endDate, category, region, status);
        BigDecimal netRevenue = orderRepository.getNetRevenueBetween(startDate, endDate, category, region, status);
        Long orderCount = orderRepository.getOrderCountBetween(startDate, endDate, category, region, status);
        Double aov = orderCount > 0 ? netRevenue.doubleValue() / orderCount : 0.0;
        Double cancellationRate = orderRepository.getCancellationRate(startDate, endDate, category, region, status);
        Double grossMargin = netRevenue.doubleValue() > 0 ?
                (netRevenue.doubleValue() - getCostOfGoodsSold(startDate, endDate, category, region, status)) / netRevenue.doubleValue() : 0.0;


        // Get chart data - sử dụng cùng logic cho tất cả period
        List<Object[]> rawRevenueTrend = orderRepository.getRevenueTrend(period, startDate, endDate, category, region, status);
        List<Object[]> revenueTrend = groupRevenueByPeriod(rawRevenueTrend, period);
        List<Object[]> revenueByCategory = orderRepository.getRevenueByCategory(startDate, endDate, null, null, null);
        List<Object[]> revenueByRegion = orderRepository.getRevenueByRegion(startDate, endDate, null, null, null);
        List<Object[]> paretoData = orderRepository.getParetoDate(startDate, endDate, null, null, null);

        BigDecimal prevGrossRevenue = BigDecimal.ZERO;
        BigDecimal prevNetRevenue = BigDecimal.ZERO;
        Long prevOrderCount = 0L;
        Double prevAov = 0.0;
        Double prevCancellationRate = 0.0;
        Double preGrossMargin = 0.0;

        BigDecimal yoyGrossRevenue = BigDecimal.ZERO;
        BigDecimal yoyNetRevenue = BigDecimal.ZERO;
        Long yoyOrderCount = 0L;
        Double yoyCancellationRate = 0.0;
        Double yoyAov = 0.0;
        Double yoyGrossMargin = 0.0;

        List<Object[]> prevRevenueTrend = null;
        List<Object[]> yoyRevenueTrend = null;

        long durationInMillis = endDate.getTime() - startDate.getTime();
        Date prevStartDate = new Date(startDate.getTime() - durationInMillis);
        Date prevEndDate = new Date(endDate.getTime() - durationInMillis);

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.YEAR, -1);
        Date yoyStartDate = cal.getTime();
        cal.setTime(endDate);
        cal.add(Calendar.YEAR, -1);
        Date yoyEndDate = cal.getTime();

        Double grossRevenueChange = null;
        Double netRevenueChange = null;
        Double orderCountChange = null;
        Double aovChange = null;
        Double cancellationRateChange = null;
        Double grossMarginChange = null;
        Double yoyGrossRevenueChange = null;
        Double yoyNetRevenueChange = null;
        Double yoyOrderCountChange = null;
        Double yoyAovChange = null;
        Double yoyCancellationRateChange = null;
        Double yoyGrossMarginChange = null;

        if (comparePeriod) {
            prevGrossRevenue = orderRepository.getGrossRevenue(prevStartDate, prevEndDate, category, region, status);
            prevNetRevenue = orderRepository.getNetRevenueBetween(prevStartDate, prevEndDate, category, region, status);
            prevOrderCount = orderRepository.getOrderCountBetween(prevStartDate, prevEndDate, category, region, status);
            prevAov = prevOrderCount > 0 ? prevNetRevenue.doubleValue() / prevOrderCount : 0.0;
            prevCancellationRate = orderRepository.getCancellationRate(prevStartDate, prevEndDate, category, region, status);
            preGrossMargin = prevNetRevenue.doubleValue() > 0 ?
                    (prevNetRevenue.doubleValue() - getCostOfGoodsSold(prevStartDate, prevEndDate, category, region, status)) / prevNetRevenue.doubleValue() : 0.0;
            List<Object[]> rawPrevTrend = orderRepository.getRevenueTrend(period, prevStartDate, prevEndDate, category, region, status);
            prevRevenueTrend = groupRevenueByPeriod(rawPrevTrend, period);

            grossRevenueChange = calculatePercentageChange(prevGrossRevenue, grossRevenue);
            netRevenueChange = calculatePercentageChange(prevNetRevenue, netRevenue);
            orderCountChange = calculatePercentageChange(prevOrderCount.doubleValue(), orderCount.doubleValue());
            aovChange = calculatePercentageChange(prevAov, aov);
            cancellationRateChange = calculatePercentageChange(prevCancellationRate, cancellationRate);
            grossMarginChange = calculatePercentageChange(preGrossMargin, grossMargin);
        }

        if (compareYoY) {
            yoyGrossRevenue = orderRepository.getGrossRevenue(yoyStartDate, yoyEndDate, category, region, status);
            yoyNetRevenue = orderRepository.getNetRevenueBetween(yoyStartDate, yoyEndDate, category, region, status);
            yoyOrderCount = orderRepository.getOrderCountBetween(yoyStartDate, yoyEndDate, category, region, status);
            yoyAov = yoyOrderCount > 0 ? yoyNetRevenue.doubleValue() / yoyOrderCount : 0.0;
            yoyCancellationRate = orderRepository.getCancellationRate(yoyStartDate, yoyEndDate, category, region, status);
            yoyGrossMargin = yoyNetRevenue.doubleValue() > 0 ?
                    (yoyNetRevenue.doubleValue() - getCostOfGoodsSold(yoyStartDate, yoyEndDate, category, region, status)) / yoyNetRevenue.doubleValue() : 0.0;
            List<Object[]> rawYoyTrend = orderRepository.getRevenueTrend(period, yoyStartDate, yoyEndDate, category, region, status);
            yoyRevenueTrend = groupRevenueByPeriod(rawYoyTrend, period);

            yoyGrossRevenueChange = calculatePercentageChange(yoyGrossRevenue, grossRevenue);
            yoyNetRevenueChange = calculatePercentageChange(yoyNetRevenue, netRevenue);
            yoyOrderCountChange = calculatePercentageChange(yoyOrderCount.doubleValue(), orderCount.doubleValue());
            yoyAovChange = calculatePercentageChange(yoyAov, aov);
            yoyCancellationRateChange = calculatePercentageChange(yoyCancellationRate, cancellationRate);
            yoyGrossMarginChange = calculatePercentageChange(yoyGrossMargin, grossMargin);
        }

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

        //lấy dữ liệu của kì trước
        model.addAttribute("comparePeriod", comparePeriod);
        model.addAttribute("prevGrossRevenue", prevGrossRevenue);
        model.addAttribute("prevNetRevenue", prevNetRevenue);
        model.addAttribute("prevOrderCount", prevOrderCount);
        model.addAttribute("prevAov", prevAov);
        model.addAttribute("prevCancellationRate", prevCancellationRate);
        model.addAttribute("preGrossMargin", preGrossMargin);
        model.addAttribute("prevRevenueTrend", prevRevenueTrend);
        model.addAttribute("prevStartDate", prevStartDate);
        model.addAttribute("prevEndDate", prevEndDate);
        model.addAttribute("grossRevenueChange", grossRevenueChange);
        model.addAttribute("netRevenueChange", netRevenueChange);
        model.addAttribute("orderCountChange", orderCountChange);
        model.addAttribute("aovChange", aovChange);
        model.addAttribute("cancellationRateChange", cancellationRateChange);
        model.addAttribute("grossMarginChange", grossMarginChange);

        //lấy dữ liệu của năm trước

        model.addAttribute("compareYoY", compareYoY);
        model.addAttribute("yoyGrossRevenue", yoyGrossRevenue);
        model.addAttribute("yoyNetRevenue", yoyNetRevenue);
        model.addAttribute("yoyOrderCount", yoyOrderCount);
        model.addAttribute("yoyAov", yoyAov);
        model.addAttribute("yoyCancellationRate", yoyCancellationRate);
        model.addAttribute("yoyGrossMargin", yoyGrossMargin);
        model.addAttribute("yoyRevenueTrend", yoyRevenueTrend);
        model.addAttribute("yoyStartDate", yoyStartDate);
        model.addAttribute("yoyEndDate", yoyEndDate);
        model.addAttribute("yoyGrossRevenueChange", yoyGrossRevenueChange);
        model.addAttribute("yoyNetRevenueChange", yoyNetRevenueChange);
        model.addAttribute("yoyOrderCountChange", yoyOrderCountChange);
        model.addAttribute("yoyAovChange", yoyAovChange);
        model.addAttribute("yoyCancellationRateChange", yoyCancellationRateChange);
        model.addAttribute("yoyGrossMarginChange", yoyGrossMarginChange);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("regions", orderRepository.findAllRegions());
        model.addAttribute("statuses", Arrays.asList("Paid", "Shipped", "Delivered", "Cancelled"));

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
    
    private List<Object[]> groupRevenueByPeriod(List<Object[]> rawData, String period) {
        if (rawData == null || rawData.isEmpty()) {
            return rawData;
        }
        
        if ("daily".equals(period) || "custom".equals(period)) {
            return rawData;
        }
        
        Map<String, Double> groupedData = new LinkedHashMap<>();
        
        for (Object[] row : rawData) {
            String dateStr = (String) row[0];
            Double revenue = ((Number) row[1]).doubleValue();
            
            String groupKey = formatDateByPeriod(dateStr, period);
            groupedData.merge(groupKey, revenue, Double::sum);
        }
        
        return groupedData.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }
    
    private String formatDateByPeriod(String dateStr, String period) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            switch (period) {
                case "weekly":
                    return date.getYear() + "-W" + date.get(WeekFields.ISO.weekOfYear());
                case "monthly":
                    return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                case "quarterly":
                    int quarter = (date.getMonthValue() - 1) / 3 + 1;
                    return date.getYear() + "-Q" + quarter;
                case "yearly":
                    return String.valueOf(date.getYear());
                default:
                    return dateStr;
            }
        } catch (Exception e) {
            return dateStr;
        }
    }

    private double getCostOfGoodsSold(Date startDate, Date endDate, Integer category, String region, String status) {
        // Simplified calculation (60% of net revenue)
        BigDecimal netRevenue = orderRepository.getNetRevenueBetween(startDate, endDate, category, region, status);
        return netRevenue != null ? netRevenue.doubleValue() * 0.6 : 0;
    }

    private Double calculatePercentageChange(Number previousValue, Number currentValue) {
        if (previousValue == null || currentValue == null) return 0.0;
        double prev = previousValue.doubleValue();
        double curr = currentValue.doubleValue();
        if(prev == 0) {
            return curr != 0 ? 100.0 : 0.0;
        }
        return ((curr - prev) / Math.abs(prev)) * 100;
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
