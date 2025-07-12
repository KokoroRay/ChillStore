package com.esms.service.impl;

import com.esms.config.VNPayConfig;
import com.esms.dto.VNPayRequestDTO;
import com.esms.dto.VNPayResponseDTO;
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
            String vnp_Version = vnPayConfig.getVersion();
            String vnp_Command = vnPayConfig.getCommand();
            String vnp_TmnCode = vnPayConfig.getTmnCode();
            String vnp_HashSecret = vnPayConfig.getHashSecret();
            String vnp_Url = vnPayConfig.getUrl();
            String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
            String vnp_IpnUrl = vnPayConfig.getIpnUrl();
            String vnp_TxnRef = request.getOrderId();
            String vnp_IpAddr = request.getIpAddress();
            String vnp_TxnDesc = request.getOrderInfo();
            String vnp_OrderInfo = request.getOrderInfo();
            String vnp_OrderType = "other";
            String vnp_Amount = String.valueOf(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue());
            String vnp_Locale = request.getLocale() != null ? request.getLocale() : vnPayConfig.getLocale();
            String vnp_CurrCode = request.getCurrency() != null ? request.getCurrency() : vnPayConfig.getCurrCode();
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_BankCode", "");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpnUrl", vnp_IpnUrl);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
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
            return vnp_Url + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL", e);
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
        response.setResponseMessage(responseMessage);
        response.setBankTranNo(bankTranNo);
        response.setTransactionNo(transactionNo);
        response.setSecureHash(secureHash);
        
        if (amount != null) {
            response.setAmount(new java.math.BigDecimal(amount).divide(new java.math.BigDecimal("100")));
        }

        // Kiểm tra tính hợp lệ
        boolean isValid = validateCallback(responseCode, orderId, amount, secureHash, 
                                         bankTranNo, transactionNo, responseMessage);
        response.setSuccess(isValid && "00".equals(responseCode));

        return response;
    }

    @Override
    public boolean validateCallback(String responseCode, String orderId, 
                                  String amount, String secureHash, 
                                  String bankTranNo, String transactionNo, 
                                  String responseMessage) {
        try {
            // Tạo chuỗi hash để so sánh
            StringBuilder hashData = new StringBuilder();
            hashData.append("vnp_Amount=").append(amount);
            hashData.append("&vnp_BankCode=").append(bankTranNo != null ? bankTranNo : "");
            hashData.append("&vnp_BankTranNo=").append(bankTranNo != null ? bankTranNo : "");
            hashData.append("&vnp_CardType=").append("");
            hashData.append("&vnp_OrderInfo=").append("");
            hashData.append("&vnp_PayDate=").append("");
            hashData.append("&vnp_ResponseCode=").append(responseCode);
            hashData.append("&vnp_TmnCode=").append(vnPayConfig.getTmnCode());
            hashData.append("&vnp_TransactionNo=").append(transactionNo != null ? transactionNo : "");
            hashData.append("&vnp_TransactionStatus=").append("00");
            hashData.append("&vnp_TxnRef=").append(orderId);

            return validateSecureHash(hashData.toString(), secureHash);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String createSecureHash(String params) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_512, vnPayConfig.getHashSecret())
                .hmacHex(params);
    }

    @Override
    public boolean validateSecureHash(String params, String secureHash) {
        String expectedHash = createSecureHash(params);
        return expectedHash.equals(secureHash);
    }
} 