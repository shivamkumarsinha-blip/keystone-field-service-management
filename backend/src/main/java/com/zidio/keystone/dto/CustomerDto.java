package com.zidio.keystone.dto;

public record CustomerDto(
        Long id,
        String name,
        String contactEmail,
        String contactPhone,
        boolean active
) {}
