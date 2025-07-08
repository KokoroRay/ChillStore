package com.esms.service;

import com.esms.model.dto.CustomerOrderDetailDTO;
import com.esms.model.dto.OrderDTO;
import com.esms.model.dto.OrderItemDetailDTO;

import java.util.List;

public interface IOrderService {
    List<OrderDTO> getAllOrders();
    List<OrderDTO> searchOrders(String keyword, String status);
    List<OrderDTO> searchOrdersWithFilter(String keyword, String status, String fromDate, String toDate, String sortBy, String sortDir);
    void confirmOrder(Integer orderId);
    java.util.List<OrderItemDetailDTO> getOrderItemsDetail(Integer orderId);
    void updateOrderStatus(Integer orderId, String status, String refundReason);
    void confirmRefund(Integer orderId);
    List<OrderDTO> getOrderByCustomerId(Integer customerId);
    CustomerOrderDetailDTO getCustomerOrderDetail(Integer customerId, Integer orderId);
} 