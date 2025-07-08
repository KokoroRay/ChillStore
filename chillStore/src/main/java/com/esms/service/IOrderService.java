package com.esms.service;

import com.esms.model.dto.OrderDTO;
import com.esms.model.dto.OrderItemDetailDTO;

import java.util.List;

public interface IOrderService {
    List<OrderDTO> getAllOrders();
    List<OrderDTO> searchOrders(String keyword, String status);
    void confirmOrder(Integer orderId);
    java.util.List<OrderItemDetailDTO> getOrderItemsDetail(Integer orderId);
    void updateOrderStatus(Integer orderId, String status, String refundReason);
    void confirmRefund(Integer orderId);
} 