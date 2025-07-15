package com.esms;

import com.esms.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestDbController {

    private final CustomerRepository customerRepository;

    public TestDbController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/db")
    public ResponseEntity<String> testDbConnection() {
        long count = customerRepository.count(); // Truy vấn đơn giản
        return ResponseEntity.ok("Kết nối DB thành công! Tổng số khách hàng: " + count);
    }
}