package com.esms.service;

import com.esms.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class RevenueAlertService {
    private static final Logger logger = LoggerFactory.getLogger(RevenueAlertService.class);

    @Autowired
    private OrderRepository  orderRepository;

    @Autowired
    private EmailService  emailService;

    @Value("${revenue.alert.threshold:0.20}")
    private double alertThreshold;

    @Value("${revenue.alert.email:kokororay356@gmail.com}")
    private String alertEmail;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkRevenueAlert() {
        logger.info("Checking revenue drop alerts...");
        //so sánh doanh thu so với tháng trước đạt được
        checkMoMRevenue();
        //so sánh doanh thu với cùng kì năm ngoái đặt được
        checkYoYRevenue();
    }

    private void checkMoMRevenue() {
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate currentPeriodEnd = now;

        LocalDate previousPeriodStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate previousPeriodEnd = now.minusMonths(1);
        Double currentRevenue = getRevenueBetween(currentMonthStart, currentPeriodEnd);
        Double previousRevenue = getRevenueBetween(previousPeriodStart, previousPeriodEnd);

        if (currentRevenue > 0 && previousRevenue >0) {
            double change = (currentRevenue - previousRevenue) / previousRevenue;
            if (change < -alertThreshold) {
                String mess = String.format(
                        "Cảnh báo doanh thu tháng này đã giảm %.2f%% so với tháng trước\n" +
                                "Doanh thu hiện của chúng tại là: %, .0f VND\n" +
                                "Doanh thu tháng trước là: %, .0f VND\n" +
                                "Mức giảm đạt %, .0f VND",
                        Math.abs(change) * 100, currentRevenue, previousRevenue, (previousRevenue - currentRevenue)
                );
                sendAlert("Cảnh báo doanh thu tháng", mess);
            }
        }

    }

    private void checkYoYRevenue() {
        LocalDate now = LocalDate.now();
        LocalDate currentPeriodStart = now.withDayOfYear(1);
        LocalDate currentPeriodEnd = now;

        LocalDate previousPeriodStart = now.minusYears(1).withDayOfYear(1);
        LocalDate previousPeriodEnd = now.minusYears(1);

        Double currentRevenue = getRevenueBetween(currentPeriodStart, currentPeriodEnd);
        Double previousRevenue = getRevenueBetween(previousPeriodStart, previousPeriodEnd);

        if (currentRevenue > 0 && previousRevenue >0) {
           double change = (currentRevenue - previousRevenue) / previousRevenue;
           if (change < -alertThreshold) {
               String mess = String.format(
                       "Cảnh báo doanh thu năm hiện tại đã giảm %.2f%% so với năm ngoái\n" +
                               "Doanh thu hiện của năm nay đạt: %,.0f VND\n" +
                               "Doanh thu cùng kỳ đạt: %, 0f VND\n," +
                               "Mức giảm đạt: %,.0f VND",
                       Math.abs(change) * 100, currentRevenue, previousRevenue, (previousRevenue - currentRevenue)
               );
               sendAlert("Cảnh báo doanh thu năm", mess);
           }
        }
    }

    private Double getRevenueBetween(LocalDate start, LocalDate end) {
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return orderRepository.getNetRevenueBetween(startDate, endDate);
    }

    private void sendAlert(String subject, String mess) {
        try {
            emailService.sendEmail(alertEmail, subject, mess);
            logger.info("Sent revenue alert: {}", subject);
        } catch (Exception e) {
            logger.error("Failed to send revenue alert email", e);
        }
    }



}
