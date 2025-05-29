package com.esms.util.MapperUtils;


import com.esms.domain.dto.CustomerDto;
import com.esms.domain.entity.Customer;

public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
    Customer toEntity(CustomerDto customerDto);
}
