package com.zidio.keystone.dto;

import jakarta.validation.constraints.NotBlank;

public record SiteCreateRequest(
        @NotBlank String name,
        @NotBlank String addressLine,
        String city,
        String state,
        String postalCode
) {}
