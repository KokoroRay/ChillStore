package com.esms.service.impl;

import com.esms.model.dto.OrderDto;
import com.esms.model.dto.OrderItemDetailDto;
import com.esms.model.entity.Order;
import com.esms.model.entity.OrderItem;
import com.esms.model.entity.Product;
import com.esms.repository.OrderRepository;
import com.esms.repository.OrderItemRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

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

    public java.util.List<OrderItemDetailDto> getOrderItemsDetail(Integer orderId) {
        java.util.List<OrderItem> items = orderItemRepository.findByIdOrderId(orderId);
        java.util.List<OrderItemDetailDto> result = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getId().getProductId()).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown";
            String categoryName = (product != null && product.getCategory() != null) ? product.getCategory().getName() : "Unknown";
            result.add(new OrderItemDetailDto(productName, item.getQuantity(),
                                              item.getPriceEach().doubleValue(), categoryName));
        }
        return result;
    }

    private OrderDto convertToDto(Order order) {
        int itemsCount = orderItemRepository.countItemsByOrderId(order.getOrderId());
        Double totalAmount = orderItemRepository.sumTotalAmountByOrderId(order.getOrderId());
        if (totalAmount == null) totalAmount = 0.0;
        return new OrderDto(
                order.getOrderId(),
                order.getCustomer().getName(),
                order.getDiscountAmount(),
                order.getOrderDate(),
                totalAmount,
                order.getStatus(),
                order.getPaymentMethod(),
                itemsCount);
    }
} 