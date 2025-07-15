package com.esms.service.impl;

import com.esms.config.VNPayConfig;
import com.esms.model.dto.VNPayRequestDTO;
import com.esms.model.dto.VNPayResponseDTO;
import com.esms.service.VNPayService;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    @Override
    public String createPaymentUrl(VNPayRequestDTO request) {
        try {
            // Validate input
            if (request == null) {
                throw new IllegalArgumentException("VNPay request cannot be null");
            }
            if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }
            if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }

            String vnp_Version = vnPayConfig.getVersion();
            String vnp_Command = vnPayConfig.getCommand();
            String vnp_TmnCode = vnPayConfig.getTmnCode();
            String vnp_HashSecret = vnPayConfig.getHashSecret();
            String vnp_Url = vnPayConfig.getUrl();
            String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
            String vnp_IpnUrl = vnPayConfig.getIpnUrl();
            String vnp_TxnRef = request.getOrderId();
            String vnp_IpAddr = request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1";
            String vnp_TxnDesc = request.getOrderInfo() != null ? request.getOrderInfo() : "Thanh toan don hang";
            String vnp_OrderInfo = request.getOrderInfo() != null ? request.getOrderInfo() : "Thanh toan don hang";
            String vnp_OrderType = "other";

            // VNPay yêu cầu amount dạng số nguyên (x100) và không có dấu phẩy
            String vnp_Amount = String.valueOf(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue());

            String vnp_Locale = request.getLocale() != null ? request.getLocale() : vnPayConfig.getLocale();
            String vnp_CurrCode = request.getCurrency() != null ? request.getCurrency() : vnPayConfig.getCurrCode();
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // Validate configuration
            if (vnp_TmnCode == null || vnp_TmnCode.trim().isEmpty()) {
                throw new RuntimeException("VNPay TMN Code is not configured");
            }
            if (vnp_HashSecret == null || vnp_HashSecret.trim().isEmpty()) {
                throw new RuntimeException("VNPay Hash Secret is not configured");
            }
            if (vnp_Url == null || vnp_Url.trim().isEmpty()) {
                throw new RuntimeException("VNPay URL is not configured");
            }

            // Tạo Map chứa tất cả tham số theo đúng format VNPay
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpnUrl", vnp_IpnUrl);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            // Thêm các tham số tùy chọn nếu có
            if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
                vnp_Params.put("vnp_CustomerEmail", request.getCustomerEmail());
            }
            if (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
                vnp_Params.put("vnp_CustomerPhone", request.getCustomerPhone());
            }
            if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
                vnp_Params.put("vnp_CustomerName", request.getCustomerName());
            }
            if (request.getCustomerAddress() != null && !request.getCustomerAddress().trim().isEmpty()) {
                vnp_Params.put("vnp_CustomerAddress", request.getCustomerAddress());
            }

            // Sắp xếp các tham số theo thứ tự alphabet
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data (không encode)
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);

                    // Build query (có encode)
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = createSecureHash(hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            String finalUrl = vnp_Url + "?" + queryUrl;

            // Log for debugging (remove in production)
            System.out.println("=== VNPay Payment URL Debug ===");
            System.out.println("Order ID: " + vnp_TxnRef);
            System.out.println("Amount (VND): " + vnp_Amount);
            System.out.println("Original Amount: " + request.getAmount());
            System.out.println("Return URL: " + vnp_ReturnUrl);
            System.out.println("Hash Data: " + hashData.toString());
            System.out.println("Secure Hash: " + vnp_SecureHash);
            System.out.println("Final URL: " + finalUrl);
            System.out.println("================================");

            return finalUrl;
        } catch (Exception e) {
            System.err.println("Error creating VNPay payment URL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating VNPay payment URL: " + e.getMessage(), e);
        }
    }

    @Override
    public VNPayResponseDTO processPaymentCallback(String responseCode, String orderId,
                                                   String amount, String secureHash,
                                                   String bankTranNo, String transactionNo,
                                                   String responseMessage) {
        VNPayResponseDTO response = new VNPayResponseDTO();
        response.setOrderId(orderId);
        response.setResponseCode(responseCode);
        response.setResponseMessage(responseMessage != null ? responseMessage : "");
        response.setBankTranNo(bankTranNo);
        response.setTransactionNo(transactionNo);
        response.setSecureHash(secureHash);

        if (amount != null) {
            try {
                response.setAmount(new java.math.BigDecimal(amount).divide(new java.math.BigDecimal("100")));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing amount: " + amount);
                response.setAmount(java.math.BigDecimal.ZERO);
            }
        }

        // Kiểm tra tính hợp lệ
        boolean isValid = validateCallback(responseCode, orderId, amount, secureHash,
                bankTranNo, transactionNo, responseMessage);
        response.setSuccess(isValid && "00".equals(responseCode));

        // Log for debugging
        System.out.println("VNPay Callback processed - Order: " + orderId +
                ", Response: " + responseCode +
                ", Success: " + response.isSuccess());

        return response;
    }

    @Override
    public boolean validateCallback(String responseCode, String orderId,
                                    String amount, String secureHash,
                                    String bankTranNo, String transactionNo,
                                    String responseMessage) {
        try {
            if (secureHash == null || secureHash.trim().isEmpty()) {
                System.err.println("Secure hash is null or empty");
                return false;
            }

            // Tạo chuỗi hash để so sánh theo đúng format VNPay callback
            StringBuilder hashData = new StringBuilder();
            hashData.append("vnp_Amount=").append(amount != null ? amount : "");
            hashData.append("&vnp_BankCode=").append("");
            hashData.append("&vnp_BankTranNo=").append(bankTranNo != null ? bankTranNo : "");
            hashData.append("&vnp_CardType=").append("");
            hashData.append("&vnp_OrderInfo=").append("");
            hashData.append("&vnp_PayDate=").append("");
            hashData.append("&vnp_ResponseCode=").append(responseCode != null ? responseCode : "");
            hashData.append("&vnp_TmnCode=").append(vnPayConfig.getTmnCode());
            hashData.append("&vnp_TransactionNo=").append(transactionNo != null ? transactionNo : "");
            hashData.append("&vnp_TransactionStatus=").append("00");
            hashData.append("&vnp_TxnRef=").append(orderId != null ? orderId : "");

            boolean isValid = validateSecureHash(hashData.toString(), secureHash);

            if (!isValid) {
                System.err.println("VNPay hash validation failed");
                System.err.println("Expected hash data: " + hashData.toString());
                System.err.println("Received hash: " + secureHash);
            }

            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating VNPay callback: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String createSecureHash(String params) {
        try {
            if (vnPayConfig.getHashSecret() == null || vnPayConfig.getHashSecret().trim().isEmpty()) {
                throw new RuntimeException("VNPay Hash Secret is not configured");
            }
            return new HmacUtils(HmacAlgorithms.HMAC_SHA_512, vnPayConfig.getHashSecret())
                    .hmacHex(params);
        } catch (Exception e) {
            System.err.println("Error creating secure hash: " + e.getMessage());
            throw new RuntimeException("Error creating secure hash", e);
        }
    }

    @Override
    public boolean validateSecureHash(String params, String secureHash) {
        try {
            if (params == null || secureHash == null) {
                return false;
            }
            String expectedHash = createSecureHash(params);
            boolean isValid = expectedHash.equals(secureHash);

            if (!isValid) {
                System.err.println("Hash validation failed");
                System.err.println("Expected: " + expectedHash);
                System.err.println("Received: " + secureHash);
            }

            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating secure hash: " + e.getMessage());
            return false;
        }
    }
} 