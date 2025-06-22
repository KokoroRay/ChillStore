package com.esms.service;

import com.esms.model.dto.OrderDto;
import java.util.List;

public interface IOrderService {
    List<OrderDto> getAllOrders();
    List<OrderDto> searchOrders(String keyword, String status);
    void confirmOrder(Long orderId);
} 