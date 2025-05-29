package com.esms.service;

import com.esms.domain.dto.CustomerDto;
import com.esms.domain.entity.Customer;

public interface CustomerService {
    CustomerDto register(CustomerDto customerDto);
}
