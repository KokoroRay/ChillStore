package com.esms.controller;

import com.esms.dto.VNPayRequestDTO;
import com.esms.dto.VNPayResponseDTO;
import com.esms.model.dto.CustomerOrderDetailDTO;
import com.esms.service.IOrderService;
import com.esms.service.VNPayService;
import com.esms.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/vnpay/{orderId}")
    public String vnpayPayment(@PathVariable Integer orderId, Model model,
                               HttpServletRequest request, HttpSession session) {
        try {
            // Kiểm tra authentication
            Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
            if (customerId == null) {
                return "redirect:/auth/login";
            }

            // Lấy thông tin đơn hàng với customerId để kiểm tra quyền sở hữu
            CustomerOrderDetailDTO orderDetail = orderService.getCustomerOrderDetail(customerId, orderId);
            if (orderDetail == null) {
                model.addAttribute("error", "Order not found or access denied");
                return "payment/vnpay";
            }

            // Kiểm tra trạng thái đơn hàng
            if (!"Pending".equals(orderDetail.getStatus())) {
                model.addAttribute("error", "Order is not in pending status for payment");
                return "payment/vnpay";
            }

            // Tạo request VNPay
            VNPayRequestDTO vnPayRequest = new VNPayRequestDTO();
            vnPayRequest.setOrderId(orderId.toString());
            vnPayRequest.setAmount(orderDetail.getTotalAmount());
            vnPayRequest.setOrderInfo("Thanh toan don hang " + orderId);
            vnPayRequest.setCustomerEmail(orderDetail.getCustomerEmail());
            vnPayRequest.setCustomerPhone(orderDetail.getCustomerPhone());
            vnPayRequest.setCustomerName(orderDetail.getCustomerName());
            vnPayRequest.setCustomerAddress(orderDetail.getCustomerAddress());
            vnPayRequest.setIpAddress(getClientIpAddress(request));
            vnPayRequest.setLocale("vn");
            vnPayRequest.setCurrency("VND");

            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(vnPayRequest);

            model.addAttribute("orderId", orderId);
            model.addAttribute("orderDetail", orderDetail);
            model.addAttribute("paymentUrl", paymentUrl);

            return "payment/vnpay";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating payment: " + e.getMessage());
            return "payment/vnpay";
        }
    }

    @GetMapping("/vnpay/callback")
    public String vnpayCallback(@RequestParam("vnp_ResponseCode") String responseCode,
                                @RequestParam("vnp_TxnRef") String orderId,
                                @RequestParam("vnp_Amount") String amount,
                                @RequestParam("vnp_SecureHash") String secureHash,
                                @RequestParam(value = "vnp_BankTranNo", required = false) String bankTranNo,
                                @RequestParam(value = "vnp_TransactionNo", required = false) String transactionNo,
                                @RequestParam(value = "vnp_ResponseMessage", required = false) String responseMessage,
                                RedirectAttributes redirectAttributes) {
        try {
            // Xử lý callback từ VNPay
            VNPayResponseDTO response = vnPayService.processPaymentCallback(
                    responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
            );

            if (response.isSuccess()) {
                // Cập nhật trạng thái đơn hàng thành "Paid"
                orderService.updateOrderStatus(Integer.parseInt(orderId), "Paid", "VNPay payment successful");
                redirectAttributes.addFlashAttribute("success", "Payment successful! Transaction ID: " + response.getTransactionNo());
                redirectAttributes.addFlashAttribute("paymentResponse", response);
                return "redirect:/payment/success/" + orderId;
            } else {
                // Cập nhật trạng thái đơn hàng thành "Cancelled"
                orderService.updateOrderStatus(Integer.parseInt(orderId), "Cancelled", "VNPay payment failed: " + response.getResponseMessage());
                redirectAttributes.addFlashAttribute("error", "Payment failed: " + response.getResponseMessage());
                redirectAttributes.addFlashAttribute("paymentResponse", response);
                return "redirect:/payment/failed/" + orderId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing payment: " + e.getMessage());
            return "redirect:/customer/order/" + orderId;
        }
    }

    @PostMapping("/vnpay/{orderId}/process")
    public String processVnpayPayment(@PathVariable Integer orderId, HttpSession session) {
        // Kiểm tra authentication
        Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
        if (customerId == null) {
            return "redirect:/auth/login";
        }

        // Redirect to VNPay payment
        return "redirect:/payment/vnpay/" + orderId;
    }

    @PostMapping("/vnpay/ipn")
    @ResponseBody
    public String vnpayIpn(@RequestParam Map<String, String> params) {
        try {
            // Xử lý IPN từ VNPay
            String responseCode = params.get("vnp_ResponseCode");
            String orderId = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            String secureHash = params.get("vnp_SecureHash");
            String bankTranNo = params.get("vnp_BankTranNo");
            String transactionNo = params.get("vnp_TransactionNo");
            String responseMessage = params.get("vnp_ResponseMessage");

            // Xử lý callback
            VNPayResponseDTO response = vnPayService.processPaymentCallback(
                    responseCode, orderId, amount, secureHash, bankTranNo, transactionNo, responseMessage
            );

            if (response.isSuccess()) {
                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(Integer.parseInt(orderId), "Paid", "VNPay IPN payment successful");
                return "OK";
            } else {
                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(Integer.parseInt(orderId), "Cancelled", "VNPay IPN payment failed: " + response.getResponseMessage());
                return "FAIL";
            }
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @GetMapping("/success/{orderId}")
    public String paymentSuccess(@PathVariable Integer orderId, Model model, HttpSession session) {
        try {
            // Kiểm tra authentication
            Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
            if (customerId == null) {
                return "redirect:/auth/login";
            }

            CustomerOrderDetailDTO orderDetail = orderService.getCustomerOrderDetail(customerId, orderId);
            if (orderDetail == null) {
                model.addAttribute("error", "Order not found or access denied");
                return "payment/payment-success";
            }

            model.addAttribute("orderDetail", orderDetail);
            return "payment/payment-success";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load order details");
            return "payment/payment-success";
        }
    }

    @GetMapping("/failed/{orderId}")
    public String paymentFailed(@PathVariable Integer orderId, Model model, HttpSession session) {
        try {
            // Kiểm tra authentication
            Integer customerId = (Integer) session.getAttribute("loggedInCustomerId");
            if (customerId == null) {
                return "redirect:/auth/login";
            }

            CustomerOrderDetailDTO orderDetail = orderService.getCustomerOrderDetail(customerId, orderId);
            if (orderDetail == null) {
                model.addAttribute("error", "Order not found or access denied");
                return "payment/payment-failed";
            }

            model.addAttribute("orderDetail", orderDetail);
            return "payment/payment-failed";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load order details");
            return "payment/payment-failed";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
} 