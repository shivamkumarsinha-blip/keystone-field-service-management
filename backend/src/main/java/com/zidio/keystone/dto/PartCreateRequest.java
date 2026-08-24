package com.zidio.keystone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PartCreateRequest(
        @NotBlank String name,
        @NotBlank String sku,
        @PositiveOrZero Integer quantityInStock,
        @PositiveOrZero BigDecimal unitCost
) {}
