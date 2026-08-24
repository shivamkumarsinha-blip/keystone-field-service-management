package com.zidio.keystone.dto;

import com.zidio.keystone.enums.Role;

public record UserDto(
        Long id,
        String fullName,
        String email,
        Role role,
        boolean active
) {}
