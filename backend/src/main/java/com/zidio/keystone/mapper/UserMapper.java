package com.zidio.keystone.mapper;

import com.zidio.keystone.dto.UserDto;
import com.zidio.keystone.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User u) {
        if (u == null) return null;
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive());
    }
}
