package com.esms.service.impl;

import com.esms.model.dto.CustomerOrderDetailDTO;
import com.esms.model.dto.OrderDTO;
import com.esms.model.dto.OrderItemDetailDTO;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Order;
import com.esms.model.entity.OrderItem;
import com.esms.model.entity.Product;
import com.esms.repository.OrderRepository;
import com.esms.repository.OrderItemRepository;
import com.esms.repository.ProductRepository;
import com.esms.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderSupport;
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
    private PropertiesLoaderSupport propertiesLoaderSupport;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> searchOrders(String keyword, String status) {
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

    public java.util.List<OrderItemDetailDTO> getOrderItemsDetail(Integer orderId) {
        java.util.List<OrderItem> items = orderItemRepository.findByIdOrderId(orderId);
        java.util.List<OrderItemDetailDTO> result = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getId().getProductId()).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown";
            String categoryName = (product != null && product.getCategory() != null) ? product.getCategory().getName() : "Unknown";
            result.add(new OrderItemDetailDTO(productName, item.getQuantity(),
                                              item.getPriceEach().doubleValue(), categoryName));
        }
        return result;
    }

    @Override
    public void updateOrderStatus(Integer orderId, String status, String refundReason) {
        System.out.println("Updating order " + orderId + " from status to " + status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));
        String currentStatus = order.getStatus();
        System.out.println("Current status: " + currentStatus);
        // Xử lý refund status cho VNpay TRƯỚC khi cập nhật status
        if ("Cancelled".equals(status) && "VNpay".equals(order.getPaymentMethod())) {
            System.out.println("Setting refund status for VNpay order: " + orderId);
            if (refundReason != null && !refundReason.trim().isEmpty()) {
                System.out.println("Setting refund status to: " + refundReason);
                order.setRefundStatus(refundReason);
            } else {
                System.out.println("Setting default refund status to: pending_refund");
                order.setRefundStatus("pending_refund");
            }
        }

        // Cập nhật status sau khi đã set refund status
        if ("Pending".equals(currentStatus)) {
            if ("Shipped".equals(status) || "Cancelled".equals(status)) {
                order.setStatus(status);
            }
        } else if ("Shipped".equals(currentStatus)) {
            if ("Delivered".equals(status) || "Cancelled".equals(status)) {
                order.setStatus(status);
            }
        } else {
            throw  new IllegalArgumentException("Invalid status transition from " + currentStatus);
        }
        orderRepository.save(order);
    }

    @Override
    public void confirmRefund(Integer orderId) {
        System.out.println("Confirming refund for order " + orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + orderId));
        System.out.println("Order status: " + order.getStatus() + ", Payment: " + order.getPaymentMethod());
        if ("Cancelled".equals(order.getStatus()) && "VNpay".equals(order.getPaymentMethod())) {
            order.setRefundStatus("refunded");
            orderRepository.save(order);
            System.out.println("Refund confirmed successfully");
        } else {
            System.out.println("Cannot confirm refund - invalid conditions");
        }
    }

    //khúc này của customer khi order

    @Override
    public List<OrderDTO> getOrderByCustomerId(Integer customerId) {
        return orderRepository.findByCustomerCustomerId(customerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerOrderDetailDTO getCustomerOrderDetail(Integer customerId, Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Invalid order id: " + orderId));
        if (!order.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Order dose not belong to the customer");
        }
        CustomerOrderDetailDTO detailDTO = new CustomerOrderDetailDTO();
        detailDTO.setOrderId(order.getOrderId());
        detailDTO.setOrderDate(order.getOrderDate());
        detailDTO.setTotalAmount(order.getTotalAmount());
        detailDTO.setStatus(order.getStatus());
        detailDTO.setPaymentMethod(order.getPaymentMethod());
        detailDTO.setDiscountAmount(order.getDiscountAmount());
        detailDTO.setRefundStatus(order.getRefundStatus());
        detailDTO.setItems(getOrderItemsDetail(orderId));
        
        // Set customer information with null checks
        Customer customer = order.getCustomer();
        if (customer != null) {
            detailDTO.setCustomerName(customer.getName());
            detailDTO.setCustomerEmail(customer.getEmail());
            detailDTO.setCustomerPhone(customer.getPhone());
            detailDTO.setCustomerAddress(customer.getAddress());
        }
        
        return detailDTO;
    }

    private OrderDTO convertToDto(Order order) {
        int itemsCount = orderItemRepository.countItemsByOrderId(order.getOrderId());
        Double totalAmountRaw = orderItemRepository.sumTotalAmountByOrderId(order.getOrderId());
        java.math.BigDecimal totalAmount = totalAmountRaw != null ? java.math.BigDecimal.valueOf(totalAmountRaw) : java.math.BigDecimal.ZERO;
        return new OrderDTO(
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