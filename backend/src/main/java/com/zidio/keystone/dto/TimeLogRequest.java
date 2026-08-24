package com.zidio.keystone.dto;

import jakarta.validation.constraints.Positive;

public record TimeLogRequest(
        @Positive Integer minutes,
        String note
) {}
