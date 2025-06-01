package com.esms.util.MapperUtils;


import com.esms.model.dto.RegisterDto;
import com.esms.model.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    RegisterDto toDto(Customer customer);
    Customer toEntity(RegisterDto registerDto);
}
