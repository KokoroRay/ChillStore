package com.esms.service.impl;

import com.esms.model.dto.OrderDto;
import com.esms.model.entity.Order;
import com.esms.repository.OrderRepository;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> searchOrders(String keyword, String status) {
        return orderRepository.searchOrders(keyword, status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void confirmOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));
        order.setStatus("Đã xác nhận");
        orderRepository.save(order);
    }

    private OrderDto convertToDto(Order order) {
        return new OrderDto(
                order.getOrderId(),
                order.getCustomer().getName(),
                order.getDiscountAmount(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod());
    }
} 