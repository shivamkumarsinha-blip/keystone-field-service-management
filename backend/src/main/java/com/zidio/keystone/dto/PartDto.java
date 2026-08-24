package com.zidio.keystone.dto;

import java.math.BigDecimal;

public record PartDto(
        Long id,
        String name,
        String sku,
        Integer quantityInStock,
        BigDecimal unitCost,
        boolean active
) {}
