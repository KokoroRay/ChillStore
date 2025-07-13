package com.esms.service;

public interface CheckoutService {
    /**
     * Tạo đơn hàng mới từ giỏ hàng
     * @param customerId ID của khách hàng
     * @param customerName Tên khách hàng
     * @param customerPhone Số điện thoại
     * @param customerEmail Email khách hàng
     * @param deliveryAddress Địa chỉ giao hàng
     * @param paymentMethod Phương thức thanh toán
     * @param orderNotes Ghi chú đơn hàng
     * @param voucherCode Mã voucher (nếu có)
     * @return ID của đơn hàng đã tạo
     */
    Integer createOrder(Integer customerId, 
                       String customerName, 
                       String customerPhone, 
                       String customerEmail,
                       String deliveryAddress, 
                       String paymentMethod, 
                       String orderNotes, 
                       String voucherCode);
} 