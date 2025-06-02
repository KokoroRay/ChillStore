package com.esms.util.MapperUtils;


import com.esms.model.dto.RegisterDto;
import com.esms.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    // Thêm mapper cho birthDate
    default LocalDate map(Date date) {
        if (date == null) { // Xử lý trường hợp date là null
            return null; // Hoặc trả về LocalDate.now() tùy theo logic của bạn
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Mapping(target = "birth_date", source = "birth_date") // Dòng này có vẻ đúng nếu birth_date có trong DTO
    Customer toEntity(RegisterDto dto);
}