package com.zidio.keystone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerCreateRequest(
        @NotBlank String name,
        @Email String contactEmail,
        String contactPhone
) {}
