package com.zidio.keystone.dto;

public record SiteDto(
        Long id,
        Long customerId,
        String name,
        String addressLine,
        String city,
        String state,
        String postalCode,
        boolean active
) {}
