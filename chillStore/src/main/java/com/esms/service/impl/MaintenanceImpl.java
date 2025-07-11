package com.esms.service.impl;

import com.esms.model.entity.Maintenance;
import com.esms.model.entity.Order;
import com.esms.model.entity.Product;
import com.esms.model.entity.Customer;
import com.esms.model.entity.Staff;
import com.esms.repository.MaintenanceRepository;
import com.esms.repository.OrderRepository;
import com.esms.repository.ProductRepository;
import com.esms.repository.CustomerRepository;
import com.esms.repository.StaffRepository;
import com.esms.service.MaintenanceService;
import com.esms.model.dto.MaintenanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceImpl implements MaintenanceService {
    @Autowired
    private MaintenanceRepository maintenanceRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public List<MaintenanceDto> getAllMaintenances() {
        List<Maintenance> maintenances = maintenanceRepository.findAll();
        return maintenances.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public MaintenanceDto getMaintenanceById(Integer id) {
        Maintenance entity = maintenanceRepository.findById(id).orElse(null);
        if (entity == null) return null;
        return toDto(entity);
    }

    @Override
    public void updateMaintenance(MaintenanceDto dto) {
        Maintenance entity = maintenanceRepository.findById(dto.getRequestId()).orElse(null);
        if (entity != null) {
            entity.setOrderId(dto.getOrderId());
            entity.setProductId(dto.getProductId());
            entity.setCustomerId(dto.getCustomerId());
            entity.setRequestType(dto.getRequestType());
            entity.setRequestDate(dto.getRequestDate());
            entity.setReason(dto.getReason());
            entity.setStatus(dto.getStatus());
            maintenanceRepository.save(entity);
        }
    }

    @Override
    public void addMaintenance(MaintenanceDto dto) {
        Maintenance entity = new Maintenance();
        entity.setOrderId(dto.getOrderId());
        entity.setProductId(dto.getProductId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setRequestType(dto.getRequestType());
        entity.setRequestDate(dto.getRequestDate());
        entity.setReason(dto.getReason());
        entity.setStatus(dto.getStatus());
        maintenanceRepository.save(entity);
    }

    private MaintenanceDto toDto(Maintenance entity) {
        // Get related entities
        Order order = orderRepository.findById(entity.getOrderId()).orElse(null);
        Product product = productRepository.findById(entity.getProductId()).orElse(null);
        Customer customer = customerRepository.findById(entity.getCustomerId()).orElse(null);
        Staff staff = null;
        String staffName = "";
        if (entity.getStaffId() != 0) {
            staff = staffRepository.findById(entity.getStaffId()).orElse(null);
            if (staff != null) {
                staffName = staff.getName();
            }
        }
        
        // Build info strings
        String orderInfo = "";
        if (order != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            orderInfo = "Order #" + order.getOrderId() + " - " + order.getCustomer().getName() + 
                       " (" + order.getOrderDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(formatter) + 
                       " - " + order.getStatus() + ")";
        }
        
        String productInfo = "";
        if (product != null) {
            productInfo = product.getName() + " - " + product.getCategory().getName() + 
                         " (" + product.getBrand().getName() + ")";
        }
        
        String customerInfo = "";
        if (customer != null) {
            customerInfo = customer.getName() + " (" + customer.getPhone() + ")";
        }
        
        return new MaintenanceDto(
                entity.getRequestId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getCustomerId(),
                entity.getStaffId(),
                entity.getRequestType(),
                entity.getRequestDate(),
                entity.getReason(),
                entity.getStatus(),
                orderInfo,
                productInfo,
                customerInfo,
                staffName
        );
    }
}
