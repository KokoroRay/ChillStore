package com.esms.service;

import com.esms.dto.VNPayRequestDTO;
import com.esms.dto.VNPayResponseDTO;

public interface VNPayService {
    
    /**
     * Tạo URL thanh toán VNPay
     * @param request Thông tin thanh toán
     * @return URL thanh toán VNPay
     */
    String createPaymentUrl(VNPayRequestDTO request);
    
    /**
     * Xử lý callback từ VNPay
     * @param responseCode Mã phản hồi
     * @param orderId Mã đơn hàng
     * @param amount Số tiền
     * @param secureHash Hash bảo mật
     * @param bankTranNo Mã giao dịch ngân hàng
     * @param transactionNo Mã giao dịch VNPay
     * @param responseMessage Thông điệp phản hồi
     * @return Kết quả xử lý
     */
    VNPayResponseDTO processPaymentCallback(String responseCode, String orderId, 
                                          String amount, String secureHash, 
                                          String bankTranNo, String transactionNo, 
                                          String responseMessage);
    
    /**
     * Kiểm tra tính hợp lệ của callback
     * @param responseCode Mã phản hồi
     * @param orderId Mã đơn hàng
     * @param amount Số tiền
     * @param secureHash Hash bảo mật
     * @param bankTranNo Mã giao dịch ngân hàng
     * @param transactionNo Mã giao dịch VNPay
     * @param responseMessage Thông điệp phản hồi
     * @return true nếu hợp lệ
     */
    boolean validateCallback(String responseCode, String orderId, 
                           String amount, String secureHash, 
                           String bankTranNo, String transactionNo, 
                           String responseMessage);
    
    /**
     * Tạo hash bảo mật
     * @param params Các tham số
     * @return Hash bảo mật
     */
    String createSecureHash(String params);
    
    /**
     * Kiểm tra hash bảo mật
     * @param params Các tham số
     * @param secureHash Hash bảo mật
     * @return true nếu hợp lệ
     */
    boolean validateSecureHash(String params, String secureHash);
} 