package com.zidio.keystone.mapper;

import com.zidio.keystone.dto.CustomerDto;
import com.zidio.keystone.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerDto toDto(Customer c) {
        if (c == null) return null;
        return new CustomerDto(c.getId(), c.getName(), c.getContactEmail(), c.getContactPhone(), c.isActive());
    }
}
